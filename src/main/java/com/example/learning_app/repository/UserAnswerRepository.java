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

    List<UserAnswer> findByUser(Users user);
    List<UserAnswer> findByUser_Id(Long userId);
    List<UserAnswer> findByQuestion(Question question);
    UserAnswer findByUserAndQuestion(Users user, Question question);
    int countByUserMockExam_IdAndCorrectTrue(Long userMockExamId);
    Optional<UserAnswer> findByUserMockExam_IdAndQuestion_Id(Long userMockExamId, Long questionId);
    List<UserAnswer> findByUserMockExam_Id(Long userMockExamId);

    /**
     * 【修正ポイント】
     * データベースのタイムゾーン(UTC)を日本時間(+09:00)に変換して日付集計します。
     * JPQLでは時間変換関数に制限があるため、Native Queryを使用します。
     */
    @Query(value = "SELECT " +
            "  DATE(CONVERT_TZ(answered_at, '+00:00', '+09:00')) as date, " +
            "  COUNT(*) as count, " +
            "  AVG(CASE WHEN is_correct = true THEN 100.0 ELSE 0.0 END) as accuracy " +
            "FROM user_answers " +
            "WHERE user_id = :userId AND answered_at BETWEEN :start AND :end " +
            "GROUP BY DATE(CONVERT_TZ(answered_at, '+00:00', '+09:00'))", 
            nativeQuery = true)
    List<Map<String, Object>> findDailyStats(@Param("userId") Long userId, 
                                             @Param("start") LocalDateTime start, 
                                             @Param("end") LocalDateTime end);
    
    @Modifying
    @Query("DELETE FROM UserAnswer ua WHERE ua.userMockExam.mockExam.id = :mockExamId")
    void deleteByMockExamId(@Param("mockExamId") Long mockExamId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserAnswer ua WHERE ua.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);
    
    void deleteByUserId(Long userId);
}