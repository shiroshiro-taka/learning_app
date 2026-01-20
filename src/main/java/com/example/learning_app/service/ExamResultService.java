package com.example.learning_app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.learning_app.entity.ExamResult;
import com.example.learning_app.entity.UserMockExam;
import com.example.learning_app.repository.ExamResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamResultService {

    private final ExamResultRepository examResultRepository;

    /**
     * 試験結果を計算し、ExamResultとして永続化します。
     *
     * @param userExam       UserMockExamエンティティ（ユーザー、模擬試験、開始/終了時間を含む）
     * @param correctCount   計算済みの正答数
     * @param totalQuestions 全問題数
     * @return 保存されたExamResultエンティティ
     */
    @Transactional
    public ExamResult saveResult(UserMockExam userExam, int correctCount, int totalQuestions) {

        // 誤答数を計算
        int incorrectCount = totalQuestions - correctCount;
        
        // スコアを100点満点換算で計算（必要に応じて端数処理を調整してください）
        int score = (int) Math.round(((double) correctCount / totalQuestions) * 100);

        // ExamResultエンティティを構築
        ExamResult result = ExamResult.builder()
            .user(userExam.getUser())               // UserMockExamからUserを取得
            .mockExam(userExam.getMockExam())       // UserMockExamからMockExamを取得
            .correctCount(correctCount)
            .incorrectCount(incorrectCount)
            .totalQuestions(totalQuestions)
            .score(score)
            .startedAt(userExam.getStartedAt())     // UserMockExamから開始日時を取得
            .finishedAt(userExam.getFinishedAt())   // UserMockExamから終了日時を取得
            .build();

        // データベースに保存
        return examResultRepository.save(result);
    }
}