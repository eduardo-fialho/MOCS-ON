document.addEventListener("DOMContentLoaded", function () {
    const menuButton = document.getElementById("menuButton");
    const menuDropdown = document.getElementById("menuDropdown");
    document.addEventListener("click", function (e) {
        if (menuButton.contains(e.target)) {
            menuDropdown.classList.toggle("hidden");
        } else if (!menuDropdown.contains(e.target)) {
            menuDropdown.classList.add("hidden");
        }
    });
});
