package com.example.learning_app.dto;

import java.util.List;

/**
 * 特定の日の学習詳細情報を保持するDTO
 */
public class DailyDetailDto {
    private String formattedDate; // "2026/03/26 (木)"
    private long totalCount;      // 回答数
    private long correctCount;    // 正解数
    private double accuracy;      // 正解率
    private List<MockResultDto> mockExams; // 模擬試験の結果リスト

    // コンストラクタ
    public DailyDetailDto() {}

    public DailyDetailDto(String formattedDate, long totalCount, long correctCount, double accuracy, List<MockResultDto> mockExams) {
        this.formattedDate = formattedDate;
        this.totalCount = totalCount;
        this.correctCount = correctCount;
        this.accuracy = accuracy;
        this.mockExams = mockExams;
    }

    // Getters and Setters
    public String getFormattedDate() { return formattedDate; }
    public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }

    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

    public long getCorrectCount() { return correctCount; }
    public void setCorrectCount(long correctCount) { this.correctCount = correctCount; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public List<MockResultDto> getMockExams() { return mockExams; }
    public void setMockExams(List<MockResultDto> mockExams) { this.mockExams = mockExams; }

    /**
     * 模擬試験の簡易結果用内部クラス
     */
    public static class MockResultDto {
        private String name;
        private int score;       // 正答数
        private int totalQuestions; // 総問題数
        private boolean passed;  // 合格フラグ

        public MockResultDto(String name, int score, int totalQuestions, boolean passed) {
            this.name = name;
            this.score = score;
            this.totalQuestions = totalQuestions;
            this.passed = passed;
        }

        public String getName() { return name; }
        public int getScore() { return score; }
        public int getTotalQuestions() { return totalQuestions; }
        public boolean isPassed() { return passed; }
    }
}