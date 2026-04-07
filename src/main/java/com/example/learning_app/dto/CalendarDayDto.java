package com.example.learning_app.dto;

import java.time.LocalDate;

public class CalendarDayDto {
    private LocalDate date;
    private int dayOfMonth;
    private long answerCount;    // その日の解答数
    private double accuracyRate; // その日の正解率

    // コンストラクタ
    public CalendarDayDto(LocalDate date, long answerCount, double accuracyRate) {
        this.date = date;
        this.dayOfMonth = date.getDayOfMonth();
        this.answerCount = answerCount;
        this.accuracyRate = accuracyRate;
    }

    // Getterのみ（Thymeleafから参照用）
    public LocalDate getDate() { return date; }
    public int getDayOfMonth() { return dayOfMonth; }
    public long getAnswerCount() { return answerCount; }
    public double getAccuracyRate() { return accuracyRate; }
}