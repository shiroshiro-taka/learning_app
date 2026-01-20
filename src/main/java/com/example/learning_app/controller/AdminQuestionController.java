package com.example.learning_app.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.learning_app.entity.Choice;
import com.example.learning_app.entity.Question;
import com.example.learning_app.repository.CategoryRepository;
import com.example.learning_app.repository.ChoiceRepository;
import com.example.learning_app.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/questions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminQuestionController {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final ChoiceRepository choiceRepository;

    // 一覧
    @GetMapping
    public String index(Model model) {
        model.addAttribute("questions", questionRepository.findAll());
        return "admin/questions/index";
    }

    // 新規作成フォーム
    @GetMapping("/new")
    public String newQuestion(Model model) {
        Question question = new Question();

        // 6択初期化
        List<Choice> choices = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Choice c = new Choice();
            c.setQuestion(question);
            choices.add(c);
        }
        question.setChoices(choices);

        model.addAttribute("question", question);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/questions/form";
    }

    // 登録処理
    @PostMapping
    public String create(@ModelAttribute Question question,
                         @RequestParam int correctChoiceIndex) {

        if (question.getChoices() != null) {
            // 空欄の選択肢を除外
            question.setChoices(
                question.getChoices().stream()
                    .filter(c -> c.getChoiceText() != null && !c.getChoiceText().trim().isEmpty())
                    .toList()
            );
        }

        question.setCreatedAt(LocalDateTime.now());
        Question savedQuestion = questionRepository.save(question);

        List<Choice> savedChoices = new ArrayList<>();
        for (Choice c : question.getChoices()) {
            c.setQuestion(savedQuestion);
            savedChoices.add(choiceRepository.save(c));
        }

        if (correctChoiceIndex >= 0 && correctChoiceIndex < savedChoices.size()) {
            savedQuestion.setCorrectChoiceId(savedChoices.get(correctChoiceIndex).getId());
            savedQuestion.setCorrectAnswer(savedChoices.get(correctChoiceIndex).getChoiceText());
        }

        questionRepository.save(savedQuestion);
        return "redirect:/admin/questions";
    }
    
//    @PostMapping
//    public String create(@ModelAttribute Question question,
//                         @RequestParam int correctChoiceIndex) {
//
//        // 関連付け
//        if (question.getChoices() != null) {
//            for (Choice c : question.getChoices()) {
//                c.setQuestion(question);
//            }
//        }
//
//        question.setCreatedAt(LocalDateTime.now());
//        Question savedQuestion = questionRepository.save(question);
//
//        // 選択肢保存
//        List<Choice> savedChoices = new ArrayList<>();
//        for (Choice c : question.getChoices()) {
//            c.setQuestion(savedQuestion);
//            savedChoices.add(choiceRepository.save(c));
//        }
//
//        // 正答設定
//        if (correctChoiceIndex >= 0 && correctChoiceIndex < savedChoices.size()) {
//            savedQuestion.setCorrectChoiceId(savedChoices.get(correctChoiceIndex).getId());
//            savedQuestion.setCorrectAnswer(savedChoices.get(correctChoiceIndex).getChoiceText());
//        }
//        questionRepository.save(savedQuestion);
//
//        return "redirect:/admin/questions";
//    }

    // 編集フォーム
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Question question = questionRepository.findById(id).orElseThrow();
        List<Choice> choices = choiceRepository.findByQuestionId(id);

        // 正答が削除されている場合は警告のみ（nullにはしない）
        boolean correctExists = question.getCorrectChoiceId() != null &&
                choices.stream().anyMatch(c -> c.getId().equals(question.getCorrectChoiceId()));

        if (!correctExists && question.getCorrectChoiceId() != null) {
            // 正答が削除されているが、保持はしておく（nullにはしない）
            // 画面では選択肢未選択として扱う
            model.addAttribute("correctMissing", true);
        }

        for (Choice c : choices) {
            c.setQuestion(question);
        }
        question.setChoices(choices);

        model.addAttribute("question", question);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/questions/form";
    }

 // 更新処理
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute Question question,
                         @RequestParam(required = false) Integer correctChoiceIndex) {

        Question existing = questionRepository.findById(id).orElseThrow();

        existing.setQuestionText(question.getQuestionText());
        existing.setCategory(question.getCategory());
        existing.setExplanation(question.getExplanation());

        // --- 旧選択肢を取得
        List<Choice> oldChoices = choiceRepository.findByQuestionId(id);

        // --- 🔸正答が削除対象に含まれるなら解除（null安全版）
        if (existing.getCorrectChoiceId() != null) {
            boolean isCorrectBeingDeleted = oldChoices.stream()
                .filter(c -> c.getId() != null) // ←★ nullチェック追加
                .anyMatch(c ->
                    c.getId().equals(existing.getCorrectChoiceId()) &&
                    (question.getChoices() == null ||
                     question.getChoices().stream().noneMatch(
                         nc -> nc.getId() != null && nc.getId().equals(c.getId())
                     ))
                );

            if (isCorrectBeingDeleted) {
                existing.setCorrectChoiceId(null);
                existing.setCorrectAnswer(null);
                questionRepository.save(existing); // 一旦保存
            }
        }

        // --- user_answersで使われていない選択肢だけ削除
        for (Choice oldChoice : oldChoices) {
            boolean isUsed = choiceRepository.isChoiceUsedByUserAnswers(oldChoice.getId());
            if (!isUsed) {
                choiceRepository.delete(oldChoice);
            }
        }

        // --- 新しい選択肢を登録（空欄を除外）
        List<Choice> savedChoices = new ArrayList<>();
        if (question.getChoices() != null) {
            for (Choice c : question.getChoices()) {
                if (c.getChoiceText() != null && !c.getChoiceText().trim().isEmpty()) {
                    c.setQuestion(existing);
                    savedChoices.add(choiceRepository.save(c));
                }
            }
        }

        // --- 正答設定
        if (correctChoiceIndex != null &&
            correctChoiceIndex >= 0 && correctChoiceIndex < savedChoices.size()) {

            Choice correct = savedChoices.get(correctChoiceIndex);
            existing.setCorrectChoiceId(correct.getId());
            existing.setCorrectAnswer(correct.getChoiceText());

        } else if (existing.getCorrectChoiceId() == null) {
            existing.setCorrectAnswer(null);
        }

        questionRepository.save(existing);
        return "redirect:/admin/questions";
    }
    
