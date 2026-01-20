package com.example.learning_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnswerDto {

    /**
     * 見直し画面のテンプレートが期待する、解答の状態を表すEnum。
     */
    public enum AnswerStatus {
        UNANSWERED("未解答"),
        ANSWERED("解答済み");
        
        private final String displayName;
        
        AnswerStatus(String displayName) {
            this.displayName = displayName;
        }

        // Thymeleaf (${dto.answerStatus.displayName}) からアクセスされる
        public String getDisplayName() {
            return displayName;
        }
        
        // Thymeleaf (${dto.answerStatus.name().toLowerCase()}) からアクセスされる
        // name() メソッドはEnum標準で提供されるため不要
    }

    /**
     * 問題ID (Question.id)
     */
    private Long questionId;

    /**
     * 問題の表示番号 (1, 2, 3, ...)
     */
    private Integer questionNumber; 

    /**
     * ユーザーが選択した選択肢のID (Choice.id)
     */
    private Long selectedChoiceId;

    /**
     * ユーザーが見直しのために付けたフラグ
     */
    private boolean reviewFlag;
    
    // isAnswered は selectedChoiceId から判断できるため削除します（冗長性排除）
    // private boolean isAnswered; 
    
    // ★ 修正箇所: Thymeleafのエラーを解消するため、Enum型の answerStatus を追加
    private AnswerStatus answerStatus; 
    
    // --- コンビニエンスメソッド ---
    
    /**
     * selectedChoiceId の有無に基づいて answerStatus を設定するヘルパーメソッド。
     */
    public void updateAnswerStatus() {
        boolean answered = (this.selectedChoiceId != null && this.selectedChoiceId > 0);
        
        if (answered) {
            this.answerStatus = AnswerStatus.ANSWERED;
        } else {
            this.answerStatus = AnswerStatus.UNANSWERED;
        }
    }
}