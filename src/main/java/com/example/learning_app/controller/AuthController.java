package com.example.learning_app.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.learning_app.dto.UserRegistrationDto;
import com.example.learning_app.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") UserRegistrationDto userDto, 
            BindingResult result, Model model) {
        if (result.hasErrors()) return "auth/register"; // 入力ミスを即座に返す

        try {
            userService.registerNewUser(userDto);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", "登録に失敗しました");
            return "auth/register";
        }
    }
}