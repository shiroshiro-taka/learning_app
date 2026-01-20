package com.example.learning_app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.learning_app.entity.Category;
import com.example.learning_app.entity.MockExam;
import com.example.learning_app.entity.Question;
import com.example.learning_app.repository.CategoryRepository;
import com.example.learning_app.repository.QuestionRepository;
import com.example.learning_app.service.MockExamService;
import com.example.learning_app.service.QuestionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/mock_exam")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminMockExamController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionService questionService;

    private final MockExamService mockExamService;
    private final QuestionRepository questionRepository;

    /** 一覧表示 */
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("exams", mockExamService.findAll());
        return "admin/mock_exam/list";
    }

    /** 新規登録フォーム */
    @GetMapping("/create")
    public String createForm(@RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) String examName,
                             @RequestParam(required = false) Integer durationMinutes,
                             @RequestParam(required = false) Integer questionCount,
                             @RequestParam(required = false) String description,
                             Model model) {

        List<Category> categories = categoryRepository.findAll();
        List<Question> questions = questionService.searchQuestions(categoryId);

        model.addAttribute("exam", new MockExam());
        model.addAttribute("categories", categories);
        model.addAttribute("questions", questions);

        model.addAttribute("categoryId", categoryId);
        model.addAttribute("examName", examName);
        model.addAttribute("durationMinutes", durationMinutes);
        model.addAttribute("questionCount", questionCount);
        model.addAttribute("description", description);

        return "admin/mock_exam/create";
    }

    /** 登録処理 */
    @PostMapping("/create")
    public String create(
            @RequestParam String examName,
            @RequestParam Integer durationMinutes,
            @RequestParam Integer questionCount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(value = "questionIds", required = false) List<Long> questionIds,
            Model model) {

        MockExam exam = new MockExam();
        exam.setExamName(examName);
        exam.setDurationMinutes(durationMinutes);
        exam.setQuestionCount(questionCount);
        exam.setDescription(description);

        if (examName == null || examName.isBlank() || durationMinutes == null || questionCount == null) {
            model.addAttribute("errorMessage", "試験名、試験時間、問題数は必須項目です。");
            model.addAttribute("examName", examName);
            model.addAttribute("durationMinutes", durationMinutes);
            model.addAttribute("questionCount", questionCount);
            model.addAttribute("description", description);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("selectedQuestionIds", questionIds);

            List<Category> categories = categoryRepository.findAll();
            List<Question> questions = questionService.searchQuestions(categoryId);
            model.addAttribute("categories", categories);
            model.addAttribute("questions", questions);

            return "admin/mock_exam/create";
        }

        mockExamService.createMockExam(exam, questionIds);
        return "redirect:/admin/mock_exam/list";
    }


    /** 削除処理 */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        mockExamService.deleteExam(id);
        return "redirect:/admin/mock_exam/list";
    }
}