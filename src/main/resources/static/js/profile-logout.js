(function () {
  const triggers = Array.from(document.querySelectorAll('#logoutButton, .logout-action, a[href="/auth/logout"]'));
  const modal = document.getElementById('logoutModal');
  const cancelBtn = document.getElementById('cancelLogout');
  const confirmBtn = document.getElementById('confirmLogout');
  if (!triggers.length || !modal || !cancelBtn || !confirmBtn) return;

  const open = () => {
    modal.classList.remove('hidden');
    requestAnimationFrame(() => modal.classList.add('active'));
  };
  const close = () => {
    modal.classList.remove('active');
    setTimeout(() => modal.classList.add('hidden'), 180);
  };

  triggers.forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      open();
    });
  });
  cancelBtn.addEventListener('click', (e) => { e.preventDefault(); close(); });
  confirmBtn.addEventListener('click', (e) => {
    e.preventDefault();
    window.location.href = confirmBtn.getAttribute('href') || '/auth/logout';
  });
  modal.addEventListener('click', (e) => { if (e.target === modal) close(); });
  document.addEventListener('keyup', (e) => { if (e.key === 'Escape') close(); });
})();
