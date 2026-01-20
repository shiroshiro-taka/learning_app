package com.example.learning_app.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import jakarta.transaction.Transactional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.learning_app.entity.Category;
import com.example.learning_app.entity.Choice;
import com.example.learning_app.entity.Question;
import com.example.learning_app.entity.Score;
import com.example.learning_app.entity.UserAnswer;
import com.example.learning_app.entity.Users;
import com.example.learning_app.repository.CategoryRepository;
import com.example.learning_app.repository.ChoiceRepository;
import com.example.learning_app.repository.QuestionRepository;
import com.example.learning_app.repository.ScoreRepository;
import com.example.learning_app.repository.UserAnswerRepository;
import com.example.learning_app.repository.UsersRepository;

@Controller
@RequestMapping("/play")
public class QuestionController {

    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final CategoryRepository categoryRepository;
    private final UsersRepository usersRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final ScoreRepository scoreRepository;

    public QuestionController(
            QuestionRepository questionRepository,
            ChoiceRepository choiceRepository,
            CategoryRepository categoryRepository,
            UsersRepository usersRepository,
            UserAnswerRepository userAnswerRepository,
            ScoreRepository scoreRepository
    ) {
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
        this.categoryRepository = categoryRepository;
        this.usersRepository = usersRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.scoreRepository = scoreRepository;
    }

    /** 分野選択画面 */
    @GetMapping("/select")
    public String selectCategory(Model model) {

    	 List<Category> categories = categoryRepository.findAll();

    	    // カテゴリごとの問題数を取得
    	    Map<Long, Long> questionCounts = new HashMap<>();
    	    for (Category cat : categories) {
    	        long count = questionRepository.countByCategory_Id(cat.getId());
    	        questionCounts.put(cat.getId(), count);
    	    }

    	    model.addAttribute("categories", categories);
    	    model.addAttribute("questionCounts", questionCounts);

    	return "play/select_category";
    }

    /** 分野別出題（ID昇順） */
    @GetMapping("/category/{id}")
    public String playByCategory(@PathVariable Long id, Model model) {
        List<Question> questions = questionRepository.findByCategory_IdOrderByIdAsc(id);
        if (questions.isEmpty()) {
            model.addAttribute("message", "このカテゴリには問題がありません。");
            return "play/select_category";
        }

        // 最初の問題（インデックス0）
        Question q = questions.get(0);
        model.addAttribute("question", q);
        model.addAttribute("choices", choiceRepository.findByQuestionId(q.getId()));
        model.addAttribute("categoryId", id);
        model.addAttribute("currentIndex", 0);
        model.addAttribute("total", questions.size());
        return "play/question";
    }

    /** ランダム出題 */
    @GetMapping("/random")
    public String playRandom(Model model) {
        List<Question> questions = questionRepository.findAll();
        if (questions.isEmpty()) {
            model.addAttribute("message", "まだ問題が登録されていません。");
            return "play/select_category";
        }

        Question q = questions.get(new Random().nextInt(questions.size()));
        model.addAttribute("question", q);
        model.addAttribute("choices", choiceRepository.findByQuestionId(q.getId()));
        model.addAttribute("categoryId", q.getCategory().getId());
        model.addAttribute("isRandom", true); // ランダム出題フラグ
        return "play/question";
    }

    /** 回答送信（分野別・ランダム両対応） */
    @PostMapping("/answer")
    @Transactional // ★重要：履歴保存とスコア更新を「セット」で扱う
    public String submitAnswer(
            @RequestParam Long questionId,
            @RequestParam Long choiceId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer currentIndex,
            @RequestParam(required = false, defaultValue = "false") boolean isRandom,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("問題が存在しません"));
        Choice c = choiceRepository.findById(choiceId)
                .orElseThrow(() -> new RuntimeException("選択肢が存在しません"));
        Users user = usersRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("ログインユーザーが存在しません"));

        boolean isCorrect = c.getChoiceText().equals(q.getCorrectAnswer());

        // 解答履歴を保存
        UserAnswer answer = new UserAnswer();
        answer.setUser(user);
        answer.setQuestion(q);
        answer.setChoice(c);
        answer.setCorrect(isCorrect);
        answer.setAnsweredAt(LocalDateTime.now());
        userAnswerRepository.save(answer);

        // スコアを更新
        Optional<Score> optScore = scoreRepository.findByUserIdAndCategoryId(user.getId(), q.getCategory().getId());
        Score score = optScore.orElse(new Score(user, q.getCategory(), 0, 0));

        if (isCorrect) {
            score.setCorrectCount(score.getCorrectCount() + 1);
        } else {
            score.setWrongCount(score.getWrongCount() + 1);
        }
        score.setUpdatedAt(LocalDateTime.now());
        scoreRepository.save(score);

        // 共通のモデル属性
        model.addAttribute("question", q);
        model.addAttribute("choice", c);
        model.addAttribute("isCorrect", isCorrect);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categoryName", q.getCategory().getName());
        model.addAttribute("isRandom", isRandom);

        // 分野別出題なら次の問題をチェック
        if (!isRandom && categoryId != null && currentIndex != null) {
            List<Question> questions = questionRepository.findByCategory_IdOrderByIdAsc(categoryId);
            int nextIndex = currentIndex + 1;
            boolean hasNext = nextIndex < questions.size();

            model.addAttribute("nextIndex", nextIndex);
            model.addAttribute("hasNext", hasNext);
        }

        return "play/result";
    }

    /** 分野別：次の問題へ */
    @GetMapping("/next")
    public String nextQuestion(
            @RequestParam Long categoryId,
            @RequestParam int index,
            Model model
    ) {
    	List<Question> questions = questionRepository.findByCategory_IdOrderByIdAsc(categoryId);

        if (index >= questions.size()) {
            // ★ finished.html の代わりに result.html に完了メッセージを表示
            Category cat = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("カテゴリが存在しません"));
            model.addAttribute("categoryName", cat.getName());
            model.addAttribute("hasNext", false);
            model.addAttribute("isRandom", false);
            model.addAttribute("isCorrect", true); // ダミー値（画面遷移のため）
            return "play/result";
    	
        }

        Question q = questions.get(index);
        model.addAttribute("question", q);
        model.addAttribute("choices", choiceRepository.findByQuestionId(q.getId()));
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("currentIndex", index);
        model.addAttribute("total", questions.size());
        return "play/question";
    }
}