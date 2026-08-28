package com.example.learning_app.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.learning_app.entity.Users;
import com.example.learning_app.repository.ScoreRepository;
import com.example.learning_app.repository.UserAnswerRepository;
import com.example.learning_app.repository.UserMockExamRepository;
import com.example.learning_app.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor // コンストラクタを自動生成（Lombok使用）
public class AdminUserController {

    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAnswerRepository userAnswerRepository;
    private final ScoreRepository scoreRepository;
    private final UserMockExamRepository userMockExamRepository; 

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
    @Transactional
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
    @Transactional
    public String updateUser(@PathVariable Long id, @ModelAttribute Users updatedUser) {
        Users user = userRepository.findById(id).orElseThrow();
        user.setUsername(updatedUser.getUsername());
        user.setRole(updatedUser.getRole());

        // パスワードが入力されている場合のみ更新
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        userRepository.save(user);
        return "redirect:/admin/users";
    }

    // 削除処理（関連データをすべて消してからユーザーを消す）
    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteUser(@PathVariable Long id) {
        Users user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. 模擬試験に紐づく回答履歴を削除
        userAnswerRepository.deleteByUserMockExamUserId(id);

        // 2. 直接紐づく回答履歴があればそれも削除（念のため）
        userAnswerRepository.deleteDirectByUserId(id);

        // 3. 模擬試験履歴 (user_mock_exams) を削除
        userMockExamRepository.deleteByUserId(id);

        // 4. スコア情報の削除 (scores)
        scoreRepository.deleteByUserId(id);

        // 5. 最後にユーザー本体を削除
        userRepository.delete(user);;

        return "redirect:/admin/users";
    }
}
