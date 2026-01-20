package com.example.learning_app.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

// ★ JacksonのJsonIgnoreをインポート
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 問題ID

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText; // 問題文

    // 文字列での正答保持（互換性のため残す）
    @Column(name = "correct_answer", nullable = true, length = 255)
    private String correctAnswer; 

    // 追加：正答選択肢ID
    @Column(name = "correct_choice_id", nullable = true)
    private Long correctChoiceId;
    
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation; // 解説

    // ★ category を EAGER にして安全にアクセス可能に
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // カテゴリ

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 登録日時

    // choices は問題ごとに複数あるので LAZY のままでも OK
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Choice> choices;

    // ★ 修正点: 無限再帰を防ぐため、この逆参照を無視する
    @JsonIgnore 
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private List<UserAnswer> userAnswers;
    
    // ★ 正解選択肢のインデックス（0,1,2,3...）
    private Integer correctChoiceIndex;
    
 // ★ 追加: 模擬試験との関連 (多対多)
    // 中間テーブル 'mock_exam_questions' の存在を仮定します。
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "mock_exam_questions", // 適切な中間テーブル名に置き換えてください
        joinColumns = @JoinColumn(name = "question_id"),
        inverseJoinColumns = @JoinColumn(name = "mock_exam_id")
    )
    private List<MockExam> mockExams; // ★ リポジトリのJPQLで使用するフィールド名
}