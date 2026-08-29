// Tailwindのカスタム設定 (他の画面と共通化)
tailwind.config = {
    theme: {
        extend: {
            colors: {
                'primary-blue': '#1c55c8', // ボタンやアクセント用の青
                'primary-blue-hover': '#1747a5',
                'success-green': '#10b981', // 保存ボタンの緑
                'gray-button': '#6b7280',   // 戻るボタンの灰色
            },
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
            },
        }
    }
}
