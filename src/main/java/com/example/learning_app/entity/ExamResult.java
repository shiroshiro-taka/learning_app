package com.example.learning_app.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 結果ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // 受験者

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mock_exam_id", nullable = false)
    private MockExam mockExam; // 受験した模擬試験

    @Column(nullable = false)
    private int correctCount; // 正答数

    @Column(nullable = false)
    private int incorrectCount; // 誤答数

    @Column(nullable = false)
    private int totalQuestions; // 全問題数

    @Column(nullable = false)
    private int score; // 得点（例：100点満点換算）

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt; // 試験開始日時

    private LocalDateTime finishedAt; // 試験終了日時
}