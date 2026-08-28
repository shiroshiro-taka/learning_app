package com.example.learning_app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.learning_app.entity.UserMockExam;

/**
 * UserMockExamエンティティのためのリポジトリ。
 * ユーザーの模擬試験インスタンスに関するCRUD操作を提供します。
 */
@Repository
public interface UserMockExamRepository extends JpaRepository<UserMockExam, Long> {

    /**
     * 特定のユーザーが、特定の模擬試験IDに対して、
     * 終了日時（finishedAt）がNULLである（＝受験中である）UserMockExamインスタンスを検索します。
     * * @param userId ユーザーID
     * @param mockExamId 模擬試験ID
     * @return 受験中のUserMockExam（存在しない場合はOptional.empty()）
     */
    Optional<UserMockExam> findByUserIdAndMockExam_IdAndFinishedAtIsNull(Long userId, Long mockExamId);

	Optional<UserMockExam> findTopByMockExam_IdAndUser_IdOrderByStartedAtDesc(Long examId, Long userId);
    
    /* * 【参考】: 以前のUserMockExamServiceで利用したエンティティ引数バージョン
     * Optional<UserMockExam> findByUserAndMockExamAndFinishedAtIsNull(Users user, MockExam mockExam);
     */
	List<UserMockExam> findByUserIdAndFinishedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

	@Modifying
	@Query("DELETE FROM UserMockExam ume WHERE ume.mockExam.id = :mockExamId")
	void deleteByMockExamId(@Param("mockExamId") Long mockExamId);

	@Modifying
    @Query("DELETE FROM UserMockExam ume WHERE ume.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
