package com.example.learning_app;

import java.util.TimeZone;

import jakarta.annotation.PostConstruct; // 追加

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LearningAppApplication {

    // アプリケーション全体のデフォルトタイムゾーンをJSTに設定
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
    }

    public static void main(String[] args) {
        SpringApplication.run(LearningAppApplication.class, args);
    }
}