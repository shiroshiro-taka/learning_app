package com.example.learning_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.learning_app.entity.ExamResult;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    
    /**
     * 指定されたユーザーIDの試験結果をすべて取得し、終了日時(finishedAt)の降順でソートします。
     * @param userId ユーザーのID
     * @return ユーザーIDに紐づくExamResultのリスト
     */
    List<ExamResult> findByUser_IdOrderByFinishedAtDesc(Long userId); // 👈 この行を追加
    
    @Modifying
    @Query("DELETE FROM ExamResult e WHERE e.mockExam.id = :mockExamId")
    void deleteByMockExamId(@Param("mockExamId") Long mockExamId);
}