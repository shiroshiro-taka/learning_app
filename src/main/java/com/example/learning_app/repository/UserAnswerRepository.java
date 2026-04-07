package com.example.learning_app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.learning_app.entity.Question;
import com.example.learning_app.entity.UserAnswer;
import com.example.learning_app.entity.Users;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    // ... 既存のメソッド ...
    List<UserAnswer> findByUser(Users user);
    List<UserAnswer> findByUser_Id(Long userId);
    List<UserAnswer> findByQuestion(Question question);
    UserAnswer findByUserAndQuestion(Users user, Question question);
    int countByUserMockExam_IdAndCorrectTrue(Long userMockExamId);
    Optional<UserAnswer> findByUserMockExam_IdAndQuestion_Id(Long userMockExamId, Long questionId);
    List<UserAnswer> findByUserMockExam_Id(Long userMockExamId);

    @Query("SELECT new map(CAST(u.answeredAt AS date) as date, COUNT(u) as count, " +
            "SUM(CASE WHEN u.correct = true THEN 1 ELSE 0 END) * 100.0 / COUNT(u) as accuracy) " +
            "FROM UserAnswer u " +
            "WHERE u.user.id = :userId AND u.answeredAt BETWEEN :start AND :end " +
            "GROUP BY CAST(u.answeredAt AS date)")
     List<Map<String, Object>> findDailyStats(@Param("userId") Long userId, 
                                              @Param("start") LocalDateTime start, 
                                              @Param("end") LocalDateTime end);
    
    
    // --- ★ 追加：外部キー制約エラーを回避するための削除メソッド ---
    @Modifying
    @Transactional
    @Query("DELETE FROM UserAnswer ua WHERE ua.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);
    void deleteByUserId(Long userId);
}