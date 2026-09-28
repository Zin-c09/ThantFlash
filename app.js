'use strict';

// ---------- storage ----------
const KEY_CARDS = 'thantflash.cards';
const KEY_REMINDERS = 'thantflash.reminders';
const MIN = 60 * 1000;
const DAY = 24 * 60 * MIN;

function load(key, fallback) {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch {
    return fallback;
  }
}
function save(key, value) {
  try { localStorage.setItem(key, JSON.stringify(value)); } catch { /* storage blocked */ }
}
const uid = () => Date.now().toString(36) + Math.random().toString(36).slice(2, 7);

const SAMPLE_CARDS = [
  ['English', 'Hello', 'မင်္ဂလာပါ'],
  ['English', 'Thank you', 'ကျေးဇူးတင်ပါတယ်'],
  ['English', 'Remember', 'မှတ်မိသည်'],
  ['General', 'မြန်မာနိုင်ငံ၏ မြို့တော်', 'နေပြည်တော်'],
].map(([deck, front, back]) => newCard(deck, front, back));

let cards = load(KEY_CARDS, null) ?? SAMPLE_CARDS;
let reminders = load(KEY_REMINDERS, []);
save(KEY_CARDS, cards);

function newCard(deck, front, back) {
  return { id: uid(), deck, front, back, interval: 0, ease: 2.5, due: Date.now(), reps: 0 };
}

// ---------- helpers ----------
const $ = (sel) => document.querySelector(sel);
const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
}[c]));

function toast(msg) {
  const t = $('#toast');
  t.textContent = msg;
  t.classList.add('show');
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => t.classList.remove('show'), 2500);
}

