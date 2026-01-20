package com.example.learning_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learning_app.entity.MockExam;

public interface MockExamRepository extends JpaRepository<MockExam, Long> {
}