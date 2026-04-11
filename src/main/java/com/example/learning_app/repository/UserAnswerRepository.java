package com.example.learning_app.repository;

import java.time.LocalDateTime;
import java.util.List;
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

    List<UserAnswer> findByUser(Users user);
    List<UserAnswer> findByUser_Id(Long userId);
    List<UserAnswer> findByQuestion(Question question);
    UserAnswer findByUserAndQuestion(Users user, Question question);
    int countByUserMockExam_IdAndCorrectTrue(Long userMockExamId);
    Optional<UserAnswer> findByUserMockExam_IdAndQuestion_Id(Long userMockExamId, Long questionId);
    List<UserAnswer> findByUserMockExam_Id(Long userMockExamId);

    /**
     * 指定された期間内（start 〜 end）の総回答数を取得
     * DB側の日付関数を使わず、Javaから渡された LocalDateTime を直接比較します。
     */
    @Query("SELECT COUNT(u) FROM UserAnswer u " +
           "WHERE u.user.id = :userId AND u.answeredAt BETWEEN :start AND :end")
    long countByUserIdAndAnsweredAtBetween(@Param("userId") Long userId, 
                                           @Param("start") LocalDateTime start, 
                                           @Param("end") LocalDateTime end);

    /**
     * 指定された期間内（start 〜 end）の正答率（%）を取得
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN u.correct = true THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(u), 0), 0.0) " +
           "FROM UserAnswer u " +
           "WHERE u.user.id = :userId AND u.answeredAt BETWEEN :start AND :end")
    Double calculateAccuracyByUserIdAndAnsweredAtBetween(@Param("userId") Long userId, 
                                                         @Param("start") LocalDateTime start, 
                                                         @Param("end") LocalDateTime end);

    // --- 既存の削除メソッド ---
    @Modifying
    @Query("DELETE FROM UserAnswer ua WHERE ua.userMockExam.mockExam.id = :mockExamId")
    void deleteByMockExamId(@Param("mockExamId") Long mockExamId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserAnswer ua WHERE ua.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);
    
    void deleteByUserId(Long userId);
}