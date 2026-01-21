package com.example.learning_app.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
import com.example.learning_app.repository.UserAnswerRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/questions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminQuestionController {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final ChoiceRepository choiceRepository;
    private final UserAnswerRepository userAnswerRepository; // 追加

    // 一覧
    @GetMapping
    public String index(Model model) {
        model.addAttribute("questions", questionRepository.findAll());
        return "admin/questions/index";
    }

    // 新規作成
    @GetMapping("/new")
    public String newQuestion(Model model) {
        Question question = new Question();
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

    // 登録
    @PostMapping
    public String create(@ModelAttribute Question question, @RequestParam int correctChoiceIndex) {
        if (question.getChoices() != null) {
            List<Choice> filteredChoices = new ArrayList<>(
                question.getChoices().stream()
                    .filter(c -> c.getChoiceText() != null && !c.getChoiceText().trim().isEmpty())
                    .toList()
            );
            for (Choice c : filteredChoices) {
                c.setQuestion(question);
            }
            question.setChoices(filteredChoices);
        }
        question.setCreatedAt(LocalDateTime.now());
        Question savedQuestion = questionRepository.save(question);

        List<Choice> savedChoices = savedQuestion.getChoices();
        if (correctChoiceIndex >= 0 && correctChoiceIndex < savedChoices.size()) {
            Choice correct = savedChoices.get(correctChoiceIndex);
            savedQuestion.setCorrectChoiceId(correct.getId());
            savedQuestion.setCorrectAnswer(correct.getChoiceText());
            questionRepository.save(savedQuestion);
        }
        return "redirect:/admin/questions";
    }

    // 編集
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Question question = questionRepository.findById(id).orElseThrow();
        List<Choice> choices = choiceRepository.findByQuestionId(id);
        for (Choice c : choices) { c.setQuestion(question); }
        question.setChoices(choices);
        model.addAttribute("question", question);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/questions/form";
    }

    // 更新
    @PostMapping("/{id}")
    @Transactional
    public String update(@PathVariable Long id, @ModelAttribute Question question, @RequestParam(required = false) Integer correctChoiceIndex) {
        Question existing = questionRepository.findById(id).orElseThrow();
        existing.setQuestionText(question.getQuestionText());
        existing.setCategory(question.getCategory());
        existing.setExplanation(question.getExplanation());

        // 使用されていない古い選択肢を整理（履歴があるものは残す等、既存ロジックを維持）
        List<Choice> oldChoices = choiceRepository.findByQuestionId(id);
        for (Choice oldChoice : oldChoices) {
            if (!choiceRepository.isChoiceUsedByUserAnswers(oldChoice.getId())) {
                choiceRepository.delete(oldChoice);
            }
        }

        List<Choice> savedChoices = new ArrayList<>();
        if (question.getChoices() != null) {
            for (Choice c : question.getChoices()) {
                if (c.getChoiceText() != null && !c.getChoiceText().trim().isEmpty()) {
                    c.setQuestion(existing);
                    savedChoices.add(choiceRepository.save(c));
                }
            }
        }

        if (correctChoiceIndex != null && correctChoiceIndex >= 0 && correctChoiceIndex < savedChoices.size()) {
            Choice correct = savedChoices.get(correctChoiceIndex);
            existing.setCorrectChoiceId(correct.getId());
            existing.setCorrectAnswer(correct.getChoiceText());
        }
        questionRepository.save(existing);
        return "redirect:/admin/questions";
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public String delete(@PathVariable Long id) {
        // 1. まずQuestionを読み込む
        Question existing = questionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Question not found"));

        // 2. Questionが持っている「正解選択肢ID」の参照を null にして保存
        // これをしないと、choices を消すときに「まだQuestionの正解IDとして使われてる」と怒られます
        existing.setCorrectChoiceId(null);
        existing.setCorrectAnswer(null);
        questionRepository.saveAndFlush(existing); // Flushして即座にDBへ反映

        // 3. ユーザーの解答履歴を削除（ここが一番外側の制約）
        userAnswerRepository.deleteByQuestionId(id);

        // 4. 選択肢を削除
        choiceRepository.deleteByQuestionId(id);

        // 5. 最後に問題本体を削除
        questionRepository.deleteById(id);

        return "redirect:/admin/questions";
    }
}