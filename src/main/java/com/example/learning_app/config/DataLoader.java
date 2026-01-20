package com.example.learning_app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.learning_app.entity.Users;
import com.example.learning_app.repository.UsersRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UsersRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 管理ユーザーが存在しない場合のみ作成
        if (userRepository.findByUsername("admin").isEmpty()) {
            Users admin = new Users();
            admin.setUsername("admin");
            // パスワードをハッシュ化して保存
            admin.setPassword(passwordEncoder.encode("admin1234")); 
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            System.out.println("Admin user has been created.");
        }
    }
}