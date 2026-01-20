package com.example.learning_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learning_app.entity.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {

    // usernameでユーザーを検索（ログイン認証用など）
    Optional<Users> findByUsername(String username);

    // usernameの存在確認
    boolean existsByUsername(String username);
}