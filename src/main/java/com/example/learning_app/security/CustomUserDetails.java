package com.example.learning_app.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.learning_app.entity.Users;

import lombok.Getter;

/**
 * Spring Security が認証後に保持するユーザー情報クラス
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Users user; // ← DBエンティティをそのまま持つ

    public CustomUserDetails(Users user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // 以下は常に true でOK（必要なら制御可能）
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /** UsersエンティティのIDなどを取得するためのヘルパー */
    public Long getId() {
        return user.getId();
    }
}