package com.example.learning_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.learning_app.entity.Category;
import com.example.learning_app.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 特定カテゴリに属する問題一覧を取得
    List<Question> findByCategory(Category category);

    // カテゴリID指定でも取得可能
    List<Question> findByCategory_Id(Long categoryId);
    
    // 追加：ID昇順で取得
    List<Question> findByCategory_IdOrderByIdAsc(Long categoryId);
    
    long countByCategory(Category category);
    
    long countByCategory_Id(Long categoryId);
    
    @Query("""
            SELECT q FROM Question q
            WHERE (:keyword IS NULL OR q.questionText LIKE %:keyword%)
              AND (:categoryId IS NULL OR q.category.id = :categoryId)
            ORDER BY q.id ASC
            """)
        List<Question> search(@Param("keyword") String keyword,
                              @Param("categoryId") Long categoryId);
    
    @Query("SELECT q FROM Question q JOIN FETCH q.category WHERE q.id = :id")
    Optional<Question> findByIdWithCategory(@Param("id") Long id);
    
    @Query("SELECT COUNT(q) FROM Question q JOIN q.mockExams me WHERE me.id = :examId")
    long countQuestionsByExamId(@Param("examId") Long examId);
    
    /**
     * 【★修正されたメソッド】
     * 指定された模擬試験ID (examId) に関連する全ての質問を、IDの昇順で取得します。
     * QuestionエンティティにquestionNumberフィールドがないため、代わりにID (q.id) でソートします。
     * @param examId 模擬試験のID (MockExamのID)
     * @return 質問エンティティのリスト（ID順）
     */
    @Query("SELECT q FROM Question q JOIN q.mockExams me WHERE me.id = :examId ORDER BY q.id ASC")
    List<Question> findByMockExam_IdOrderByQuestionNumberAsc(@Param("examId") Long examId);
}