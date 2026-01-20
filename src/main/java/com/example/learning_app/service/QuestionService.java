package com.example.learning_app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.learning_app.entity.Question;
import com.example.learning_app.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    /**
     * キーワードとカテゴリIDで問題を検索する（既存のメソッド）
     */
    public List<Question> searchQuestions(String keyword, Long categoryId) {
        return questionRepository.search(keyword, categoryId);
    }
    
    /**
     * カテゴリIDのみで問題を検索する新しいメソッド
     */
    public List<Question> searchQuestions(Long categoryId) {
        // キーワード検索をしないため、keyword引数に null を渡す
        String keyword = null;
        return questionRepository.search(keyword, categoryId);
    }
    
    // ----------------------------------------------------------------------
    // ★ 試験結果保存のために新しく追加するメソッド
    // ----------------------------------------------------------------------

    /**
     * 指定された模擬試験に含まれる問題の総数をカウントします。
     * * @param examId 模擬試験ID
     * @return 問題の総数
     */
    public int countQuestionsByExamId(Long examId) {
        // QuestionエンティティがMockExamエンティティ（またはID）と関連付けられていることを前提とします。
        // countBy... というメソッドはSpring Data JPAのリポジトリで自動生成可能です。
    	return (int) questionRepository.countQuestionsByExamId(examId);
    }

	public Question findByIdWithChoices(Long currentQuestionId) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}