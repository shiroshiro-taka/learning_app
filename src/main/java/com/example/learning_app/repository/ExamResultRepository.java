package com.example.learning_app.repository;

import java.util.List; // 👈 Listをインポート

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learning_app.entity.ExamResult;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    
    /**
     * 指定されたユーザーIDの試験結果をすべて取得し、終了日時(finishedAt)の降順でソートします。
     * @param userId ユーザーのID
     * @return ユーザーIDに紐づくExamResultのリスト
     */
    List<ExamResult> findByUser_IdOrderByFinishedAtDesc(Long userId); // 👈 この行を追加
}