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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="user_mock_exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMockExam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name="user_id", nullable=false)
    private Users user;
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name="mock_exam_id", nullable=false)
    private MockExam mockExam;
    
    // ★ 修正1: 最後に見ていた問題IDを保持するフィールドを追加
    // 未回答時は NULL を許容
    @Column(name = "latest_question_id", nullable = true) 
    private Long latestQuestionId; 
        
    private LocalDateTime startedAt; 
    
    private LocalDateTime finishedAt;
    
    @Column(name = "correct_count", nullable = false) 
    private int correctCount = 0; 
    
    @JsonIgnore
    @OneToMany(mappedBy="userMockExam", cascade=CascadeType.ALL)
    private List<UserAnswer> userAnswers;

	// ★ 修正2: 外部から呼ばれるスタブメソッドは削除または実装を想定
    // コントローラーで使用されているため、今回はフィールドのgetter/setterに任せる
    /*
	public Long getLatestQuestionId() {
		return this.latestQuestionId;
	}

	public static UserMockExam findLatestUserMockExam(Long examId, Long id2) {
		// リポジトリメソッドの想定
		return null;
	}

	public static UserMockExam finishExam(Long userExamId) {
		// サービスメソッドの想定
		return null;
	}
    */
}