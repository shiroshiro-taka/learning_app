// --- 選択肢を削除する ---
function removeChoice(button) {
    button.closest('.choice-row').remove();
    refreshChoiceIndexes();
}

// --- 新しい選択肢を追加する ---
function addChoice() {
    const container = document.getElementById('choices-container');
    const index = container.children.length;

    const div = document.createElement('div');
    div.classList.add('choice-row', 'flex', 'items-start', 'gap-3');
    div.innerHTML = `
        <textarea name="choices[${index}].choiceText" 
                  class="choice-text-area flex-1 mt-1 block w-full px-4 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-primary-blue focus:border-primary-blue sm:text-sm" 
                  placeholder="選択肢を入力"></textarea>
        <button type="button" 
                class="delete-choice-btn flex-shrink-0 mt-1 px-3 py-2 text-sm font-semibold bg-error-red text-white rounded-lg shadow-md hover:bg-red-700 transition duration-200" 
                onclick="removeChoice(this)">削除</button>
    `;
    container.appendChild(div);
    refreshChoiceIndexes();
}

// --- 選択肢削除・追加後に正答リストを更新 ---
function refreshChoiceIndexes() {
    const container = document.getElementById('choices-container');
    const select = document.getElementById('correct-choice-select');
    
    // 現在選択されている値を保持
    const currentSelectedValue = select.value;

    select.innerHTML = '<option value="">選択してください</option>';

    let newIndexToSelect = -1;

    Array.from(container.children).forEach((row, index) => {
        // name属性を再設定 (配列インデックスを修正)
        const textarea = row.querySelector('textarea');
        textarea.name = `choices[${index}].choiceText`;

        // 正答セレクトの再構築
        const option = document.createElement('option');
        option.value = index;
        option.textContent = `選択肢 ${index + 1}`;
        select.appendChild(option);

        // 以前選択されていた値と同じインデックスがあるかチェック
        if (currentSelectedValue !== "" && parseInt(currentSelectedValue) === index) {
            newIndexToSelect = index;
        }
    });
    
    // 以前選択されていた値を再設定（または最も近いもの）
    if (newIndexToSelect !== -1) {
        select.value = newIndexToSelect;
    } else if (currentSelectedValue !== "") {
        // 削除などによって以前の値が失われた場合、デフォルトに戻す
        select.value = "";
    }
}
