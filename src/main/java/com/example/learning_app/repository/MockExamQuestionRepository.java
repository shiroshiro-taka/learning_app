package com.example.learning_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learning_app.entity.MockExamQuestion;

public interface MockExamQuestionRepository extends JpaRepository<MockExamQuestion, Long> {
    void deleteByMockExamId(Long mockExamId); // 編集時に再登録用
}