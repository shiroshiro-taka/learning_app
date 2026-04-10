package com.example.learning_app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.learning_app.entity.MockExam;
import com.example.learning_app.entity.MockExamQuestion;
import com.example.learning_app.entity.Question;
import com.example.learning_app.repository.ExamResultRepository;
import com.example.learning_app.repository.MockExamQuestionRepository;
import com.example.learning_app.repository.MockExamRepository;
import com.example.learning_app.repository.QuestionRepository;
import com.example.learning_app.repository.UserAnswerRepository;
import com.example.learning_app.repository.UserMockExamRepository; // 追加

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MockExamService {

    private final MockExamRepository mockExamRepository;
    private final MockExamQuestionRepository mockExamQuestionRepository;
    private final QuestionRepository questionRepository;
    
    // 削除時に必要なリポジトリ
    private final UserAnswerRepository userAnswerRepository;
    private final ExamResultRepository examResultRepository;
    private final UserMockExamRepository userMockExamRepository;

    /** 模擬試験一覧取得 */
    public List<MockExam> findAll() {
        return mockExamRepository.findAll();
    }

    /** 模擬試験に紐付く問題一覧を取得 */
    @Transactional(readOnly = true)
    public List<Question> getQuestions(Long examId) {
        MockExam exam = findById(examId)
                            .orElseThrow(() -> new IllegalArgumentException("指定された模擬試験が見つかりません。ID: " + examId));
        
        return exam.getQuestions().stream() 
                .map(MockExamQuestion::getQuestion)
                .toList();
    }
    
    /** 模擬試験をID指定で取得 */
    public Optional<MockExam> findById(Long id) {
        return mockExamRepository.findById(id);
    }

    /** 模擬試験登録 */
    @Transactional
    public void createMockExam(MockExam exam, List<Long> questionIds) {
        MockExam savedExam = mockExamRepository.save(exam);

        if (questionIds != null) {
            for (Long qid : questionIds) {
                questionRepository.findById(qid).ifPresent(q -> {
                    MockExamQuestion meq = MockExamQuestion.builder()
                            .mockExam(savedExam)
                            .question(q)
                            .build();
                    mockExamQuestionRepository.save(meq);
                });
            }
        }
    }

    /** 模擬試験更新 */
    @Transactional
    public void updateMockExam(MockExam exam, List<Long> questionIds) {
        mockExamRepository.save(exam);
        mockExamQuestionRepository.deleteByMockExamId(exam.getId());

        if (questionIds != null) {
            for (Long qid : questionIds) {
                questionRepository.findById(qid).ifPresent(q -> {
                    MockExamQuestion meq = MockExamQuestion.builder()
                            .mockExam(exam)
                            .question(q)
                            .build();
                    mockExamQuestionRepository.save(meq);
                });
            }
        }
    }

    /** * 模擬試験削除
     * 受験履歴等の関連データを末端（子・孫）から順番に消去し、外部キー制約エラーを回避する
     */
    @Transactional
    public void deleteExam(Long id) {
        // 1. 最末端の「回答詳細（孫）」を削除
        userAnswerRepository.deleteByMockExamId(id);

        // 2. 「受験結果スコア（子）」を削除
        examResultRepository.deleteByMockExamId(id);

        // 3. 「試験実施インスタンス（子）」を削除
        userMockExamRepository.deleteByMockExamId(id);

        // 4. 「模擬試験と問題の紐付け」を削除
        mockExamQuestionRepository.deleteByMockExamId(id);

        // 5. 最後に「模擬試験本体（親）」を削除
        mockExamRepository.deleteById(id);
    }
}