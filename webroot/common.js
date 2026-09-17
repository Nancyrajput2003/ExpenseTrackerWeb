const API = '/api';
const SESSION_KEY = 'etw_session_active';

// ---------------------- generic helpers ----------------------
function money(n) {
  return 'Rs. ' + Number(n || 0).toFixed(2);
}
function openModal(id) { document.getElementById(id).classList.remove('hidden'); }
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }

async function apiGet(path) {
  const res = await fetch(API + path);
  return res.json();
}
async function apiPost(path, body) {
  const res = await fetch(API + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined
  });
  return res.json();
}
async function apiDelete(path) {
  const res = await fetch(API + path, { method: 'DELETE' });
  return res.json();
}

/**
 * Every protected page (welcome/dashboard/profile) calls this first.
 * If no profile has been created yet, bounce back to login.html.
 * Returns the profile object so the caller doesn't need a second fetch.
 */
async function requireProfile() {
  const profile = await apiGet('/profile');
  if (!profile.exists) {
    window.location.href = 'login.html';
    return null;
  }
  return profile;
}

/** Dashboard/profile pages also require an active session (post-login/welcome). */
async function requireSession() {
  const profile = await requireProfile();
  if (!profile) return null;
  if (sessionStorage.getItem(SESSION_KEY) !== 'true') {
    window.location.href = 'welcome.html';
    return null;
  }
  return profile;
}
