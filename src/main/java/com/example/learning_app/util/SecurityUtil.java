package com.example.learning_app.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Securityのコンテキストから認証情報を取得するためのユーティリティクラス。
 */
public class SecurityUtil {

    private SecurityUtil() {
        // インスタンス化防止
    }

    /**
     * 現在ログインしているユーザーのユーザー名（通常はログインID）を取得する。
     * 認証されていない場合は null を返す。
     * @return ユーザー名
     */
    public static String getCurrentUsername() {
        // SecurityContextHolderから現在のAuthenticationオブジェクトを取得
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 認証情報が存在しない、または「匿名ユーザー」の場合はnullを返す
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null; 
        }
        
        // ユーザー名を取得して返す
        return authentication.getName();
    }
}