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

    public List<CalendarDayDto> getCalendarData(Long userId, YearMonth yearMonth) {
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        
        // 日本時間の00:00:00から23:59:59までを指定
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX);

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

    public DailyDetailDto getDailyDetail(Long userId, LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd (E)", Locale.JAPANESE));

        // 検索条件を日本時間の範囲に設定
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
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