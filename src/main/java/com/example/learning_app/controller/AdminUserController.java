package com.example.learning_app.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.learning_app.entity.Users;
import com.example.learning_app.repository.UsersRepository;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(UsersRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 一覧表示
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users/list";
    }

    // 新規登録フォーム
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new Users());
        return "admin/users/form";
    }

    // 登録処理
    @PostMapping("/new")
    public String createUser(@ModelAttribute Users user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    // 編集フォーム
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Users user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "admin/users/form";
    }

    // 更新処理
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute Users updatedUser) {
        Users user = userRepository.findById(id).orElseThrow();
        user.setUsername(updatedUser.getUsername());
        user.setRole(updatedUser.getRole());

        if (!updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        userRepository.save(user);
        return "redirect:/admin/users";
    }

    // 削除
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }
}