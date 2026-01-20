package com.example.learning_app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service; // ★Serviceアノテーションを追加

import com.example.learning_app.dto.UserAnswerDto;
import com.example.learning_app.entity.Question;
import com.example.learning_app.entity.UserAnswer;
import com.example.learning_app.entity.UserMockExam;

import lombok.RequiredArgsConstructor; // DIのために追加

/**
 * 模擬試験全体のロジック（DTO変換、採点など）を担うサービス。
 */
@Service // Springのサービスとして認識させる
@RequiredArgsConstructor
public class ExamService {
    
    // このサービスが他のRepositoryやServiceに依存する場合、ここにDIする

    /**
     * UserAnswerエンティティとQuestionエンティティのリストから、
     * 見直し画面などで使用する UserAnswerDto のリストを作成する。
     * (未解答の問題にもダミーのDTOが作成される)
     * * @param userMockExam ユーザーの模擬試験インスタンス
     * @param allQuestions 模擬試験に含まれる全問題のリスト
     * @return UserAnswerDto のリスト
     */
    public List<UserAnswerDto> createUserAnswerDtos(UserMockExam userMockExam, List<Question> allQuestions) {
        
        // 既存のUserAnswerをQuestionIdをキーとしたMapに変換しておく
        // UserMockExamエンティティからUserAnswerリストを取得（UserMockExam内にgetUserAnswers()が必要）
        List<UserAnswer> userAnswers = userMockExam.getUserAnswers();
        
        // userAnswers が null の場合の防御
        if (userAnswers == null) {
            userAnswers = List.of();
        }
        
        Map<Long, UserAnswer> existingAnswersMap = userAnswers.stream()
            .collect(Collectors.toMap(
                answer -> answer.getQuestion().getId(), 
                answer -> answer
            ));

        List<UserAnswerDto> dtoList = new ArrayList<>();
        
        // 全問題リストを回してDTOを作成する (未回答の問題も含めるため)
        for (int i = 0; i < allQuestions.size(); i++) {
            Question question = allQuestions.get(i);
            UserAnswer existingAnswer = existingAnswersMap.get(question.getId());
            
            UserAnswerDto dto = new UserAnswerDto();
            dto.setQuestionId(question.getId());
            dto.setQuestionNumber(i + 1); // 1から始まる問題番号を設定
            
            if (existingAnswer != null) {
                // 既存の回答がある場合、その情報をDTOにマッピング
                dto.setSelectedChoiceId(
                    existingAnswer.getChoice() != null ? existingAnswer.getChoice().getId() : null
                );
                // ★修正: boolean型のLombokゲッターは getReviewFlag() である可能性が高い
                // boolean型の場合、Boolean.TRUE.equals(getReviewFlag()) のような防御的コーディングも推奨されますが、
                // Entityフィールドがプリミティブ boolean または Boolean のどちらかによって異なります。
                // 安全策として getReviewFlag() を使用します。
                dto.setReviewFlag(existingAnswer.isReviewFlag()); 
            } else {
                // 既存の回答がない場合（未回答）
                dto.setSelectedChoiceId(null);
                dto.setReviewFlag(false);
            }
            
            // 回答ステータスを更新
            dto.updateAnswerStatus();
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }
}