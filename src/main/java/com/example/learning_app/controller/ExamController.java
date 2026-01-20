package com.example.learning_app.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.learning_app.dto.UserAnswerDto;
import com.example.learning_app.entity.MockExam;
import com.example.learning_app.entity.MockExamQuestion;
import com.example.learning_app.entity.Question;
import com.example.learning_app.entity.UserAnswer;
import com.example.learning_app.entity.UserMockExam;
import com.example.learning_app.entity.Users;
import com.example.learning_app.repository.MockExamRepository;
import com.example.learning_app.repository.UsersRepository;
import com.example.learning_app.service.MockExamService;
import com.example.learning_app.service.UserAnswerService;
import com.example.learning_app.service.UserMockExamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/exam")
@RequiredArgsConstructor
@Slf4j
public class ExamController {

    private final MockExamRepository mockExamRepository;
    private final UserMockExamService userMockExamService;
    private final UserAnswerService userAnswerService;
    private final UsersRepository usersRepository;
    private final MockExamService mockExamService;

    /**
     * 模擬試験一覧画面を表示するメソッド
     * URL: /exam/list
     */
    @GetMapping("/list")
    public String listExams(Model model) {
        List<MockExam> mockExams = mockExamService.findAll();
        
        // ★修正点: モデル属性のキーを "mockExams" から "exams" に変更
        model.addAttribute("exams", mockExams);
        
        return "exam/list"; 
    }

    /**
     * 試験開始画面を表示し、試験中の問題を再開できるようにする。
     * @param examId 対象の模擬試験ID
     * @param questionId URLパラメータで指定された問題ID (見直し画面からの戻り時などに使用)
     */
    @GetMapping("/start/{examId}")
    public String startExam(@PathVariable Long examId, 
                            @RequestParam(value = "questionId", required = false) Long questionId, 
                            Principal principal, 
                            Model model) {
        
        MockExam exam = mockExamRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("指定された模擬試験が見つかりません。ID: " + examId));
        
