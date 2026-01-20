package com.example.learning_app.service;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.learning_app.entity.ExamResult;
import com.example.learning_app.entity.MockExam;
import com.example.learning_app.entity.UserMockExam;
import com.example.learning_app.entity.Users;
import com.example.learning_app.repository.ExamResultRepository;
import com.example.learning_app.repository.UserMockExamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserMockExamService {

    private final UserMockExamRepository userMockExamRepository; 
    private final UserService userService;
    private final UserAnswerService userAnswerService;
    private final MockExamService mockExamService; 
    private final ExamResultRepository examResultRepository;

    /**
     * 試験開始/再開処理。未終了の既存試験があればそれを返し、なければ新規作成する。
     * @param examId 模擬試験ID
     * @param userId ユーザーID
     * @return 既存または新規作成された UserMockExam
     */
    @Transactional
    public UserMockExam findOrCreateLatestUnfinishedExam(Long examId, Long userId) {
        
        // 1. 未完了の最新の試験を探す（finishedAt が null）
        Optional<UserMockExam> latestUnfinished = 
            userMockExamRepository.findByUserIdAndMockExam_IdAndFinishedAtIsNull(userId, examId);
        
        if (latestUnfinished.isPresent()) {
            return latestUnfinished.get();
        }
        
        // 2. なければ新規作成
        
        // ★修正1: Optional<Users> を返すことを前提とし、orElseThrow を標準的なラムダ式の形式に修正
        // ここでエラーが再発する場合、userService.findById(userId) の戻り値が Optional ではない
        Users user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません。ID: " + userId)); 
        
        // ★修正2: Optional<MockExam> を返すことを前提とし、orElseThrow を標準的なラムダ式の形式に修正
        // ここでエラーが再発する場合、mockExamService.findById(examId) の戻り値が Optional ではない
        MockExam exam = mockExamService.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("模擬試験が見つかりません。ID: " + examId));

        UserMockExam newUme = UserMockExam.builder()
            .user(user) 
            .mockExam(exam)
            .startedAt(LocalDateTime.now())
            .build();
            
        return userMockExamRepository.save(newUme);
    }
    
    /**
     * 元々存在していた startExam メソッドのロジックを findOrCreateLatestUnfinishedExam に統合。
     */
    @Transactional
    public UserMockExam startExam(MockExam exam, Users user) {
        return findOrCreateLatestUnfinishedExam(exam.getId(), user.getId());
    }

    /**
     * UserMockExam IDに基づいてエンティティを取得します。
     */
    @Transactional(readOnly = true)
    public Optional<UserMockExam> findById(Long userExamId) {
        return userMockExamRepository.findById(userExamId);
    }
    
    //-------------------------------------------------------------
    
    /**
     * ユーザーの最新の試験データ（完了/未完了問わず）を取得します。
     */
    @Transactional(readOnly = true)
    public Optional<UserMockExam> findLatestUserMockExam(Long examId, Long userId) {
        // Repository に findTopByMockExam_IdAndUser_IdOrderByStartedAtDesc があることを前提
        return userMockExamRepository.findTopByMockExam_IdAndUser_IdOrderByStartedAtDesc(examId, userId);
    }

    /**
     * 最新の未終了 UserMockExam (受験中インスタンス) を検索します。
     */
    @Transactional(readOnly = true)
    public Optional<UserMockExam> findLatestUnfinishedExam(Long examId, Long userId) {
        return userMockExamRepository.findByUserIdAndMockExam_IdAndFinishedAtIsNull(userId, examId);
    }


    /**
     * 試験終了処理。
     */
    @Transactional
    public UserMockExam finishExam(Long userExamId) {
        // 1. DBから UserMockExam を取得
        UserMockExam ume = userMockExamRepository.findById(userExamId)
                .orElseThrow(() -> new EntityNotFoundException("UserMockExam not found with id: " + userExamId)); 

        // 2. 終了日時を設定
        ume.setFinishedAt(LocalDateTime.now());
        
        // 3. 正答数を計算し、UserMockExamに設定 (UserAnswerService に依存)
        int correctCount = userAnswerService.calculateCorrectCount(userExamId);
        ume.setCorrectCount(correctCount); 
        
        // 4. UserMockExamを更新
        UserMockExam finishedUme = userMockExamRepository.save(ume);

        // 5. ★ ExamResultエンティティを作成し、結果を永続化する
        
        // 全問題数を取得
        // MockExamエンティティはLAZYですが、umeを通じてロードされているはずです
        int totalQuestions = finishedUme.getMockExam().getQuestions().size(); 
        int incorrectCount = totalQuestions - correctCount;

        // 正答率とスコアを計算
        int correctRate = 0;
        if (totalQuestions > 0) {
            correctRate = (int) Math.round(((double) correctCount / totalQuestions) * 100);
        }
        int score = correctRate; // 正答率をスコアとして格納

        ExamResult result = ExamResult.builder()
            .user(finishedUme.getUser())
            .mockExam(finishedUme.getMockExam())
            .correctCount(correctCount)
            .incorrectCount(incorrectCount)
            .totalQuestions(totalQuestions)
            .score(score) // 正答率 (%)
            .startedAt(finishedUme.getStartedAt()) 
            .finishedAt(finishedUme.getFinishedAt())
            .build();
            
        examResultRepository.save(result); // ★ 2. ExamResultの保存

        // 6. UserMockExamを返す
        return finishedUme;
    }
}