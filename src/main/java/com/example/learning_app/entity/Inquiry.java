package com.example.learning_app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;  // 問い合わせたユーザー名
    private String email;
    private String category;
    
    private boolean replyWanted; // 🔹 返信希望（true=する, false=しない）
    
    @Column(length = 2000)
    private String message;

    private LocalDateTime createdAt;

    // --- コンストラクタ ---
    public Inquiry() {
        this.createdAt = LocalDateTime.now();
    }

    // --- getter/setter ---
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCategory() { return category; } 
    public void setCategory(String category) { this.category = category; } 
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isReplyWanted() { return replyWanted; }
    public void setReplyWanted(boolean replyWanted) { this.replyWanted = replyWanted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}