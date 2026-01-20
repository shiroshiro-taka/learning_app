package com.example.learning_app.form;

import lombok.Data;

/**
 * 見直し画面へ遷移する際にフォームから送られるデータを受け取るクラス (DTO/Form)。
 * データベースへの永続化は行わないため、@Entity や @Repository は不要です。
 */
@Data
public class ReviewForm {
    
    // 見直し対象のUserMockExamのID（ExamControllerで必須）
    private Long userMockExamId; 
    
    // 全解答を見直すかどうかのフラグ
    private boolean reviewAnswers; 
    
    // 間違いのみを見直すかどうかのフラグ
    private boolean reviewMistakes; 
}