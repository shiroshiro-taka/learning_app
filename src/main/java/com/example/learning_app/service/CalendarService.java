package com.example.learning_app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
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

    private static final ZoneId TOKYO_ZONE = ZoneId.of("Asia/Tokyo");

    /**
     * カレンダー表示用の月間集計データを取得
     */
    public List<CalendarDayDto> getCalendarData(Long userId, YearMonth yearMonth) {
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        
        // ★修正: LocalDate を日本時間の開始・終了に変換
        LocalDateTime startDateTime = startOfMonth.atStartOfDay(TOKYO_ZONE).toLocalDateTime();
        LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX).atZone(TOKYO_ZONE).toLocalDateTime();

        List<Map<String, Object>> stats = userAnswerRepository.findDailyStats(userId, startDateTime, endDateTime);

        Map<LocalDate, Map<String, Object>> statsMap = stats.stream()
            .collect(Collectors.toMap(
                s -> {
                    Object dateObj = s.get("date") != null ? s.get("date") : s.get("DATE");
                    if (dateObj == null) return LocalDate.MIN;
                    if (dateObj instanceof java.time.LocalDate) return (java.time.LocalDate) dateObj;
                    if (dateObj instanceof java.sql.Date) return ((java.sql.Date) dateObj).toLocalDate();
                    try {
                        return LocalDate.parse(dateObj.toString().substring(0, 10));
                    } catch (Exception e) {
                        return LocalDate.MIN;
                    }
                },
                s -> s,
                (v1, v2) -> v1
            ));

        List<CalendarDayDto> calendarDays = new ArrayList<>();
        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            Map<String, Object> dayData = statsMap.get(date);
            if (dayData != null) {
                Object countObj = dayData.get("count") != null ? dayData.get("count") : dayData.get("COUNT");
                Object accuracyObj = dayData.get("accuracy") != null ? dayData.get("accuracy") : dayData.get("ACCURACY");
                long count = (countObj != null) ? ((Number) countObj).longValue() : 0L;
                double accuracy = (accuracyObj != null) ? ((Number) accuracyObj).doubleValue() : 0.0;
                calendarDays.add(new CalendarDayDto(date, count, accuracy));
            } else {
                calendarDays.add(new CalendarDayDto(date, 0L, 0.0));
            }
        }
        return calendarDays;
    }

    /**
     * 特定の日の詳細データを取得
     */
    public DailyDetailDto getDailyDetail(Long userId, LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd (E)", Locale.JAPANESE));

        // ★修正: LocalDate（引数）を日本時間基準の開始/終了 LocalDateTime に変換
        // JVM が UTC の場合でも、これで「日本時間の 00:00〜23:59」を正確に指定できます
        LocalDateTime startOfDay = date.atStartOfDay(TOKYO_ZONE).toLocalDateTime();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX).atZone(TOKYO_ZONE).toLocalDateTime();
        
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
            accuracy = BigDecimal.valueOf(rawAccuracy).setScale(1, RoundingMode.HALF_UP).doubleValue();
            correctCount = Math.round(totalCount * (rawAccuracy / 100.0));
        }

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