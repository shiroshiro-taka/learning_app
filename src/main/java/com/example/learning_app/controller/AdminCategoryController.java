package com.example.learning_app.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.learning_app.entity.Category;
import com.example.learning_app.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/categories/index";
    }

    @GetMapping("/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    @PostMapping
    // 1. @Valid を追加して入力をチェック。結果を BindingResult で受ける
    public String create(@Valid @ModelAttribute Category category, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/form"; // エラーがあれば入力画面に戻す
        }
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        // 2. orElseThrow の中身を指定して、存在しないIDへのアクセスに対処
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        model.addAttribute("category", category);
        return "admin/categories/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute Category category, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/form";
        }
        // 3. パスで指定されたIDを確実にセットして保存
        category.setId(id);
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        // 存在確認をしてから削除するとなお安全
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        categoryRepository.deleteById(id);
        return "redirect:/admin/categories";
    }
}