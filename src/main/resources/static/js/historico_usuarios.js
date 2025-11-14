document.addEventListener("DOMContentLoaded", function () {
    const tableRows = document.querySelectorAll("tbody tr")
    tableRows.forEach(row => {
        row.addEventListener("mouseenter", () => row.classList.add("bg-gray-50"))
        row.addEventListener("mouseleave", () => row.classList.remove("bg-gray-50"))
    })
})
