(async () => {
  const profile = await requireSession();
  if (!profile) return;
  await loadProfile(profile);
})();

async function loadProfile(profile) {
  const todayStat = await apiGet('/stats/today');

  document.getElementById('profileName').textContent = profile.name || '—';
  document.getElementById('profileFullName').textContent = profile.fullName || '—';
  document.getElementById('profileEmail').textContent = profile.email || '—';
  document.getElementById('profileDailyExpenses').textContent = money(todayStat.total);
  document.getElementById('profileIncomeInput').value = profile.monthlyIncome || '';
}

document.getElementById('navBackToDashboard').onclick = () => {
  window.location.href = 'dashboard.html';
};

document.getElementById('btnSaveIncome').onclick = async () => {
  const income = document.getElementById('profileIncomeInput').value.trim();
  if (income === '' || isNaN(parseFloat(income))) { alert('Enter a valid amount'); return; }
  await apiPost('/profile/income', { monthlyIncome: income });
  alert('Monthly income updated.');
};

document.getElementById('btnLogout').onclick = () => {
  sessionStorage.removeItem(SESSION_KEY);
  window.location.href = 'welcome.html';
};

document.getElementById('btnDeleteAccount').onclick = () => {
  openModal('deleteAccountModal');
};
document.getElementById('btnCancelDeleteAccount').onclick = () => closeModal('deleteAccountModal');
document.getElementById('btnConfirmDeleteAccount').onclick = async () => {
  await apiDelete('/profile');
  sessionStorage.removeItem(SESSION_KEY);
  closeModal('deleteAccountModal');
  window.location.href = 'login.html';
};
