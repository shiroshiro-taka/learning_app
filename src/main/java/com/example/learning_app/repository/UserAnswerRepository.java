package com.example.learning_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learning_app.entity.Question;
import com.example.learning_app.entity.UserAnswer;
import com.example.learning_app.entity.Users;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    // 既存のメソッド
    List<UserAnswer> findByUser(Users user);
    
    List<UserAnswer> findByUser_Id(Long userId);

    List<UserAnswer> findByQuestion(Question question);

    UserAnswer findByUserAndQuestion(Users user, Question question);
    
    // --- ★ 模擬試験の結果集計のための追加メソッド ---

    /**
     * 特定の UserMockExam インスタンスにおける、正解 (isCorrect = true) の数をカウントする。
     * UserAnswer エンティティに isCorrect: Boolean と userMockExam 関連が必須。
     */
    int countByUserMockExam_IdAndCorrectTrue(Long userMockExamId);

    /**
     * 特定の UserMockExam インスタンスIDと問題IDに基づいて解答を取得する。
     * 回答の更新時（再回答や見直しフラグの更新時）に使用。
     */
    Optional<UserAnswer> findByUserMockExam_IdAndQuestion_Id(Long userMockExamId, Long questionId);

    /**
     * 特定の UserMockExam インスタンスIDに関連するすべての回答を取得する。
     * UserAnswerService の findByUserExamId メソッドで使用されます。
     * @param userMockExamId UserMockExamのID
     * @return UserAnswerエンティティのリスト
     */
    List<UserAnswer> findByUserMockExam_Id(Long userMockExamId);
}