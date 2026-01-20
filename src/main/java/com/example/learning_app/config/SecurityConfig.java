package com.example.learning_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.learning_app.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // DaoAuthenticationProvider の明示的な定義は、特殊なカスタマイズがなければ不要です。
    // Spring Security が自動的に UserDetailsService と PasswordEncoder を紐付けます。

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/login", "/css/**", "/js/**", "/images/**").permitAll() // 画像パスも必要なら追加
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error") // ログイン失敗時の遷移先を明記
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // ログアウトパス
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true) // セッションを無効化
                .deleteCookies("JSESSIONID") // クッキーを削除
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession() // セッション固定攻撃対策
                .maximumSessions(1) // 同時ログイン制限（必要に応じて）
            );

        return http.build();
    }
}