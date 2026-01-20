package com.example.learning_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.learning_app.repository.InquiryRepository;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/inquiries")
public class AdminInquiryController {

    @Autowired
    private InquiryRepository inquiryRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("inquiries", inquiryRepository.findAll());
        return "admin/inquiry_list";
    }
}
