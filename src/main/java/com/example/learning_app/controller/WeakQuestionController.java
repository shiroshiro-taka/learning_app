package com.example.learning_app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.learning_app.entity.Question;
import com.example.learning_app.entity.UserAnswer;
import com.example.learning_app.entity.Users;
import com.example.learning_app.repository.ChoiceRepository;
import com.example.learning_app.repository.QuestionRepository;
import com.example.learning_app.repository.UserAnswerRepository;
import com.example.learning_app.repository.UsersRepository;

@Controller
@RequestMapping("/play")
public class WeakQuestionController {

    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;
    private final UsersRepository usersRepository;
    private final ChoiceRepository choiceRepository;

    public WeakQuestionController(UserAnswerRepository userAnswerRepository,
                                  QuestionRepository questionRepository,
                                  UsersRepository usersRepository,
                                  ChoiceRepository choiceRepository) {
        this.userAnswerRepository = userAnswerRepository;
        this.questionRepository = questionRepository;
        this.usersRepository = usersRepository;
        this.choiceRepository = choiceRepository;
    }

    /** 苦手問題一覧画面（絞り込み機能なし） */
    @GetMapping("/weak")
    public String weakQuestions(@AuthenticationPrincipal UserDetails userDetails,
                                Model model) {

        // ログインユーザー取得
        Users user = usersRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("ユーザーが存在しません"));

        // ユーザー回答取得
        List<UserAnswer> answers = userAnswerRepository.findByUser(user);

        // 各問題の回答数・正答数集計
        Map<Question, long[]> stats = new HashMap<>();
        for (UserAnswer ua : answers) {
            stats.putIfAbsent(ua.getQuestion(), new long[]{0, 0});
            stats.get(ua.getQuestion())[0]++; // 回答数
            if (ua.isCorrect()) stats.get(ua.getQuestion())[1]++; // 正答数
        }

        // 正答率60%以下の問題を抽出 & 正答率順ソート
        List<Map<String, Object>> weakQuestions = stats.entrySet().stream()
                .filter(e -> {
                    long answered = e.getValue()[0];
                    long correct = e.getValue()[1];
                    double rate = (double) correct / answered;
                    return rate <= 0.6;
                })
                .sorted((e1, e2) -> {
                    double r1 = (double) e1.getValue()[1] / e1.getValue()[0];
                    double r2 = (double) e2.getValue()[1] / e2.getValue()[0];
                    return Double.compare(r1, r2);
                })
                .map(e -> {
                    Question q = e.getKey();
                    long[] counts = e.getValue();
                    Map<String, Object> map = new HashMap<>();
                    map.put("question", q);
                    map.put("answered", counts[0]);
                    map.put("correct", counts[1]);
                    map.put("rate", counts[0] > 0 ? (double) counts[1] / counts[0] : 0.0);
                    return map;
                })
                .toList();

        model.addAttribute("weakQuestions", weakQuestions);
        return "play/weak";
    }

    /** 再挑戦ボタン押下 → 該当問題出題 */
    @GetMapping("/weak/{id}")
    public String retryWeakQuestion(@PathVariable Long id, Model model) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("問題が存在しません"));

        // 選択肢を取得
        List<?> choices = choiceRepository.findByQuestionId(q.getId());

        model.addAttribute("question", q);
        model.addAttribute("choices", choices);
        model.addAttribute("categoryId", q.getCategory().getId());
        model.addAttribute("currentIndex", 0);
        model.addAttribute("isRandom", false);
        return "play/question";
    }
}