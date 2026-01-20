package com.example.learning_app.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ユーザーID

    @Column(nullable = false, unique = true, length = 50)
    private String username; // ユーザー名（ログインID）

    @Column(nullable = false, length = 255)
    private String password; // パスワード（BCrypt）

    @Column(nullable = false, length = 20)
    private String role; // 権限（ROLE_USER / ROLE_ADMIN）

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 登録日時

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt; // 更新日時

    // リレーション
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserAnswer> userAnswers;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Score> scores;

	public Users orElseThrow(Object object) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
