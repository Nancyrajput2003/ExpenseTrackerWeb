(async () => {
  const profile = await requireSession();
  if (!profile) return;
  loadDashboard();
})();

document.getElementById('navProfile').onclick = () => {
  window.location.href = 'profile.html';
};
document.getElementById('navReports').onclick = () => {
  window.location.href = 'reports.html';
};

// ---------------------- element refs ----------------------
const expenseListEl = document.getElementById('expenseList');
const emptyStateEl = document.getElementById('emptyState');
const totalSpentEl = document.getElementById('totalSpent');
const filterBannerEl = document.getElementById('filterBanner');

let pendingDeleteId = null;

function loadDashboard() {
  refreshList();
}

/** Scrolls the results card into view so the user sees results immediately, not at the bottom of the page. */
function scrollToResults() {
  document.getElementById('resultsCard').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function setFilterBanner(text) {
  if (!text) {
    filterBannerEl.classList.add('hidden');
    filterBannerEl.textContent = '';
  } else {
    filterBannerEl.classList.remove('hidden');
    filterBannerEl.textContent = text;
  }
}

function renderExpenses(expenses) {
  expenseListEl.innerHTML = '';
  emptyStateEl.classList.toggle('hidden', expenses.length !== 0);

  for (const e of expenses) {
    const item = document.createElement('div');
    item.className = 'expense-item';

    const info = document.createElement('div');
    info.className = 'expense-info';

    const title = document.createElement('div');
    title.className = 'expense-title';
    title.textContent = e.title;

    const meta = document.createElement('div');
    meta.className = 'expense-meta';
    meta.textContent = e.category + ' • ' + e.date;

    info.appendChild(title);
    info.appendChild(meta);

    if (e.note && e.note.trim() !== '') {
      const note = document.createElement('div');
      note.className = 'expense-note';
      note.textContent = e.note;
      info.appendChild(note);
    }

    const actions = document.createElement('div');
    actions.className = 'expense-actions';

    const amount = document.createElement('div');
    amount.className = 'expense-amount';
    amount.textContent = money(e.amount);

    const del = document.createElement('button');
    del.className = 'delete-icon';
    del.title = 'Delete';
    del.textContent = '🗑';
    del.onclick = () => confirmDelete(e.id, e.title);

    actions.appendChild(amount);
    actions.appendChild(del);

    item.appendChild(info);
    item.appendChild(actions);
    expenseListEl.appendChild(item);
  }
}

async function refreshList() {
  setFilterBanner(null);
  const [expenses, categories] = await Promise.all([
    apiGet('/expenses'),
    apiGet('/categories')
  ]);
  renderExpenses(expenses);
  totalSpentEl.textContent = money(categories.grandTotal || 0);
}

// ---------------------- Add Expense ----------------------
document.getElementById('btnAdd').onclick = () => {
  document.getElementById('inputTitle').value = '';
  document.getElementById('inputAmount').value = '';
  document.getElementById('inputCategory').value = '';
  document.getElementById('inputDate').value = new Date().toISOString().slice(0, 10);
  document.getElementById('inputNote').value = '';
  openModal('addModal');
};
document.getElementById('btnCancelAdd').onclick = () => closeModal('addModal');

document.getElementById('btnSaveAdd').onclick = async () => {
  const title = document.getElementById('inputTitle').value.trim();
  const amount = document.getElementById('inputAmount').value.trim();
  const category = document.getElementById('inputCategory').value.trim();
  const date = document.getElementById('inputDate').value;
  const note = document.getElementById('inputNote').value.trim();

  if (!title) { alert('Title is required'); return; }
  if (!amount || isNaN(parseFloat(amount))) { alert('Enter a valid amount'); return; }

  const result = await apiPost('/expenses', { title, amount, category, date, note });
  if (result.error) { alert(result.error); return; }

  closeModal('addModal');
  refreshList();
};

// ---------------------- Delete ----------------------
function confirmDelete(id, title) {
  pendingDeleteId = id;
  document.getElementById('deleteMessage').textContent =
    'This will remove "' + title + '" permanently.';
  openModal('deleteModal');
}
document.getElementById('btnCancelDelete').onclick = () => closeModal('deleteModal');
document.getElementById('btnConfirmDelete').onclick = async () => {
  if (pendingDeleteId != null) {
    await apiDelete('/expenses/' + pendingDeleteId);
    pendingDeleteId = null;
  }
  closeModal('deleteModal');
  refreshList();
};

// ---------------------- Sort ----------------------
document.getElementById('btnSort').onclick = async () => {
  const field = document.getElementById('sortField').value;
  const sorted = await apiPost(`/sort?field=${field}`);
  renderExpenses(sorted);
  scrollToResults();
};

// ---------------------- Search by keyword ----------------------
async function runKeywordSearch() {
  const keyword = document.getElementById('searchKeyword').value.trim();
  if (!keyword) return;
  const results = await apiGet('/search?keyword=' + encodeURIComponent(keyword));
  setFilterBanner(`Showing ${results.length} result(s) for "${keyword}"`);
  renderExpenses(results);
  scrollToResults();
}
document.getElementById('btnSearch').onclick = runKeywordSearch;
document.getElementById('searchKeyword').addEventListener('keydown', (e) => {
  if (e.key === 'Enter') runKeywordSearch();
});

// ---------------------- Search by exact date ----------------------
async function runDateSearch() {
  const date = document.getElementById('searchDate').value;
  if (!date) { alert('Pick a date first'); return; }
  const results = await apiGet('/search/date?date=' + date);
  setFilterBanner(`Showing ${results.length} result(s) on ${date}`);
  renderExpenses(results);
  scrollToResults();
}
document.getElementById('btnSearchDate').onclick = runDateSearch;
document.getElementById('searchDate').addEventListener('change', runDateSearch);

// ---------------------- Monthly / Yearly view ----------------------
const periodModeEl = document.getElementById('periodMode');
const periodMonthEl = document.getElementById('periodMonth');
const periodYearEl = document.getElementById('periodYear');

periodModeEl.onchange = () => {
  const mode = periodModeEl.value;
  periodMonthEl.classList.toggle('hidden-input', mode !== 'monthly');
  periodYearEl.classList.toggle('hidden-input', mode !== 'yearly');
};

document.getElementById('btnApplyPeriod').onclick = async () => {
  const mode = periodModeEl.value;
  if (mode === 'all') {
    refreshList();
    return;
  }
  if (mode === 'monthly') {
    const val = periodMonthEl.value; // "YYYY-MM"
    if (!val) { alert('Pick a month first'); return; }
    const [year, month] = val.split('-').map(Number);
    const data = await apiGet(`/expenses/period?year=${year}&month=${month}`);
    setFilterBanner(`Showing ${val} — total ${money(data.total)}`);
    renderExpenses(data.expenses);
    totalSpentEl.textContent = money(data.total);
    scrollToResults();
  } else if (mode === 'yearly') {
    const year = parseInt(periodYearEl.value, 10);
    if (!year) { alert('Enter a year first'); return; }
    const data = await apiGet(`/expenses/period?year=${year}`);
    setFilterBanner(`Showing ${year} — total ${money(data.total)}`);
    renderExpenses(data.expenses);
    totalSpentEl.textContent = money(data.total);
    scrollToResults();
  }
};

document.getElementById('btnClearFilter').onclick = () => {
  document.getElementById('searchKeyword').value = '';
  document.getElementById('searchDate').value = '';
  periodModeEl.value = 'all';
  periodMonthEl.value = '';
  periodYearEl.value = '';
  periodMonthEl.classList.add('hidden-input');
  periodYearEl.classList.add('hidden-input');
  refreshList();
};

// ---------------------- Category totals ----------------------
document.getElementById('btnCategories').onclick = async () => {
  const data = await apiGet('/categories');
  const entries = Object.entries(data.totals || {});
  let body;
  if (entries.length === 0) {
    body = 'No data yet.';
  } else {
    body = entries.map(([cat, amt]) => `${cat.padEnd(15)} ${money(amt)}`).join('\n');
    body += `\n\nGrand total: ${money(data.grandTotal)}`;
  }
  showInfo('Category-wise Totals', body);
};

// ---------------------- Top expenses ----------------------
document.getElementById('btnTop').onclick = async () => {
  const n = prompt('How many top expenses to show?', '3');
  if (n === null) return;
  const count = parseInt(n, 10) || 3;
  const results = await apiGet('/top?n=' + count);
  const body = results.length === 0
    ? 'No expenses yet.'
    : results.map(e => `${e.title} — ${money(e.amount)}\n${e.category} • ${e.date}`).join('\n\n');
  showInfo(`Top ${results.length} Biggest Expenses`, body);
};

// ---------------------- Activity log ----------------------
document.getElementById('btnActivity').onclick = async () => {
  const log = await apiGet('/activity?limit=15');
  const body = log.length === 0 ? 'No activity yet.' : log.join('\n\n');
  showInfo('Recent Activity', body);
};

function showInfo(title, body) {
  document.getElementById('infoTitle').textContent = title;
  document.getElementById('infoBody').textContent = body;
  openModal('infoModal');
}
document.getElementById('btnCloseInfo').onclick = () => closeModal('infoModal');

// ---------------------- Reminders ----------------------
document.getElementById('btnReminders').onclick = () => {
  document.getElementById('reminderText').value = '';
  document.getElementById('reminderResult').textContent = '';
  openModal('reminderModal');
};
document.getElementById('btnCloseReminder').onclick = () => closeModal('reminderModal');

document.getElementById('btnScheduleReminder').onclick = async () => {
  const text = document.getElementById('reminderText').value.trim();
  if (!text) return;
  const result = await apiPost('/reminders', { text });
  document.getElementById('reminderResult').textContent = result.queued
    ? 'Reminder queued: "' + text + '"'
    : (result.error || 'Failed to queue reminder.');
  document.getElementById('reminderText').value = '';
};

document.getElementById('btnProcessReminder').onclick = async () => {
  const result = await apiPost('/reminders/process');
  document.getElementById('reminderResult').textContent = result.reminder
    ? 'Processing: ' + result.reminder
    : 'No pending reminders.';
};

document.getElementById('btnReminderCount').onclick = async () => {
  const result = await apiGet('/reminders/count');
  document.getElementById('reminderResult').textContent = 'Pending reminders: ' + result.count;
};