function fmtDate(ms) {
  return new Date(ms).toLocaleString(undefined, {
    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

function decks() {
  return [...new Set(cards.map((c) => c.deck))].sort();
}

// ---------- tabs ----------
document.querySelectorAll('.tab').forEach((btn) => {
  btn.addEventListener('click', () => showTab(btn.dataset.tab));
});
function showTab(name) {
  document.querySelectorAll('.tab').forEach((b) => b.classList.toggle('active', b.dataset.tab === name));
  document.querySelectorAll('.panel').forEach((p) => p.classList.toggle('active', p.id === name));
  if (name === 'study') startStudy();
}

// ---------- study (spaced repetition) ----------
let queue = [];
let current = null;
let studyAll = false;

function renderDeckSelect() {
  const sel = $('#studyDeck');
  const prev = sel.value;
  sel.innerHTML = '<option value="">အားလုံး (All)</option>' +
    decks().map((d) => `<option>${esc(d)}</option>`).join('');
  sel.value = decks().includes(prev) ? prev : '';
  $('#deckList').innerHTML = decks().map((d) => `<option value="${esc(d)}">`).join('');
}

function startStudy() {
  const deck = $('#studyDeck').value;
  const now = Date.now();
  queue = cards
    .filter((c) => (!deck || c.deck === deck) && (studyAll || c.due <= now))
    .sort((a, b) => a.due - b.due);
  nextCard();
}

function nextCard() {
  current = queue.shift() || null;
  const flash = $('#flashcard');
  flash.classList.remove('flipped');
  $('#gradeButtons').classList.add('hidden');
  $('#studyArea').classList.toggle('hidden', !current);
  $('#studyEmpty').classList.toggle('hidden', !!current);
  if (current) {
    $('#cardFront').textContent = current.front;
    // wait for the flip-back animation before revealing the next answer
    setTimeout(() => { $('#cardBack').textContent = current ? current.back : ''; }, 250);
  } else {
    studyAll = false;
  }
  const deck = $('#studyDeck').value;
  const total = cards.filter((c) => !deck || c.deck === deck).length;
  $('#studyStats').textContent = `ကျန် ${queue.length + (current ? 1 : 0)} / စုစုပေါင်း ${total}`;
}

function flip() {
  if (!current) return;
  $('#flashcard').classList.toggle('flipped');
  $('#gradeButtons').classList.remove('hidden');
}

// grade: 0 again, 1 hard, 2 good, 3 easy (simplified SM-2)
function grade(g) {
  if (!current) return;
  const c = current;
  if (g === 0) {
    c.interval = 0;
    c.ease = Math.max(1.3, c.ease - 0.2);
    c.due = Date.now() + MIN;
    queue.push(c); // see it again this session
  } else {
    if (g === 1) {
      c.interval = Math.max(1, c.interval * 1.2);
      c.ease = Math.max(1.3, c.ease - 0.15);
    } else if (g === 2) {
      c.interval = c.interval ? c.interval * c.ease : 1;
    } else {
      c.interval = c.interval ? c.interval * c.ease * 1.3 : 3;
      c.ease += 0.15;
    }
    c.interval = Math.round(c.interval * 10) / 10;
    c.due = Date.now() + c.interval * DAY;
  }
  c.reps += 1;
  save(KEY_CARDS, cards);
  nextCard();
}

$('#flashcard').addEventListener('click', flip);
$('#gradeButtons').addEventListener('click', (e) => {
  const b = e.target.closest('[data-grade]');
  if (b) grade(Number(b.dataset.grade));
});
$('#studyDeck').addEventListener('change', startStudy);
$('#studyAll').addEventListener('click', () => { studyAll = true; startStudy(); });
document.addEventListener('keydown', (e) => {
  if (!$('#study').classList.contains('active') || e.target.matches('input, textarea, select')) return;
  if (e.code === 'Space' || e.key === 'Enter') { e.preventDefault(); flip(); }
  if ($('#flashcard').classList.contains('flipped') && ['1', '2', '3', '4'].includes(e.key)) {
    grade(Number(e.key) - 1);
  }
});

// ---------- card management ----------
function renderCards() {
  const q = $('#cardSearch').value.trim().toLowerCase();
  const list = cards.filter((c) => !q || `${c.deck} ${c.front} ${c.back}`.toLowerCase().includes(q));
  $('#cardList').innerHTML = list.length ? list.map((c) => `
    <li data-id="${c.id}">
      <div class="body">
        <div class="title">${esc(c.front)}</div>
        <div class="sub">${esc(c.back)}</div>
        <span class="tag">${esc(c.deck)}</span>
        <span class="sub"> · နောက်တစ်ကြိမ်: ${c.due <= Date.now() ? 'ယခု' : fmtDate(c.due)}</span>
      </div>
      <div class="actions">
        <button data-act="edit" title="ပြင်">✏️</button>
        <button data-act="del" title="ဖျက်">🗑️</button>
      </div>
    </li>`).join('') : '<li class="muted">ကတ် မရှိသေးပါ</li>';
  renderDeckSelect();
}

$('#cardForm').addEventListener('submit', (e) => {
  e.preventDefault();
  const id = $('#cardId').value;
  const deck = $('#cardDeck').value.trim();
  const front = $('#cardFrontIn').value.trim();
  const back = $('#cardBackIn').value.trim();
  if (!deck || !front || !back) return;
  if (id) {
    Object.assign(cards.find((c) => c.id === id), { deck, front, back });
    toast('ပြင်ပြီးပါပြီ ✅');
  } else {
    cards.push(newCard(deck, front, back));
    toast('ကတ်အသစ် ထည့်ပြီးပါပြီ ✅');
  }
  save(KEY_CARDS, cards);
  $('#cardForm').reset();
  $('#cardDeck').value = deck; // keep deck for quick entry
  $('#cardFrontIn').focus();
  renderCards();
});
$('#cardForm').addEventListener('reset', () => { $('#cardId').value = ''; });

$('#cardList').addEventListener('click', (e) => {
  const btn = e.target.closest('button[data-act]');
  if (!btn) return;
  const id = btn.closest('li').dataset.id;
  const card = cards.find((c) => c.id === id);
  if (btn.dataset.act === 'del') {
    if (!confirm(`"${card.front}" ကို ဖျက်မှာ သေချာလား?`)) return;
    cards = cards.filter((c) => c.id !== id);
    save(KEY_CARDS, cards);
    renderCards();
  } else {
    $('#cardId').value = card.id;
    $('#cardDeck').value = card.deck;
    $('#cardFrontIn').value = card.front;
    $('#cardBackIn').value = card.back;
    $('#cardFrontIn').focus();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
});
$('#cardSearch').addEventListener('input', renderCards);

$('#exportBtn').addEventListener('click', () => {
  const blob = new Blob([JSON.stringify({ cards, reminders }, null, 2)], { type: 'application/json' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = `thantflash-${new Date().toISOString().slice(0, 10)}.json`;
  a.click();
  URL.revokeObjectURL(a.href);
});
$('#importFile').addEventListener('change', async (e) => {
  const file = e.target.files[0];
  if (!file) return;
  try {
    const data = JSON.parse(await file.text());
    const known = new Set(cards.map((c) => c.id));
    const incoming = (Array.isArray(data) ? data : data.cards || [])
      .filter((c) => c && c.front && c.back && !known.has(c.id))
      .map((c) => ({ ...newCard(c.deck || 'Imported', c.front, c.back), ...c }));
    cards.push(...incoming);
    const knownR = new Set(reminders.map((r) => r.id));
    reminders.push(...(data.reminders || []).filter((r) => r && r.title && !knownR.has(r.id)));
    save(KEY_CARDS, cards);
    save(KEY_REMINDERS, reminders);
    renderCards();
    renderReminders();
    toast(`ကတ် ${incoming.length} ခု ထည့်ပြီးပါပြီ`);
  } catch {
    toast('ဖိုင် မမှန်ပါ ❌');
  }
  e.target.value = '';
});

// ---------- reminders ----------
function toLocalInput(ms) {
  const d = new Date(ms - new Date(ms).getTimezoneOffset() * MIN);
  return d.toISOString().slice(0, 16);
}

function calendarLink(r) {
  const fmt = (ms) => new Date(ms).toISOString().replace(/[-:]/g, '').replace(/\.\d{3}/, '');
  const params = new URLSearchParams({
    action: 'TEMPLATE',
    text: r.title,
    dates: `${fmt(r.time)}/${fmt(r.time + 15 * MIN)}`,
    details: 'ThantFlash reminder',
  });
  if (r.repeat !== 'none') params.set('recur', `RRULE:FREQ=${r.repeat.toUpperCase()}`);
  return `https://calendar.google.com/calendar/render?${params}`;
}

const REPEAT_LABEL = { none: 'တစ်ကြိမ်', daily: 'နေ့တိုင်း', weekly: 'အပတ်တိုင်း' };

function renderReminders() {
  const now = Date.now();
  const sorted = [...reminders].sort((a, b) => (a.done - b.done) || (a.time - b.time));
  $('#reminderList').innerHTML = sorted.length ? sorted.map((r) => `
    <li data-id="${r.id}" class="${r.done ? 'done' : ''} ${!r.done && r.time <= now ? 'due' : ''}">
      <div class="body">
        <div class="title">${r.done ? '✅ ' : '⏰ '}${esc(r.title)}</div>
        <div class="sub">${fmtDate(r.time)} · ${REPEAT_LABEL[r.repeat] || ''}</div>
      </div>
      <div class="actions">
        <a href="${calendarLink(r)}" target="_blank" rel="noopener" title="Google Calendar ထဲ ထည့်ရန်">📅</a>
        <button data-act="toggle" title="${r.done ? 'ပြန်ဖွင့်' : 'ပြီးပြီ'}">${r.done ? '↩️' : '✔️'}</button>
        <button data-act="del" title="ဖျက်">🗑️</button>
      </div>
    </li>`).join('') : '<li class="muted">သတိပေးချက် မရှိသေးပါ</li>';
}

$('#reminderForm').addEventListener('submit', (e) => {
  e.preventDefault();
  const time = new Date($('#remTime').value).getTime();
  if (Number.isNaN(time)) return;
  reminders.push({
    id: uid(),
    title: $('#remTitle').value.trim(),
    time,
    repeat: $('#remRepeat').value,
    done: false,
  });
  save(KEY_REMINDERS, reminders);
  $('#reminderForm').reset();
  $('#remTime').value = toLocalInput(Date.now() + 60 * MIN);
  renderReminders();
  toast('သတိပေးချက် ထည့်ပြီးပါပြီ ⏰');
  if ('Notification' in window && Notification.permission === 'default') Notification.requestPermission();
});

$('#reminderList').addEventListener('click', (e) => {
  const btn = e.target.closest('button[data-act]');
  if (!btn) return;
  const id = btn.closest('li').dataset.id;
  if (btn.dataset.act === 'del') {
    reminders = reminders.filter((r) => r.id !== id);
  } else {
    const r = reminders.find((x) => x.id === id);
    r.done = !r.done;
  }
  save(KEY_REMINDERS, reminders);
  renderReminders();
});

$('#notifyBtn').addEventListener('click', async () => {
  if (!('Notification' in window)) return toast('ဒီ browser မှာ Notification မရပါ');
  const p = await Notification.requestPermission();
  toast(p === 'granted' ? 'Notification ဖွင့်ပြီးပါပြီ 🔔' : 'Notification ခွင့်မပြုပါ');
});

function notify(title, body) {
  toast(`⏰ ${title}`);
  if ('Notification' in window && Notification.permission === 'granted') {
    try {
      new Notification(title, { body, icon: 'assets/logo.svg', tag: title });
    } catch { /* some mobile browsers require a service worker */ }
  }
  try {
    // short beep so the reminder is noticed even without notifications
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    osc.frequency.value = 880;
    osc.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.3);
  } catch { /* audio unavailable */ }
}

function checkReminders() {
  const now = Date.now();
  let changed = false;
  for (const r of reminders) {
    if (r.done || r.time > now) continue;
    notify(r.title, `ThantFlash · ${fmtDate(r.time)}`);
    if (r.repeat === 'none') {
      r.done = true;
    } else {
      const step = r.repeat === 'daily' ? DAY : 7 * DAY;
      while (r.time <= now) r.time += step;
    }
    changed = true;
  }
  // cards waiting for review count as a gentle reminder too
  const dueCards = cards.filter((c) => c.due <= now).length;
  document.title = dueCards ? `(${dueCards}) ThantFlash` : 'ThantFlash';
  if (changed) {
    save(KEY_REMINDERS, reminders);
    renderReminders();
  }
}

// ---------- init ----------
$('#year').textContent = new Date().getFullYear();
$('#remTime').value = toLocalInput(Date.now() + 60 * MIN);
renderCards();
renderReminders();
startStudy();
checkReminders();
setInterval(checkReminders, 20 * 1000);

if ('serviceWorker' in navigator && location.protocol !== 'file:') {
  navigator.serviceWorker.register('sw.js').catch(() => {});
}
