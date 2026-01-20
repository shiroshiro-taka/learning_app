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
import lombok.Setter;

@Entity
@Table(name = "user_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 解答履歴ID

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // ユーザーID (必須)

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "question_id", nullable = false)
    private Question question; // 問題ID (必須)

    // 修正1: 未回答時は NULL を許容するため、nullable = true を明示
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "choice_id", nullable = true) 
    private Choice choice; // 選択肢エンティティ（未回答時はNULL可）
    
    // ExamResultエンティティは、試験終了後の結果レポートのために使用
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_result_id")
    private ExamResult examResult; 

    // 修正2: 正誤判定（is_correct）は未回答時や未判定時に NULL を許容するため、Booleanラッパー型に変更
    @Column(name = "is_correct", nullable = true) 
    @Setter
    private Boolean correct; // 正誤判定（未判定/未回答時はNULL可）
    
    /**
     * 正誤判定の結果を返すヘルパーメソッド。
     * null の場合は false（不正解/未回答扱い）と見なす。
     */
    public boolean isCorrect() {
        // Boolean.TRUE.equals() は NullPointerException を防ぐ安全な方法
        return Boolean.TRUE.equals(this.correct);
    }

    @CreationTimestamp
    @Column(name = "answered_at", nullable = false, updatable = false)
    private LocalDateTime answeredAt; // 解答日時
    
    // UserMockExam（試験インスタンス）との関連付け。
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_mock_exam_id", nullable = true) // 試験中に作成されるため必須とする
    private UserMockExam userMockExam;
    
    // 見直しフラグ
    @Column(name = "review_flag", nullable =true)
    private boolean reviewFlag = false;

    // ★ 以前あった getReviewFlag() メソッドは削除されました。
    //   これにより、Lombokが生成する安全な isReviewFlag() メソッドが使われます。
}