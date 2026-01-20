package com.example.learning_app.controller;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.learning_app.entity.Inquiry;
import com.example.learning_app.repository.InquiryRepository;
import com.example.learning_app.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/inquiry")
@RequiredArgsConstructor // ★Lombokでコンストラクタ注入
public class InquiryController {

    private final InquiryRepository inquiryRepository;

    @GetMapping("/form")
    public String showForm(Model model, @AuthenticationPrincipal CustomUserDetails user) {
        Inquiry inquiry = new Inquiry();
        inquiry.setUsername(user.getUsername());
        model.addAttribute("inquiry", inquiry);
        return "inquiry_form";
    }

    @PostMapping("/submit")
    public String submit(@Valid @ModelAttribute Inquiry inquiry, // ★@Validを追加
                         BindingResult result,                  // ★検証結果
                         @AuthenticationPrincipal CustomUserDetails user,
                         Model model) {
        
        // 1. ユーザー情報の強制セット（改ざん防止）
        inquiry.setUsername(user.getUsername());

        // 2. 手動バリデーション（返信希望チェック）
        if (inquiry.isReplyWanted() && (inquiry.getEmail() == null || inquiry.getEmail().isBlank())) {
            result.rejectValue("email", "error.email", "返信希望の場合はメールアドレスを入力してください。");
        }
        
        // 3. 全体のバリデーションエラー確認
        if (result.hasErrors()) {
            return "inquiry_form";
        }
        
        inquiryRepository.save(inquiry);
        model.addAttribute("message", "お問い合わせを送信しました。");
        return "inquiry_result";
    }
}