        Users user = usersRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("認証ユーザーが見つかりません。"));
        
        // 1. 最新のUserMockExamを取得（未完了のものがあればそれを使う）
        UserMockExam userExam = userMockExamService.findOrCreateLatestUnfinishedExam(examId, user.getId());
        
        List<MockExamQuestion> examQuestions = exam.getQuestions();
        
        // 2. 初期表示問題インデックスの決定ロジック
        int initialQuestionIndex = 0;
        
        if (questionId != null) {
            initialQuestionIndex = calculateQuestionIndex(examQuestions, questionId);
        } 
        else if (userExam.getLatestQuestionId() != null) {
            initialQuestionIndex = calculateQuestionIndex(examQuestions, userExam.getLatestQuestionId());
        }

        // 3. 既存の解答状況を取得し、DTOに変換
        List<UserAnswerDto> existingAnswers = createUserAnswerDtoListForStart(userExam, examQuestions);
        
        // 4. モデルに格納
        model.addAttribute("exam", exam);
        model.addAttribute("examQuestions", examQuestions); 
        model.addAttribute("userExamId", userExam.getId());
        model.addAttribute("initialQuestionIndex", initialQuestionIndex);
        model.addAttribute("existingAnswers", existingAnswers);
        
        return "exam/start";
    }

    /**
     * Ajaxリクエストによる解答の保存・更新
     */
    @PostMapping("/answer")
    @ResponseBody
    public String saveAnswer(@RequestParam Long userExamId,
                             @RequestParam Long questionId,
                             @RequestParam(required = false) Long choiceId,
                             @RequestParam(defaultValue = "false") boolean reviewFlag) {
        
        userAnswerService.saveOrUpdateAnswer(userExamId, questionId, choiceId, reviewFlag);
        
        return "{\"status\": \"success\"}"; 
    }
    
    /**
     * 試験終了処理と結果表示
     */
    @PostMapping("/finish")
    public String finishExam(@RequestParam Long userExamId, Model model) {
        
        UserMockExam finishedExam = userMockExamService.finishExam(userExamId);
        
        int correctCount = finishedExam.getCorrectCount();
        // finishedExam.getMockExam() と .getQuestions().size() が null でないことを前提とします
        int totalQuestions = finishedExam.getMockExam().getQuestions().size(); 
        
        // 1. 正答率を計算 (doubleで計算し、四捨五入して整数%にする)
        // 計算結果が nullPointerException にならないよう、分母が 0 でないか確認する処理を追加するとより安全です
        int correctRate = 0;
        if (totalQuestions > 0) {
            correctRate = (int) Math.round(((double) correctCount / totalQuestions) * 100);
        }
        
        // 2. 合格/不合格を判定 (60%以上で合格)
        String resultStatus = correctRate >= 60 ? "合格" : "不合格";

        model.addAttribute("result", finishedExam);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("totalQuestions", totalQuestions);
        
        // ★ 新しく追加・更新する属性
        model.addAttribute("correctRate", correctRate); // 正答率
        model.addAttribute("resultStatus", resultStatus); // 合否ステータス
        
        return "exam/result";
    }
    /**
     * 解答見直し画面を表示
     * @param examId 対象の模擬試験ID
     */
    @GetMapping("/review/{examId}")
    public String reviewExam(@PathVariable Long examId, Principal principal, Model model) {
        
        MockExam exam = mockExamRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("指定された模擬試験が見つかりません。ID: " + examId));
        
        Users user = usersRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("認証ユーザーが見つかりません。"));
        
        UserMockExam userExam = userMockExamService.findLatestUserMockExam(examId, user.getId())
                .orElseThrow(() -> new IllegalStateException("試験データが見つかりません。"));
        
        List<MockExamQuestion> allExamQuestions = exam.getQuestions();
        
        List<UserAnswerDto> dtoList = createUserAnswerDtoListForReview(userExam, allExamQuestions);

        model.addAttribute("initialQuestionId", userExam.getLatestQuestionId()); 
        model.addAttribute("exam", exam);
        model.addAttribute("userAnswerDtoList", dtoList);
        model.addAttribute("allExamQuestions", allExamQuestions); 
        
        return "exam/review";
    }


    // --- プライベートヘルパーメソッド ---
    
    private int calculateQuestionIndex(List<MockExamQuestion> examQuestions, Long targetQuestionId) {
        for (int i = 0; i < examQuestions.size(); i++) {
            if (examQuestions.get(i).getQuestion().getId().equals(targetQuestionId)) {
                return i;
            }
        }
        return 0;
    }


    private List<UserAnswerDto> createUserAnswerDtoListForStart(UserMockExam userMockExam, List<MockExamQuestion> allExamQuestions) {
        List<UserAnswer> userAnswersList = userAnswerService.findByUserExamId(userMockExam.getId());
        
        Map<Long, UserAnswer> existingAnswersMap = userAnswersList.stream()
            .collect(Collectors.toMap(
                answer -> answer.getQuestion().getId(), 
                answer -> answer
            ));
        
        List<UserAnswerDto> dtoList = new ArrayList<>();
        
        for (MockExamQuestion examQuestion : allExamQuestions) {
            Question question = examQuestion.getQuestion();
            UserAnswer existingAnswer = existingAnswersMap.get(question.getId());
            
            UserAnswerDto dto = new UserAnswerDto();
            dto.setQuestionId(question.getId());
            
            if (existingAnswer != null) {
                dto.setSelectedChoiceId(
                    existingAnswer.getChoice() != null ? existingAnswer.getChoice().getId() : null
                );
                dto.setReviewFlag(existingAnswer.isReviewFlag());
            } else {
                dto.setSelectedChoiceId(null);
                dto.setReviewFlag(false);
            }
            
            dto.updateAnswerStatus(); 
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }

    private List<UserAnswerDto> createUserAnswerDtoListForReview(UserMockExam userMockExam, List<MockExamQuestion> allExamQuestions) {
        List<UserAnswer> userAnswersList = userAnswerService.findByUserExamId(userMockExam.getId());
        
        Map<Long, UserAnswer> existingAnswersMap = userAnswersList.stream()
            .collect(Collectors.toMap(
                answer -> answer.getQuestion().getId(), 
                answer -> answer
            ));

        List<UserAnswerDto> dtoList = new ArrayList<>();
        
        int i = 0;
        for (MockExamQuestion examQuestion : allExamQuestions) {
            Question question = examQuestion.getQuestion();
            UserAnswer existingAnswer = existingAnswersMap.get(question.getId());
            
            UserAnswerDto dto = new UserAnswerDto();
            dto.setQuestionId(question.getId());
            dto.setQuestionNumber(i + 1);
            
            if (existingAnswer != null) {
                dto.setSelectedChoiceId(
                    existingAnswer.getChoice() != null ? existingAnswer.getChoice().getId() : null
                );
                dto.setReviewFlag(existingAnswer.isReviewFlag());
            } else {
                dto.setSelectedChoiceId(null);
                dto.setReviewFlag(false);
            }
            
            dto.updateAnswerStatus(); 
            
            dtoList.add(dto);
            i++;
        }
        
        return dtoList;
    }
}