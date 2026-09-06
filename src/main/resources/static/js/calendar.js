document.addEventListener('DOMContentLoaded', function() {
    const days = document.querySelectorAll('.clickable-day');
    
    /**
     * toISOString() を使用せず、Intl.DateTimeFormat で日本時間の yyyy-mm-dd を取得します。
     * これにより深夜0時〜9時の間の判定ズレを防ぎます。
     */
    const todayStr = new Intl.DateTimeFormat('sv-SE', { 
        timeZone: 'Asia/Tokyo' 
    }).format(new Date()); 

    let initialCell = null;

    days.forEach(day => {
        const dateStr = day.getAttribute('data-date');

        if (dateStr === todayStr) {
            day.classList.add('is-today');
            initialCell = day;
        }

        day.addEventListener('click', function() {
            days.forEach(d => d.classList.remove('selected-day'));
            this.classList.add('selected-day');
            const date = this.getAttribute('data-date');
            if (date) loadDailyDetail(date);
        });
    });

    if (initialCell) {
        initialCell.click();
    } else if (days.length > 0) {
        days[0].click();
    }
});

function loadDailyDetail(dateStr) {
    const detailArea = document.getElementById('detail-area');
    // HTML側でグローバル変数として定義された apiUrl を利用
    const apiUrl = window.dailyApiUrl || '/api/scores/daily';

    fetch(`${apiUrl}?date=${dateStr}`)
        .then(response => response.json())
        .then(data => {
            detailArea.classList.remove('d-none');
            document.getElementById('detail-date').innerText = data.formattedDate || dateStr;
            document.getElementById('detail-total').innerText = (data.totalCount || 0) + ' 問';
            document.getElementById('detail-correct').innerText = (data.correctCount || 0) + ' 問';
            document.getElementById('detail-accuracy').innerText = (data.accuracy || 0).toFixed(1) + ' %';

            const mockListContainer = document.getElementById('mock-exam-list');
            mockListContainer.innerHTML = ''; 

            if (data.mockExams && data.mockExams.length > 0) {
                data.mockExams.forEach(exam => {
                    const statusText = exam.passed ? '合格' : '不合格';
                    const statusClass = exam.passed ? 'badge-pass' : 'badge-fail';
                    const item = `
                        <div class="list-group-item d-flex justify-content-between align-items-center px-0 py-3">
                            <div>
                                <span class="fw-bold text-secondary">${exam.name}</span>
                                <span class="badge ${statusClass} ms-2">${statusText}</span>
                            </div>
                            <div class="badge bg-light text-dark border rounded-pill px-3">${exam.score} / ${exam.totalQuestions} 問</div>
                        </div>`;
                    mockListContainer.insertAdjacentHTML('beforeend', item);
                });
            } else {
                mockListContainer.innerHTML = '<p class="text-muted py-4 mb-0 text-center small">この日の模擬試験データはありません。</p>';
            }
            
            detailArea.classList.add('show');
        });
}
