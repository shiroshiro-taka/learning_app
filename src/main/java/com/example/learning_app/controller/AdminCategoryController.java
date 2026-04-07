package com.example.learning_app.controller;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.learning_app.entity.Category;
import com.example.learning_app.entity.Question;
import com.example.learning_app.entity.UserAnswer;
import com.example.learning_app.repository.CategoryRepository;
import com.example.learning_app.repository.ChoiceRepository;
import com.example.learning_app.repository.QuestionRepository;
import com.example.learning_app.repository.ScoreRepository;
import com.example.learning_app.repository.UserAnswerRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ScoreRepository scoreRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/categories/index";
    }

    @GetMapping("/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Category category, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/form";
        }
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        model.addAttribute("category", category);
        return "admin/categories/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute Category category, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/form";
        }
        category.setId(id);
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    /**
     * カテゴリを削除する。
     * 紐付く問題、回答履歴、選択肢、スコアをすべてクリアしてから削除を実行する。
     */
    @PostMapping("/{id}/delete")
    @Transactional
    public String delete(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        // 1. このカテゴリに紐付く問題をすべて取得
        List<Question> questions = questionRepository.findByCategory(category);

        for (Question q : questions) {
            // 1-1. スコアの調整（その問題に回答したユーザーのカウントを減らす）
            adjustUserScoresBeforeDelete(q.getId(), id);

            // 1-2. 問題の正解ID参照を解除（循環参照回避）
            q.setCorrectChoiceId(null);
            q.setCorrectAnswer(null);
            questionRepository.saveAndFlush(q);

            // 1-3. 回答履歴を削除
            userAnswerRepository.deleteByQuestionId(q.getId());

            // 1-4. 選択肢を削除
            choiceRepository.deleteByQuestionId(q.getId());

            // 1-5. 問題を削除
            questionRepository.delete(q);
        }

        // 2. このカテゴリに関連する全ユーザーのスコアレコード自体を削除（任意）
        // スコアレコードを残すと「0問中0点」のようなデータが残るため、カテゴリごと消すのが一般的です。
        // scoreRepository.deleteByCategory(category); 

        // 3. 最後にカテゴリ本体を削除
        categoryRepository.delete(category);

        return "redirect:/admin/categories";
    }

    /**
     * スコア調整用ヘルパー（AdminQuestionControllerのものと同様）
     */
    private void adjustUserScoresBeforeDelete(Long questionId, Long categoryId) {
        Question q = new Question();
        q.setId(questionId);
        List<UserAnswer> answers = userAnswerRepository.findByQuestion(q);

        for (UserAnswer answer : answers) {
            scoreRepository.findByUserIdAndCategoryId(answer.getUser().getId(), categoryId)
                .ifPresent(score -> {
                    if (answer.isCorrect()) {
                        score.setCorrectCount(Math.max(0, score.getCorrectCount() - 1));
                    } else {
                        score.setWrongCount(Math.max(0, score.getWrongCount() - 1));
                    }
                    score.setUpdatedAt(LocalDateTime.now());
                    scoreRepository.save(score);
                });
        }
    }
}