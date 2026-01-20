package com.example.learning_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.example.learning_app.entity.Choice;
import com.example.learning_app.entity.Question;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    // 特定の問題に紐づく選択肢一覧を取得
    List<Choice> findByQuestion(Question question);

    // 問題IDで検索
    List<Choice> findByQuestionId(Long questionId);
    

    
    @Transactional
    @Modifying
    @Query("DELETE FROM Choice c WHERE c.question.id = :questionId")
    void deleteByQuestionId(Long questionId);
    
 // 🧩 user_answersで使われているか確認するクエリ
    @Query("SELECT CASE WHEN COUNT(ua) > 0 THEN true ELSE false END FROM UserAnswer ua WHERE ua.choice.id = :choiceId")
    boolean isChoiceUsedByUserAnswers(Long choiceId);    
}