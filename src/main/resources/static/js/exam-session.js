let totalSeconds = duration * 60;
let currentQuestionIndex = 0;
let answerStatus = {};

function updateTimer() {
    let m = Math.floor(totalSeconds / 60);
    let s = totalSeconds % 60;
    $('#timer').text(`${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`);
    if (totalSeconds <= 0) $('#finishForm').submit();
    else totalSeconds--;
}

function showQuestion(index) {
    index = parseInt(index);
    if (index < 0 || index >= questionsCount) return;
    
    currentQuestionIndex = index;
    $('.question-block').addClass('hidden');
    $('.question-block').eq(index).removeClass('hidden');
    
    if (window.MathJax) {
        MathJax.typesetPromise();
    }

    $('#prev').prop('disabled', index === 0);
    $('#next').prop('disabled', index === questionsCount - 1);
    
    $('.q-status-btn').removeClass('status-current');
    let btn = $('#status-btn-' + index);
    btn.addClass('status-current');
    
    let container = $('#status-scroll-area');
    container.animate({ scrollLeft: btn.position().left + container.scrollLeft() - (container.width()/2) }, 200);

    restoreState(questionsData[index].id);
    $('main').scrollTop(0);
}

function selectChoice(qId, cId) {
    $(`#q-${qId} .choice-item`).removeClass('selected-choice');
    $(`#choice-box-${cId}`).addClass('selected-choice');
    $(`#radio-${cId}`).prop('checked', true);
    
    let isReview = $(`#review-${qId}`).is(':checked');
    sendAjax(qId, cId, isReview);
}

function restoreState(qId) {
    let s = answerStatus[qId] || {};
    $(`#q-${qId} .choice-item`).removeClass('selected-choice');
    if (s.choiceId) {
        $(`#choice-box-${s.choiceId}`).addClass('selected-choice');
        $(`#radio-${s.choiceId}`).prop('checked', true);
    }
    $(`#review-${qId}`).prop('checked', !!s.review);
}

function sendAjax(qId, cId, isReview, isOnlyToggleReview = false) {
    $.ajax({
        url: '/exam/answer',
        type: 'POST',
        data: { userExamId, questionId: qId, choiceId: cId, reviewFlag: isReview },
        beforeSend: function(xhr) { xhr.setRequestHeader('X-CSRF-TOKEN', csrfToken); },
        success: function() {
            if (!answerStatus[qId]) answerStatus[qId] = {};
            if (!isOnlyToggleReview) {
                answerStatus[qId].answered = !!cId;
                answerStatus[qId].choiceId = cId;
            }
            answerStatus[qId].review = isReview;
            updateStatusUI(qId);
        }
    });
}

function updateStatusUI(qId) {
    let idx = questionsData.findIndex(d => d.id == qId);
    let s = answerStatus[qId];
    let btn = $('#status-btn-' + idx);
    btn.toggleClass('status-answered', !!s.answered);
    btn.toggleClass('status-review', !!s.review);
}

$(document).on('change', '.review-check', function() {
    let qId = $(this).closest('.question-block').data('question-id');
    let isReview = $(this).is(':checked');
    let cId = $(`#q-${qId} input:checked`).val() || null;
    sendAjax(qId, cId, isReview, true);
});

$('#prev').click(() => showQuestion(currentQuestionIndex - 1));
$('#next').click(() => showQuestion(currentQuestionIndex + 1));

function goToReview() {
    if (mockExamId) window.location.href = '/exam/review/' + mockExamId;
}

function confirmFinish() {
    if (confirm("試験を終了して提出しますか？")) $('#finishForm').submit();
}

$(document).ready(function() {
    existingAnswers.forEach(a => {
        answerStatus[a.questionId] = { answered: !!a.selectedChoiceId, review: a.reviewFlag, choiceId: a.selectedChoiceId };
        updateStatusUI(a.questionId);
    });
    setInterval(updateTimer, 1000);
    showQuestion(0);
    hljs.highlightAll();
});
