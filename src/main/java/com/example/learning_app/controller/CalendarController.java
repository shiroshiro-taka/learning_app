package com.example.learning_app.controller;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.learning_app.dto.CalendarDayDto;
import com.example.learning_app.security.CustomUserDetails;
import com.example.learning_app.service.CalendarService;

@Controller
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    /**
     * 学習カレンダー画面を表示する
     */
    @GetMapping("/scores/calendar")
    public String showCalendar(
            @RequestParam(required = false) String month,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        // 1. 対象となる年月を決定
        YearMonth targetMonth;
        try {
            targetMonth = (month != null) ? YearMonth.parse(month) : YearMonth.now();
        } catch (Exception e) {
            targetMonth = YearMonth.now();
        }

        // 2. ログインユーザーのIDを正しく取得
        if (userDetails == null) {
            return "redirect:/login";
        }
        Long userId = userDetails.getId(); 

        // 3. Serviceからカレンダー用データを取得
        List<CalendarDayDto> calendarDays = calendarService.getCalendarData(userId, targetMonth);

        // 4. カレンダーの開始位置の空白計算
        int firstDayOfWeek = targetMonth.atDay(1).getDayOfWeek().getValue(); // 1(Mon) to 7(Sun)
        int paddingCount = (firstDayOfWeek == 7) ? 0 : firstDayOfWeek; 
        List<Integer> paddingDays = IntStream.range(0, paddingCount)
                                            .boxed()
                                            .collect(Collectors.toList());

        // 5. モデルへの追加
        model.addAttribute("dayOfWeekNames", Arrays.asList("日", "月", "火", "水", "木", "金", "土"));
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("paddingDays", paddingDays);
        model.addAttribute("currentMonthName", targetMonth.getYear() + "年 " + targetMonth.getMonthValue() + "月");
        model.addAttribute("prevMonth", targetMonth.minusMonths(1).toString());
        model.addAttribute("nextMonth", targetMonth.plusMonths(1).toString());

        return "scores/calendar";
    }
}