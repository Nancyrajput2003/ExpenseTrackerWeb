(async () => {
  const profile = await requireSession();
  if (!profile) return;
  await runReport();
})();

document.getElementById('navBackToDashboard').onclick = () => {
  window.location.href = 'dashboard.html';
};

document.getElementById('btnRunReport').onclick = runReport;

async function runReport() {
  const range = document.getElementById('reportRange').value;
  let totals, grandTotal;

  if (range === 'all') {
    const data = await apiGet('/categories');
    totals = data.totals || {};
    grandTotal = data.grandTotal || 0;
  } else {
    const now = new Date();
    let periodData;
    if (range === 'month') {
      periodData = await apiGet(`/expenses/period?year=${now.getFullYear()}&month=${now.getMonth() + 1}`);
    } else {
      periodData = await apiGet(`/expenses/period?year=${now.getFullYear()}`);
    }
    totals = {};
    for (const e of periodData.expenses) {
      totals[e.category] = (totals[e.category] || 0) + e.amount;
    }
    grandTotal = periodData.total || 0;
  }

  drawPieChart(totals);
  document.getElementById('pieTotal').textContent = grandTotal > 0 ? `Total: ${money(grandTotal)}` : '';
}

// ---------------------- Pie chart (plain Canvas, no libraries) ----------------------
const PIE_COLORS = ['#4F378B', '#03DAC5', '#D32F2F', '#F9A825', '#2E7D32', '#0288D1', '#C2185B', '#5D4037', '#7B1FA2', '#455A64'];

function drawPieChart(categoryTotals) {
  const canvas = document.getElementById('pieCanvas');
  const wrap = document.getElementById('pieChartWrap');
  const emptyEl = document.getElementById('pieEmptyState');
  const legendEl = document.getElementById('pieLegend');
  const ctx = canvas.getContext('2d');

  const entries = Object.entries(categoryTotals).filter(([, v]) => v > 0);
  const total = entries.reduce((sum, [, v]) => sum + v, 0);

  ctx.clearRect(0, 0, canvas.width, canvas.height);
  legendEl.innerHTML = '';

  if (entries.length === 0 || total <= 0) {
    wrap.classList.add('hidden');
    emptyEl.classList.remove('hidden');
    return;
  }
  wrap.classList.remove('hidden');
  emptyEl.classList.add('hidden');

  const cx = canvas.width / 2;
  const cy = canvas.height / 2;
  const radius = Math.min(cx, cy) - 6;

  let startAngle = -Math.PI / 2;
  entries.sort((a, b) => b[1] - a[1]);

  entries.forEach(([category, amount], i) => {
    const sliceAngle = (amount / total) * 2 * Math.PI;
    const color = PIE_COLORS[i % PIE_COLORS.length];

    ctx.beginPath();
    ctx.moveTo(cx, cy);
    ctx.arc(cx, cy, radius, startAngle, startAngle + sliceAngle);
    ctx.closePath();
    ctx.fillStyle = color;
    ctx.fill();

    startAngle += sliceAngle;

    const pct = ((amount / total) * 100).toFixed(1);
    const legendItem = document.createElement('div');
    legendItem.className = 'pie-legend-item';

    const swatch = document.createElement('span');
    swatch.className = 'pie-swatch';
    swatch.style.background = color;

    const labelText = document.createElement('span');
    labelText.textContent = `${category} — ${money(amount)} (${pct}%)`;

    legendItem.appendChild(swatch);
    legendItem.appendChild(labelText);
    legendEl.appendChild(legendItem);
  });
}
