package com.example.learning_app.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.learning_app.dto.DailyDetailDto;
import com.example.learning_app.security.CustomUserDetails; // 実際のクラス名に合わせて修正
import com.example.learning_app.service.CalendarService;

@RestController
public class ScoreRestController {

    @Autowired
    private CalendarService calendarService;

    /**
     * 特定の日の学習詳細データを取得する
     * @param date 対象日 (yyyy-MM-dd)
     * @param loginUser ログイン中のユーザー情報
     */
    @GetMapping("/api/scores/daily")
    public DailyDetailDto getDailyDetail(
            @RequestParam String date,
            @AuthenticationPrincipal CustomUserDetails loginUser
            ) {
        
        // CustomUserDetails の getId() メソッドを使用してログインユーザーのIDを取得
        // 未ログイン時は loginUser が null になる可能性があるため、必要に応じてチェックを追加してください
        if (loginUser == null) {
            return new DailyDetailDto(); // または適切なエラーレスポンス
        }
        
        Long userId = loginUser.getId(); 
        
        LocalDate targetDate = LocalDate.parse(date);
        
        // Serviceを呼び出して、ログインユーザーに紐づくデータを取得
        return calendarService.getDailyDetail(userId, targetDate);
    }
}