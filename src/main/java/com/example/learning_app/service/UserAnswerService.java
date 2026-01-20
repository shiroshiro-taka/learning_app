package com.example.learning_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.learning_app.entity.Choice;
import com.example.learning_app.entity.Question;
import com.example.learning_app.entity.UserAnswer;
import com.example.learning_app.entity.UserMockExam;
import com.example.learning_app.entity.Users;
import com.example.learning_app.repository.ChoiceRepository;
import com.example.learning_app.repository.QuestionRepository;
import com.example.learning_app.repository.UserAnswerRepository;
import com.example.learning_app.repository.UserMockExamRepository;

import lombok.RequiredArgsConstructor;

/**
 * ユーザーの模擬試験の解答（UserAnswer）に関するビジネスロジックを処理するサービス。
 * 解答の保存/更新、正誤判定、結果集計などを担当します。
 */
@Service
@RequiredArgsConstructor
public class UserAnswerService {

    private final UserAnswerRepository userAnswerRepository;
    private final UserMockExamRepository userMockExamRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;

    /**
     * ユーザーの解答を保存または更新し、正誤判定を行います。
     * @param userExamId UserMockExamのID
     * @param questionId 問題のID
     * @param selectedChoiceId ユーザーが選択した選択肢のID (未回答の場合はnull)
     * @param reviewFlag 見直しフラグ
     */
    @Transactional
    public void saveOrUpdateAnswer(Long userExamId, Long questionId, Long selectedChoiceId, boolean reviewFlag) {
        // 1. 関連エンティティの取得 (必須チェック)
        UserMockExam userExam = userMockExamRepository.findById(userExamId)
                .orElseThrow(() -> new IllegalArgumentException("指定されたUserMockExamが見つかりません。ID: " + userExamId));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("指定された問題が見つかりません。ID: " + questionId));

        // 2. 既存の解答を検索 (あれば更新、なければ新規作成)
        Optional<UserAnswer> existingAnswerOpt = userAnswerRepository.findByUserMockExam_IdAndQuestion_Id(userExamId, questionId);

        UserAnswer userAnswer;
        if (existingAnswerOpt.isPresent()) {
            userAnswer = existingAnswerOpt.get();
        } else {
            // 新規解答を作成 (初回保存)
            Users user = userExam.getUser(); // UserMockExamからUsersを取得
            
            userAnswer = UserAnswer.builder()
                    .user(user)
                    .userMockExam(userExam)
                    .question(question)
                    .build();
        }

        // 3. 選択肢の設定 (★修正箇所: NULLを許容し、未解答に対応)
        Choice selectedChoice = null;
        if (selectedChoiceId != null) {
            selectedChoice = choiceRepository.findById(selectedChoiceId)
                    // 選択肢IDが指定されたが、エンティティが見つからない場合はエラー
                    .orElseThrow(() -> new IllegalArgumentException("指定された選択肢が見つかりません。ID: " + selectedChoiceId));
        }
        
        // ユーザーが選択を解除した場合（selectedChoiceId=null）や、新規作成の場合はnullを設定
        userAnswer.setChoice(selectedChoice);


        // 4. 見直しフラグの更新
        userAnswer.setReviewFlag(reviewFlag);

        // 5. 正誤判定の実行 (★修正箇所: 未解答の場合は false/null ではなく、未解答のままにする)
        Boolean isCorrect = evaluateAnswer(question, selectedChoiceId);
        userAnswer.setCorrect(isCorrect);
        
        // 6. 回答時刻の更新 (解答内容が変更された、または見直しフラグが更新されたとき)
        userAnswer.setAnsweredAt(LocalDateTime.now());
        
        // 7. 保存
        userAnswerRepository.save(userAnswer);
        
        // ★ 修正3: UserMockExamの最新の質問IDを更新
        userExam.setLatestQuestionId(questionId);
        userMockExamRepository.save(userExam); 
    }
    
    /**
     * 正誤判定ロジック。
     * @param question 対象の問題
     * @param selectedChoiceId ユーザーが選択した選択肢ID (未回答の場合はnull)
     * @return 正解の場合はtrue, 不正解の場合はfalse, 未回答の場合はnull
     */
    private Boolean evaluateAnswer(Question question, Long selectedChoiceId) {
        if (selectedChoiceId == null) {
            // 未回答の場合は null を返す (または false にする設計もあり。ここでは null を維持)
            return null; 
        }

        // Questionエンティティに正解Choice IDを直接持たせる設計を仮定します
        Long correctChoiceId = question.getCorrectChoiceId(); 
        
        if (correctChoiceId == null) {
            // 正解が未設定の問題の場合
            return false;
        }

        // ユーザーの選択肢IDが正解の選択肢IDと一致するか判定
        return correctChoiceId.equals(selectedChoiceId); 
    }

    /**
     * 特定のUserMockExamに対する正答数をカウントします。
     * @param userExamId UserMockExamのID
     * @return 正答数
     */
    public int calculateCorrectCount(Long userExamId) {
        // isCorrectがnull（未回答）のデータはカウントしない
        return userAnswerRepository.countByUserMockExam_IdAndCorrectTrue(userExamId);
    }

    /**
     * 特定のUserMockExamに紐づく全ての解答を取得します。
     * 見直し画面表示に使用されます。
     * @param userExamId UserMockExamのID
     * @return UserAnswerのリスト
     */
    public List<UserAnswer> findByUserExamId(Long userExamId) {
        return userAnswerRepository.findByUserMockExam_Id(userExamId);
    }

	public UserAnswer findUserAnswerByQuestion(Long id, Long id2) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}