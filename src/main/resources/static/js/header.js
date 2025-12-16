document.addEventListener('alpine:init', () => {
    Alpine.data('headerController', () => ({
        menuOpen: false,

        toggleMenu() {
            this.menuOpen = !this.menuOpen;
        }
    }));
});
