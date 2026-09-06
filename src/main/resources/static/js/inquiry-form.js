/**
 * 「返信希望」選択によってメール必須を切り替える
 */
function toggleEmailRequired() {
    const replyYes = document.getElementById('replyYes');
    const emailInput = document.getElementById('email');
        
    if (replyYes) {
        if (replyYes.checked) {
            emailInput.required = true;
            emailInput.placeholder = "返信をご希望の場合は必須です";
        } else {
            emailInput.required = false;
            emailInput.placeholder = "example@example.com (任意)";
        }
    } else {
        emailInput.placeholder = "example@example.com (任意)";
    }
}

// ページロード時に実行
document.addEventListener('DOMContentLoaded', toggleEmailRequired);
