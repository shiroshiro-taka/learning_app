package com.example.learning_app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
     * 月間の学習カレンダー用データを取得する
     */
    public List<CalendarDayDto> getCalendarData(Long userId, YearMonth yearMonth) {
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        
        // 1. その月の全回答統計をDBから取得
        List<Map<String, Object>> stats = userAnswerRepository.findDailyStats(
            userId, 
            startOfMonth.atStartOfDay(), 
            endOfMonth.atTime(23, 59, 59)
        );

        // 2. 統計データを Map に変換 (Key: LocalDate)
        // キーの型を LocalDate に完全に一致させるための正規化処理を強化
        Map<LocalDate, Map<String, Object>> statsMap = stats.stream()
            .collect(Collectors.toMap(
                s -> {
                    Object dateObj = s.get("date") != null ? s.get("date") : s.get("DATE");
                    if (dateObj == null) return LocalDate.MIN;

                    if (dateObj instanceof java.time.LocalDate) {
                        return (java.time.LocalDate) dateObj;
                    } else if (dateObj instanceof java.sql.Date) {
                        return ((java.sql.Date) dateObj).toLocalDate();
                    } else if (dateObj instanceof java.sql.Timestamp) {
                        return ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate();
                    } else if (dateObj instanceof java.util.Date) {
                        return new java.sql.Date(((java.util.Date) dateObj).getTime()).toLocalDate();
                    } else {
                        // 文字列 (yyyy-MM-dd ...) として返ってきた場合
                        try {
                            return LocalDate.parse(dateObj.toString().substring(0, 10));
                        } catch (Exception e) {
                            return LocalDate.MIN;
                        }
                    }
                },
                s -> s,
                (v1, v2) -> v1 // 重複した場合は最初の値を優先
            ));

        // 3. 1日から月末までループして全日付分の DTO を生成
        List<CalendarDayDto> calendarDays = new ArrayList<>();
        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            // statsMap から正規化された LocalDate キーで取得
            Map<String, Object> dayData = statsMap.get(date);
            
            if (dayData != null) {
                // 回答数(count/COUNT)と正答率(accuracy/ACCURACY)の取得
                Object countObj = dayData.get("count") != null ? dayData.get("count") : dayData.get("COUNT");
                Object accuracyObj = dayData.get("accuracy") != null ? dayData.get("accuracy") : dayData.get("ACCURACY");

                long count = (countObj != null) ? ((Number) countObj).longValue() : 0L;
                double accuracy = (accuracyObj != null) ? ((Number) accuracyObj).doubleValue() : 0.0;
                
                calendarDays.add(new CalendarDayDto(date, count, accuracy));
            } else {
                // 一致するデータがない場合は 0 で初期化
                calendarDays.add(new CalendarDayDto(date, 0L, 0.0));
            }
        }
        return calendarDays;
    }

    /**
     * 特定の日の詳細な学習状況を取得する
     */
    public DailyDetailDto getDailyDetail(Long userId, LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd (E)", Locale.JAPANESE));

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        List<Map<String, Object>> stats = userAnswerRepository.findDailyStats(userId, startOfDay, endOfDay);
        
        long totalCount = 0;
        long correctCount = 0;
        double accuracy = 0.0;

        if (stats != null && !stats.isEmpty()) {
            Map<String, Object> dayData = stats.get(0);
            
            Object countObj = dayData.get("count") != null ? dayData.get("count") : dayData.get("COUNT");
            Object accuracyObj = dayData.get("accuracy") != null ? dayData.get("accuracy") : dayData.get("ACCURACY");

            totalCount = (countObj != null) ? ((Number) countObj).longValue() : 0L;
            double rawAccuracy = (accuracyObj != null) ? ((Number) accuracyObj).doubleValue() : 0.0;
            
            accuracy = BigDecimal.valueOf(rawAccuracy)
                                 .setScale(1, RoundingMode.HALF_UP)
                                 .doubleValue();
                                 
            correctCount = Math.round(totalCount * (rawAccuracy / 100.0));
        }

        // 模擬試験結果の取得
        List<UserMockExam> finishedExams = userMockExamRepository.findByUserIdAndFinishedAtBetween(userId, startOfDay, endOfDay);
        
        List<MockResultDto> mockExams = finishedExams.stream()
            .map(exam -> {
                int score = exam.getCorrectCount();
                int totalQuestions = (exam.getMockExam() != null) ? exam.getMockExam().getQuestionCount() : 0;
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