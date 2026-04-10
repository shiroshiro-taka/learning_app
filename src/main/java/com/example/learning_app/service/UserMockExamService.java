package com.example.learning_app.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    // 日本時間（Asia/Tokyo）を取得するための定数
    private static final ZoneId TOKYO_ZONE = ZoneId.of("Asia/Tokyo");

    @Transactional
    public UserMockExam findOrCreateLatestUnfinishedExam(Long examId, Long userId) {
        
        Optional<UserMockExam> latestUnfinished = 
            userMockExamRepository.findByUserIdAndMockExam_IdAndFinishedAtIsNull(userId, examId);
        
        if (latestUnfinished.isPresent()) {
            return latestUnfinished.get();
        }
        
        Users user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません。ID: " + userId)); 
        
        MockExam exam = mockExamService.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("模擬試験が見つかりません。ID: " + examId));

        UserMockExam newUme = UserMockExam.builder()
            .user(user) 
            .mockExam(exam)
            // ★修正: 明示的に日本時間を取得
            .startedAt(ZonedDateTime.now(TOKYO_ZONE).toLocalDateTime())
            .build();
            
        return userMockExamRepository.save(newUme);
    }
    
    @Transactional
    public UserMockExam finishExam(Long userExamId) {
        UserMockExam ume = userMockExamRepository.findById(userExamId)
                .orElseThrow(() -> new EntityNotFoundException("UserMockExam not found with id: " + userExamId)); 

        // ★修正: 明示的に日本時間を取得
        ume.setFinishedAt(ZonedDateTime.now(TOKYO_ZONE).toLocalDateTime());
        
        int correctCount = userAnswerService.calculateCorrectCount(userExamId);
        ume.setCorrectCount(correctCount); 
        
        UserMockExam finishedUme = userMockExamRepository.save(ume);

        int totalQuestions = finishedUme.getMockExam().getQuestions().size(); 
        int incorrectCount = totalQuestions - correctCount;

        int correctRate = 0;
        if (totalQuestions > 0) {
            correctRate = (int) Math.round(((double) correctCount / totalQuestions) * 100);
        }

        ExamResult result = ExamResult.builder()
            .user(finishedUme.getUser())
            .mockExam(finishedUme.getMockExam())
            .correctCount(correctCount)
            .incorrectCount(incorrectCount)
            .totalQuestions(totalQuestions)
            .score(correctRate)
            .startedAt(finishedUme.getStartedAt()) 
            .finishedAt(finishedUme.getFinishedAt())
            .build();
            
        examResultRepository.save(result);

        return finishedUme;
    }

    @Transactional(readOnly = true)
    public Optional<UserMockExam> findById(Long userExamId) {
        return userMockExamRepository.findById(userExamId);
    }
    
    @Transactional(readOnly = true)
    public Optional<UserMockExam> findLatestUserMockExam(Long examId, Long userId) {
        return userMockExamRepository.findTopByMockExam_IdAndUser_IdOrderByStartedAtDesc(examId, userId);
    }
}