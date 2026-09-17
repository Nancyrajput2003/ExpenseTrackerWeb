// If a profile already exists and there's an active session, skip straight to the dashboard.
(async () => {
  const profile = await apiGet('/profile');
  if (profile.exists && sessionStorage.getItem(SESSION_KEY) === 'true') {
    window.location.href = 'dashboard.html';
  }
})();

document.getElementById('btnContinue').onclick = async () => {
  const name = document.getElementById('onbName').value.trim();
  const fullName = document.getElementById('onbFullName').value.trim();
  const email = document.getElementById('onbEmail').value.trim();
  const errorEl = document.getElementById('formError');
  errorEl.textContent = '';

  if (!fullName) {
    errorEl.textContent = 'Full name is required.';
    return;
  }

  const result = await apiPost('/profile', { name, fullName, email });
  if (result.error) {
    errorEl.textContent = result.error;
    return;
  }

  window.location.href = 'welcome.html';
};
