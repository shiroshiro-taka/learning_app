package com.example.learning_app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // スコアID

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // ユーザーID

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // カテゴリID

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount; // 正解数

    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount; // 不正解数

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 最終更新日時
   
    public Score(Users user, Category category, int correctCount, int wrongCount) {
        this.user = user;
        this.category = category;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.updatedAt = LocalDateTime.now();
    }
}

