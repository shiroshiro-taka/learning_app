package com.example.learning_app.service;

import java.util.List;
import java.util.Optional; // Optionalをimport

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.learning_app.entity.MockExam;
import com.example.learning_app.entity.MockExamQuestion;
import com.example.learning_app.entity.Question;
import com.example.learning_app.repository.MockExamQuestionRepository;
import com.example.learning_app.repository.MockExamRepository;
import com.example.learning_app.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MockExamService {

    private final MockExamRepository mockExamRepository;
    private final MockExamQuestionRepository mockExamQuestionRepository;
    private final QuestionRepository questionRepository;

    /** 模擬試験一覧取得 */
    public List<MockExam> findAll() {
        return mockExamRepository.findAll();
    }

    
    @Transactional
    public List<Question> getQuestions(Long examId) {
        // ★修正: findById が Optional を返すように変更されたため、orElseThrow() を使用してエンティティを取得
        MockExam exam = findById(examId)
                            .orElseThrow(() -> new IllegalArgumentException("指定された模擬試験が見つかりません。ID: " + examId));
        
        // MockExamエンティティが List<MockExamQuestion> のゲッターを持つ前提
        return exam.getQuestions().stream() 
                .map(MockExamQuestion::getQuestion)
                .toList();
    }
    
    /** 模擬試験をID指定で取得 */
    // ★修正: 戻り値を Optional<MockExam> に変更し、Optionalをそのまま返す
    public Optional<MockExam> findById(Long id) {
        return mockExamRepository.findById(id);
    }

    /** 模擬試験登録 */
    @Transactional
    public void createMockExam(MockExam exam, List<Long> questionIds) {
        MockExam savedExam = mockExamRepository.save(exam);

        for (Long qid : questionIds) {
            // findById が Optional を返すため、Optional.ifPresent を利用するとより安全
            questionRepository.findById(qid).ifPresent(q -> {
                MockExamQuestion meq = MockExamQuestion.builder()
                        .mockExam(savedExam)
                        .question(q)
                        .build();
                mockExamQuestionRepository.save(meq);
            });
        }
    }

    /** 模擬試験更新 */
    @Transactional
    public void updateMockExam(MockExam exam, List<Long> questionIds) {
        mockExamRepository.save(exam);
        // Repositoryに deleteByMockExamId(Long) が定義されている前提
        mockExamQuestionRepository.deleteByMockExamId(exam.getId());

        for (Long qid : questionIds) {
            // findById が Optional を返すため、Optional.ifPresent を利用するとより安全
            questionRepository.findById(qid).ifPresent(q -> {
                MockExamQuestion meq = MockExamQuestion.builder()
                        .mockExam(exam)
                        .question(q)
                        .build();
                mockExamQuestionRepository.save(meq);
            });
        }
    }

    /** 模擬試験削除 */
    @Transactional
    public void deleteExam(Long id) {
        mockExamQuestionRepository.deleteByMockExamId(id);
        mockExamRepository.deleteById(id);
    }
}