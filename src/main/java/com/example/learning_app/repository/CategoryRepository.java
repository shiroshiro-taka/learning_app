package com.example.learning_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learning_app.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // カテゴリ名で検索
    Optional<Category> findByName(String name);

    // カテゴリ名の重複確認
    boolean existsByName(String name);
}
