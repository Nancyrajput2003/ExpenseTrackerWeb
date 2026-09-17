(async () => {
  const profile = await requireProfile();
  if (!profile) return;

  document.getElementById('welcomeMessage').textContent =
    `Welcome, ${profile.fullName}! Manage your daily expenses with Expense Tracker.`;
})();

document.getElementById('btnEnterDashboard').onclick = () => {
  sessionStorage.setItem(SESSION_KEY, 'true');
  window.location.href = 'dashboard.html';
};
