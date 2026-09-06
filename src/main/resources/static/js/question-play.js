document.addEventListener('DOMContentLoaded', function() {
    // シンタックスハイライトの実行
    if (window.hljs) {
        hljs.highlightAll();
    }

    // 選択肢のどこをクリックしてもラジオボタンが選択されるようにする
    document.querySelectorAll('.choice-container').forEach(container => {
        container.addEventListener('click', function() {
            const radio = this.querySelector('input[type="radio"]');
            if (radio) { 
                radio.checked = true; 
            }
        });
    });
});
