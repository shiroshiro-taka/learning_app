$(document).ready(function() {
    let sortDirection = {};

    function sortTable(colIndex) {
        const table = $("#weakTable");
        const tbody = table.find("tbody");
        const rows = tbody.find("tr").toArray();
        if (rows.length === 0) return;

        const asc = !sortDirection[colIndex];
        sortDirection[colIndex] = asc;

        table.find("th").removeClass("sort-asc sort-desc");
        table.find("th").eq(colIndex).addClass(asc ? "sort-asc" : "sort-desc");

        rows.sort((a, b) => {
            let A = $(a).find("td").eq(colIndex).text().replace('%','').trim();
            let B = $(b).find("td").eq(colIndex).text().replace('%','').trim();

            const isNumeric = colIndex !== 1 && colIndex !== 6;
            if (isNumeric) {
                A = parseFloat(A) || 0;
                B = parseFloat(B) || 0;
            }

            if (A < B) return asc ? -1 : 1;
            if (A > B) return asc ? 1 : -1;
            return 0;
        });

        $.each(rows, function(_, row) {
            tbody.append(row);
        });
    }

    $("#weakTable th").on("click", function() {
        const index = $(this).index();
        if (index < 6) sortTable(index);
    });

    if ($("#weakTable tbody tr").length > 0) {
        sortTable(0);
    }
});
