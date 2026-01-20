package com.example.learning_app.controller;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.learning_app.entity.HomeComment;
import com.example.learning_app.repository.HomeCommentRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/home-comment")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class HomeCommentAdminController {

    private final HomeCommentRepository commentRepository;

    @GetMapping
    public String editCommentForm(Model model) {
        // DBに一つしかないことを前提とする場合
        HomeComment comment = commentRepository.findAll().stream()
                .findFirst()
                .orElse(new HomeComment());
        model.addAttribute("homeComment", comment);
        return "admin/home-comment-edit";
    }

    @PostMapping
    public String updateComment(@Valid @ModelAttribute HomeComment homeComment, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/home-comment-edit";
        }
        // IDの有無に関わらず、常に最初の1件を更新するなどの制御をService層で行うのが理想
        commentRepository.save(homeComment);
        return "redirect:/home";
    }
}