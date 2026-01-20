package com.example.learning_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "choices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude // ★無限ループ防止
    private Question question;

    @Column(name = "choice_text", nullable = false, length = 255)
    private String choiceText;

    /**
     * 問題の正解IDと自分のIDを比較して正誤を判定する
     */
    public boolean isCorrectChoice() {
        if (question == null || question.getCorrectChoiceId() == null) {
            return false;
        }
        return this.id.equals(question.getCorrectChoiceId());
    }
}