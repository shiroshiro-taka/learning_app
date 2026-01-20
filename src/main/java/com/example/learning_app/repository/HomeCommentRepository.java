package com.example.learning_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learning_app.entity.HomeComment;

public interface HomeCommentRepository extends JpaRepository<HomeComment, Long> {
}