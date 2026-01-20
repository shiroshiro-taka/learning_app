package com.example.learning_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learning_app.entity.Category;
import com.example.learning_app.entity.Score;
import com.example.learning_app.entity.Users;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    // 特定ユーザーのスコア一覧
    List<Score> findByUser(Users user);

    // 特定ユーザー & カテゴリ のスコア（重複防止）
    Optional<Score> findByUserAndCategory(Users user, Category category);
    
    Optional<Score> findByUserIdAndCategoryId(Long userId, Long categoryId);
    
}