//    @PostMapping("/{id}")
//    public String update(@PathVariable Long id,
//                         @ModelAttribute Question question,
//                         @RequestParam(required = false) Integer correctChoiceIndex) {
//
//        Question existing = questionRepository.findById(id).orElseThrow();
//
//        existing.setQuestionText(question.getQuestionText());
//        existing.setCategory(question.getCategory());
//        existing.setExplanation(question.getExplanation());
//
//        // --- 旧選択肢を取得
//        List<Choice> oldChoices = choiceRepository.findByQuestionId(id);
//
//        // --- 🔸正答が削除対象に含まれるなら解除
//        if (existing.getCorrectChoiceId() != null) {
//            boolean isCorrectBeingDeleted = oldChoices.stream()
//                .anyMatch(c -> c.getId().equals(existing.getCorrectChoiceId())
//                            && (question.getChoices() == null ||
//                                question.getChoices().stream().noneMatch(nc -> c.getId() != null && nc.getId().equals(c.getId()))));
//
//            if (isCorrectBeingDeleted) {
//                existing.setCorrectChoiceId(null);
//                existing.setCorrectAnswer(null);
//                questionRepository.save(existing); // 一旦保存
//            }
//        }
//
//        // --- user_answersで使われていない選択肢だけ削除
//        for (Choice oldChoice : oldChoices) {
//            boolean isUsed = choiceRepository.isChoiceUsedByUserAnswers(oldChoice.getId());
//            if (!isUsed) {
//                choiceRepository.delete(oldChoice);
//            }
//        }
//
//        // --- 新しい選択肢を登録（空欄を除外）
//        List<Choice> savedChoices = new ArrayList<>();
//        if (question.getChoices() != null) {
//            for (Choice c : question.getChoices()) {
//                if (c.getChoiceText() != null && !c.getChoiceText().trim().isEmpty()) {
//                    c.setQuestion(existing);
//                    savedChoices.add(choiceRepository.save(c));
//                }
//            }
//        }
//
//        // --- 正答設定
//        if (correctChoiceIndex != null &&
//            correctChoiceIndex >= 0 && correctChoiceIndex < savedChoices.size()) {
//
//            Choice correct = savedChoices.get(correctChoiceIndex);
//            existing.setCorrectChoiceId(correct.getId());
//            existing.setCorrectAnswer(correct.getChoiceText());
//
//        } else if (existing.getCorrectChoiceId() == null) {
//            existing.setCorrectAnswer(null);
//        }
//
//        questionRepository.save(existing);
//        return "redirect:/admin/questions";
//    }
//    @PostMapping("/{id}")
//    public String update(@PathVariable Long id,
//                         @ModelAttribute Question question,
//                         @RequestParam(required = false) Integer correctChoiceIndex) {
//
//        Question existing = questionRepository.findById(id).orElseThrow();
//
//        existing.setQuestionText(question.getQuestionText());
//        existing.setCategory(question.getCategory());
//        existing.setExplanation(question.getExplanation());
//
//        // --- 🧩 旧選択肢を取得
//        List<Choice> oldChoices = choiceRepository.findByQuestionId(id);
//
//        // --- 🧩 旧選択肢を user_answers が参照しているか確認して安全に削除
//        List<Choice> deletableChoices = new ArrayList<>();
//        for (Choice oldChoice : oldChoices) {
//            // 外部キー制約を確認するため、user_answersを参照していないものだけ削除対象にする
//            boolean isUsed = choiceRepository.isChoiceUsedByUserAnswers(oldChoice.getId());
//            if (!isUsed) {
//                deletableChoices.add(oldChoice);
//            }
//        }
//
//        // --- 🧩 参照されていない選択肢を削除
//        for (Choice deletable : deletableChoices) {
//            choiceRepository.delete(deletable);
//        }
//
//        // --- 🧩 新しい選択肢を登録（すでに存在するchoice_idがある場合は上書き）
//        List<Choice> savedChoices = new ArrayList<>();
//        if (question.getChoices() != null) {
//            for (Choice c : question.getChoices()) {
//                c.setQuestion(existing);
//                Choice saved = choiceRepository.save(c);
//                savedChoices.add(saved);
//            }
//        }
//
//        // --- 🧩 正答設定
//        if (correctChoiceIndex != null &&
//            correctChoiceIndex >= 0 && correctChoiceIndex < savedChoices.size()) {
//
//            Choice correct = savedChoices.get(correctChoiceIndex);
//            existing.setCorrectChoiceId(correct.getId());
//            existing.setCorrectAnswer(correct.getChoiceText());
//
//        } else if (existing.getCorrectChoiceId() != null) {
//            // 元の正答がまだDB上に存在していれば保持
//            Optional<Choice> oldCorrect = choiceRepository.findById(existing.getCorrectChoiceId());
//            if (oldCorrect.isPresent()) {
//                existing.setCorrectChoiceId(oldCorrect.get().getId());
//                existing.setCorrectAnswer(oldCorrect.get().getChoiceText());
//            } else {
//                // 存在しなければ安全にnull化
//                existing.setCorrectChoiceId(null);
//                existing.setCorrectAnswer(null);
//            }
//        }
//
//        questionRepository.save(existing);
//        return "redirect:/admin/questions";
//    }
    
    
    // 更新処理
