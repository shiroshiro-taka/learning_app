package com.example.learning_app.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.learning_app.entity.Category;
import com.example.learning_app.entity.ExamResult; // 👈 追加
import com.example.learning_app.entity.UserAnswer;
import com.example.learning_app.repository.CategoryRepository;
import com.example.learning_app.repository.ExamResultRepository;
import com.example.learning_app.repository.QuestionRepository;
import com.example.learning_app.repository.UserAnswerRepository;
import com.example.learning_app.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ScoreController {

    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final ExamResultRepository examResultRepository;

    @GetMapping("/scores/result")
    public String showProgress(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        Long userId = userDetails.getId();
        List<UserAnswer> userAnswers = userAnswerRepository.findByUser_Id(userId); // ← userIdで取得
        

        // --- 既存の進捗・正答率の算出処理 (省略せず残します) ---
        long totalQuestions = questionRepository.count();
        long totalAnswered = userAnswers.stream()
                .map(ua -> ua.getQuestion().getId())
                .distinct()
                .count();
        double progressRate = totalQuestions == 0 ? 0 : (double) totalAnswered / totalQuestions * 100;

        long totalCorrect = userAnswers.stream()
                .filter(UserAnswer::isCorrect)
                .map(ua -> ua.getQuestion().getId())
                .distinct()
                .count();
        double correctRate = totalAnswered == 0 ? 0 : (double) totalCorrect / totalAnswered * 100;

        Map<String, Map<String, Object>> categoryStats = new LinkedHashMap<>();
        List<Category> categories = categoryRepository.findAll();

        for (Category category : categories) {
            long categoryTotal = questionRepository.countByCategory(category);

            List<UserAnswer> categoryAnswers = userAnswers.stream()
                    .filter(a -> a.getQuestion().getCategory().getId().equals(category.getId()))
                    .collect(Collectors.toList());
            
            long categoryAnswered = categoryAnswers.stream()
                    .map(a -> a.getQuestion().getId())
                    .distinct()
                    .count();

            long categoryCorrect = categoryAnswers.stream()
                    .filter(UserAnswer::isCorrect)
                    .map(a -> a.getQuestion().getId())
                    .distinct()
                    .count();

            double categoryProgressRate = categoryTotal == 0 ? 0 : (double) categoryAnswered / categoryTotal * 100;
            double categoryCorrectRate = categoryAnswered == 0 ? 0 : (double) categoryCorrect / categoryAnswered * 100;

            Map<String, Object> stats = new HashMap<>();
            stats.put("progressRate", (int) categoryProgressRate);
            stats.put("correctRate", (int) categoryCorrectRate);
            stats.put("answered", categoryAnswered);
            stats.put("correct", categoryCorrect);
            stats.put("total", categoryTotal);

            categoryStats.put(category.getName(), stats);
            
        }
        // --- 既存の進捗・正答率の算出処理 (ここまで) ---
        
        // 💡 模擬試験結果を取得し、表示形式に変換
        // ExamResultRepositoryには findByUser_IdOrderByFinishedAtDesc メソッドが定義されていると仮定
        List<ExamResult> rawExamResults = examResultRepository.findByUser_IdOrderByFinishedAtDesc(userId); 
        
        List<Map<String, Object>> examResultsDisplay = rawExamResults.stream()
            .map(result -> {
                long totalQuestionsCount = result.getTotalQuestions(); 
                long correctAnswersCount = result.getCorrectCount();   
                double correctRateExam = totalQuestionsCount == 0 ? 0.0 : (double) correctAnswersCount / totalQuestionsCount * 100;
                
                String passOrFail = correctRateExam >= 60.0 ? "合格" : "不合格"; // 合否判定（60%以上）

                Map<String, Object> displayData = new LinkedHashMap<>();
                
                // MockExamエンティティから情報を取得
                displayData.put("examId", result.getMockExam().getId());          // 試験番号 (MockExamのIDを仮定)
                displayData.put("examName", result.getMockExam().getExamName());     // 試験名 (MockExamのgetTitleを仮定)
                displayData.put("takenAt", result.getFinishedAt());               // 受験日 (finishedAt)
                displayData.put("totalQuestions", totalQuestionsCount);           // 問題数
                displayData.put("correctAnswers", correctAnswersCount);           // 正答数
                displayData.put("correctRate", (int) Math.round(correctRateExam)); // 正答率 (整数化)
                displayData.put("passOrFail", passOrFail);                        // 合否判定
                return displayData;
            })
            .collect(Collectors.toList());

        // --- Modelへの追加 ---
        model.addAttribute("progressRate", (int) progressRate);
        model.addAttribute("totalAnswered", totalAnswered);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("correctRate", (int) correctRate);
        model.addAttribute("totalCorrect", totalCorrect);
        model.addAttribute("categoryStats", categoryStats);
        
        // 👈 模擬試験結果リストをModelに追加
        model.addAttribute("examResults", examResultsDisplay); 

        return "scores/result";
    }
}