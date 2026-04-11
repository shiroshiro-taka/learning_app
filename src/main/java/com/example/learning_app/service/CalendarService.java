package com.example.learning_app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.learning_app.dto.CalendarDayDto;
import com.example.learning_app.dto.DailyDetailDto;
import com.example.learning_app.dto.DailyDetailDto.MockResultDto;
import com.example.learning_app.entity.UserMockExam;
import com.example.learning_app.repository.UserAnswerRepository;
import com.example.learning_app.repository.UserMockExamRepository;

@Service
public class CalendarService {

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    @Autowired
    private UserMockExamRepository userMockExamRepository;

    /**
     * カレンダー表示用の月間集計データを取得
     * 各日の 00:00:00 〜 23:59:59.999 を正確に指定して集計することで、
     * DBセッションのタイムゾーンに左右されない結果を保証します。
     */
    public List<CalendarDayDto> getCalendarData(Long userId, YearMonth yearMonth) {
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        
        List<CalendarDayDto> calendarDays = new ArrayList<>();

        // 月の開始日から終了日まで1日ずつループして集計
        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            // 日本時間の開始時刻と終了時刻を生成
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            // Repositoryの新規メソッドでその日のデータを取得
            long count = userAnswerRepository.countByUserIdAndAnsweredAtBetween(userId, startOfDay, endOfDay);
            Double accuracy = userAnswerRepository.calculateAccuracyByUserIdAndAnsweredAtBetween(userId, startOfDay, endOfDay);
            
            calendarDays.add(new CalendarDayDto(date, count, accuracy != null ? accuracy : 0.0));
        }
        return calendarDays;
    }

    /**
     * 特定の日の詳細データを取得
     */
    public DailyDetailDto getDailyDetail(Long userId, LocalDate date) {
        // 日本語形式の日付文字列（例: 2026/04/12 (日)）
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd (E)", Locale.JAPANESE));

        // 日本時間の 00:00:00 と 23:59:59.999 を生成
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        // 1. 回答数と正答率の取得
        long totalCount = userAnswerRepository.countByUserIdAndAnsweredAtBetween(userId, startOfDay, endOfDay);
        Double rawAccuracy = userAnswerRepository.calculateAccuracyByUserIdAndAnsweredAtBetween(userId, startOfDay, endOfDay);
        
        long correctCount = 0;
        double accuracy = 0.0;

        if (totalCount > 0 && rawAccuracy != null) {
            // 小数第1位で四捨五入
            accuracy = BigDecimal.valueOf(rawAccuracy).setScale(1, RoundingMode.HALF_UP).doubleValue();
            // 総回答数と正答率から正解数を逆算
            correctCount = Math.round(totalCount * (rawAccuracy / 100.0));
        }

        // 2. 模擬試験結果の取得
        List<UserMockExam> finishedExams = userMockExamRepository.findByUserIdAndFinishedAtBetween(userId, startOfDay, endOfDay);
        
        List<MockResultDto> mockExams = finishedExams.stream()
            .map(exam -> {
                int score = exam.getCorrectCount();
                int totalQuestions = (exam.getMockExam() != null) ? exam.getMockExam().getQuestionCount() : 0;
                // 正答率60%以上で合格判定
                boolean passed = totalQuestions > 0 && ((double) score / totalQuestions) >= 0.6;
                return new MockResultDto(
                    (exam.getMockExam() != null) ? exam.getMockExam().getExamName() : "不明な試験",
                    score,
                    totalQuestions,
                    passed
                );
            })
            .collect(Collectors.toList());

        return new DailyDetailDto(formattedDate, totalCount, correctCount, accuracy, mockExams);
    }
}