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
import com.example.learning_app.entity.UserAnswer;
import com.example.learning_app.repository.CategoryRepository;
import com.example.learning_app.repository.ChoiceRepository;
import com.example.learning_app.repository.QuestionRepository;
import com.example.learning_app.repository.ScoreRepository;
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
    private final UserAnswerRepository userAnswerRepository;
    private final ScoreRepository scoreRepository;

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
    @Transactional
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

    // 更新：過去の回答を削除し、スコアを調整した上でデータを書き換える
    @PostMapping("/{id}")
    @Transactional
    public String update(@PathVariable Long id, @ModelAttribute Question question, @RequestParam(required = false) Integer correctChoiceIndex) {
        Question existing = questionRepository.findById(id).orElseThrow();
        Long categoryId = existing.getCategory().getId();

        // 1. 回答履歴を消す前に、ユーザーごとの累計スコアを減算
        adjustUserScoresBeforeDelete(id, categoryId);

        // 2. 外部キー制約を回避するため、まず正解IDの参照を解除
        existing.setCorrectChoiceId(null);
        existing.setCorrectAnswer(null);
        questionRepository.saveAndFlush(existing);

        // 3. 回答履歴と古い選択肢を一掃
        userAnswerRepository.deleteByQuestionId(id);
        choiceRepository.deleteByQuestionId(id);

        // 4. 基本情報の更新
        existing.setQuestionText(question.getQuestionText());
        existing.setCategory(question.getCategory());
        existing.setExplanation(question.getExplanation());

        // 5. 新しい選択肢の登録
        List<Choice> savedChoices = new ArrayList<>();
        if (question.getChoices() != null) {
            for (Choice c : question.getChoices()) {
                if (c.getChoiceText() != null && !c.getChoiceText().trim().isEmpty()) {
                    c.setQuestion(existing);
                    c.setId(null); // 明示的に新規登録扱いにする
                    savedChoices.add(choiceRepository.save(c));
                }
            }
        }

        // 6. 新しい正解IDのセット
        if (correctChoiceIndex != null && correctChoiceIndex >= 0 && correctChoiceIndex < savedChoices.size()) {
            Choice correct = savedChoices.get(correctChoiceIndex);
            existing.setCorrectChoiceId(correct.getId());
            existing.setCorrectAnswer(correct.getChoiceText());
        }

        questionRepository.save(existing);
        return "redirect:/admin/questions";
    }

    // 削除：関連データを全て消去する
    @PostMapping("/{id}/delete")
    @Transactional
    public String delete(@PathVariable Long id) {
        Question existing = questionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Question not found"));
        Long categoryId = existing.getCategory().getId();

        // 1. スコアの調整
        adjustUserScoresBeforeDelete(id, categoryId);

        // 2. 参照の解除
        existing.setCorrectChoiceId(null);
        existing.setCorrectAnswer(null);
        questionRepository.saveAndFlush(existing);

        // 3. 物理削除（回答 -> 選択肢 -> 問題）
        userAnswerRepository.deleteByQuestionId(id);
        choiceRepository.deleteByQuestionId(id);
        questionRepository.deleteById(id);

        return "redirect:/admin/questions";
    }

    /**
     * 指定した問題に対するユーザーの回答状況を確認し、
     * scoresテーブルのカウントをマイナスする共通処理
     */
    private void adjustUserScoresBeforeDelete(Long questionId, Long categoryId) {
        Question q = new Question();
        q.setId(questionId);
        // userAnswerRepository.findByQuestion を使用して該当する回答を取得
        List<UserAnswer> answers = userAnswerRepository.findByQuestion(q);

        for (UserAnswer answer : answers) {
            scoreRepository.findByUserIdAndCategoryId(answer.getUser().getId(), categoryId)
                .ifPresent(score -> {
                    // UserAnswerエンティティに定義されているヘルパーメソッド isCorrect() を使用
                    if (answer.isCorrect()) {
                        score.setCorrectCount(Math.max(0, score.getCorrectCount() - 1));
                    } else {
                        score.setWrongCount(Math.max(0, score.getWrongCount() - 1));
                    }
                    // 必要に応じて updated_at をセット
                    score.setUpdatedAt(LocalDateTime.now());
                    scoreRepository.save(score);
                });
        }
    }
}