//    @PostMapping("/{id}")
//    public String update(@PathVariable Long id,
//                         @ModelAttribute Question question,
//                         @RequestParam(required = false) Integer correctChoiceIndex) {
//
//        Question existing = questionRepository.findById(id).orElseThrow();
//
//        existing.setQuestionText(question.getQuestionText());
//        existing.setCategory(question.getCategory());
//        existing.setExplanation(question.getExplanation());
//
//        // 旧選択肢削除（外部キー解除済み）
//        choiceRepository.deleteByQuestionId(id);
//
//        // 新選択肢を登録
//        List<Choice> savedChoices = new ArrayList<>();
//        if (question.getChoices() != null) {
//            for (Choice c : question.getChoices()) {
//                c.setQuestion(existing);
//                savedChoices.add(choiceRepository.save(c));
//            }
//        }
//
//        // 正答設定（削除された選択肢に該当する場合は保持）
//        if (correctChoiceIndex != null &&
//            correctChoiceIndex >= 0 && correctChoiceIndex < savedChoices.size()) {
//
//            Choice correct = savedChoices.get(correctChoiceIndex);
//            existing.setCorrectChoiceId(correct.getId());
//            existing.setCorrectAnswer(correct.getChoiceText());
//        } else if (existing.getCorrectChoiceId() != null) {
//            // 元の正答がまだDB上に存在していれば保持
//            Optional<Choice> oldCorrect = choiceRepository.findById(existing.getCorrectChoiceId());
//            if (oldCorrect.isPresent()) {
//                existing.setCorrectChoiceId(oldCorrect.get().getId());
//                existing.setCorrectAnswer(oldCorrect.get().getChoiceText());
//            } else {
//                // 存在しなければ安全にnull化
//                existing.setCorrectChoiceId(null);
//                existing.setCorrectAnswer(null);
//            }
//        }
//
//        questionRepository.save(existing);
//        return "redirect:/admin/questions";
//    }

    // 削除
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        Question existing = questionRepository.findById(id).orElseThrow();
        existing.setCorrectChoiceId(null);
        existing.setCorrectAnswer(null);
        questionRepository.save(existing);

        choiceRepository.deleteByQuestionId(id);
        questionRepository.deleteById(id);

        return "redirect:/admin/questions";
    }
}