package com.example.learning_app.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.learning_app.repository.HomeCommentRepository;
import com.example.learning_app.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // コンストラクタ注入のため
public class HomeController {

    private final HomeCommentRepository homeCommentRepository;

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("userId", userDetails.getId());

        // Streamを使わず、RepositoryにfindFirstメソッドを作るか、Optionalで受けるのがスマート
        homeCommentRepository.findAll().stream()
                .findFirst()
                .ifPresent(comment -> model.addAttribute("homeComment", comment.getContent()));
        
        return "home";
    }
}