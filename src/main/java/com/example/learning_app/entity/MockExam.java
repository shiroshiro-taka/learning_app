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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mock_exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockExam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 模擬試験ID

    @Column(nullable = false, length = 100)
    private String examName; // 試験名（例：Java Silver 模擬試験 第1回）

    @Column(nullable = false)
    private int questionCount; // 出題数（例：80）

    @Column(nullable = false)
    private int durationMinutes; // 試験時間（例：180）

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description; // 試験の説明・備考

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 登録日時
    
    // ★修正1: フィールド名を questions に統一し、結合エンティティ MockExamQuestion を使用
    // MockExamQuestion が Question エンティティへの参照を持つ前提
    @OneToMany(mappedBy = "mockExam", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MockExamQuestion> questions;
    
    // ★修正2: 重複していた examQuestions フィールドを削除
    // private List<MockExamQuestion> examQuestions;
    
    // ※補足: Controllerで List<Question> が期待されていたため、
    // MockExamQuestion から Question のリストを返すカスタムメソッドが必要になる場合があります。
    // しかし、このエンティティレベルでは MockExamQuestion のリストを維持するのが適切です。
    // Controller側での型変換（または MockExamService での取得ロジック）が必要になります。
}