// Browser prototype: same JWT API as Android, with role menus (Microsoft, 2025b; MDN, 2025).
const app = document.getElementById("app");
const state = { user: JSON.parse(localStorage.getItem("reslink.user") || "null"), page: "home", filter: "All", tab: "overview", menu: false, more: false, auth: "signin" };
let sessionNotice = "";

const navByRole = {
  Student: [
    ["home", "Dashboard"], ["community", "Community"], ["events", "Events"], ["market", "Marketplace"],
    ["groups", "Study Groups"], ["maintenance", "Maintenance"], ["security", "Security"], ["rewards", "Rewards"], ["profile", "Profile"]
  ],
  Admin: [
    ["home", "Dashboard"], ["community", "Community"], ["events", "Events"], ["market", "Marketplace"],
    ["groups", "Study Groups"], ["maintenance", "Maintenance"], ["security", "Security"], ["rewards", "Rewards"],
    ["admin", "Admin Panel"], ["profile", "Profile"]
  ],
  Security: [
    ["home", "Dashboard"], ["security", "Security"], ["notifications", "Notifications"], ["profile", "Profile"]
  ],
  Maintenance: [
    ["home", "Dashboard"], ["maintenance", "Maintenance"], ["notifications", "Notifications"], ["profile", "Profile"]
  ]
};
const navSection = { home: "Overview", community: "Community", events: "Community", market: "Community", groups: "Community", maintenance: "Services", security: "Services", rewards: "Services", notifications: "Services", admin: "Manage", profile: "Account" };
const shortLabel = { home: "Home", community: "Feed", events: "Events", market: "Market", maintenance: "Repairs", security: "Security", notifications: "Alerts", profile: "Profile", groups: "Groups", rewards: "Rewards", admin: "Admin" };
// Each feature keeps one tint so it is recognisable on every screen.
const featureTint = { home: "brand", community: "leaf", events: "sky", market: "plum", groups: "leaf", maintenance: "gold", security: "rose", rewards: "gold", admin: "slate", notifications: "sky", profile: "leaf" };

// Stroke icons share one weight so the chrome does not look illustrated (Material Design, 2025).
function icon(name) {
  const paths = {
    home: '<path d="M4 11.5 12 4l8 7.5"/><path d="M6.5 10.5V20h11V10.5"/><path d="M10 20v-5h4v5"/>',
    community: '<path d="M5 6.5h14v8.5H9l-4 3.5z"/><path d="M9 10.5h6"/>',
    events: '<rect x="4" y="5" width="16" height="15" rx="2.5"/><path d="M8 3.5V7M16 3.5V7M4 10h16"/>',
    market: '<path d="M5.5 8h13l-1 12h-11z"/><path d="M9 8V6.5a3 3 0 0 1 6 0V8"/>',
    groups: '<circle cx="9" cy="9" r="2.6"/><circle cx="16" cy="9.5" r="2"/><path d="M4.5 18.5c.5-2.6 2.3-4 4.5-4s4 1.4 4.5 4M14.5 14.6c2.2-.4 4.2.8 4.8 3.4"/>',
    maintenance: '<path d="M14.5 6.5a3.2 3.2 0 0 0-4.2 4.2L4.5 16.5l3 3 5.8-5.8a3.2 3.2 0 0 0 4.2-4.2L15 12l-3-3 2.5-2.5z"/>',
    security: '<path d="M12 3.5 19 6.5v5.2c0 4.2-2.8 7.2-7 8.8-4.2-1.6-7-4.6-7-8.8V6.5z"/><path d="m9.2 12 2 2 3.8-4"/>',
    rewards: '<rect x="4" y="10" width="16" height="10" rx="1.5"/><path d="M12 10v10M4 14h16M12 10c-1.4-2.2-4.2-2.4-4.2-.6S11 10 12 10zm0 0c1.4-2.2 4.2-2.4 4.2-.6S13 10 12 10z"/>',
    profile: '<circle cx="12" cy="8.5" r="3.2"/><path d="M5.5 19.5c1-3 3.2-4.5 6.5-4.5s5.5 1.5 6.5 4.5"/>',
    admin: '<path d="M4 7h10M18 7h2M4 17h4M12 17h8"/><circle cx="16" cy="7" r="2"/><circle cx="10" cy="17" r="2"/>',
    notifications: '<path d="M6 16.5h12l-1.2-2.2V10a4.8 4.8 0 0 0-9.6 0v4.3z"/><path d="M10 16.5a2 2 0 0 0 4 0"/>',
    more: '<circle cx="6" cy="12" r="1.3"/><circle cx="12" cy="12" r="1.3"/><circle cx="18" cy="12" r="1.3"/>',
    logout: '<path d="M14 5h4.5v14H14"/><path d="M10 8.5 6.5 12l3.5 3.5M6.5 12H15"/>',
    plus: '<path d="M12 5v14M5 12h14"/>',
    check: '<path d="m5 12.5 4.5 4.5L19 7.5"/>',
    alert: '<path d="M12 4 21 19.5H3z"/><path d="M12 10v4M12 17v.2"/>',
    clock: '<circle cx="12" cy="12" r="8"/><path d="M12 8v4.5l3 2"/>',
    pin: '<path d="M12 21s6.5-5.6 6.5-11A6.5 6.5 0 0 0 5.5 10c0 5.4 6.5 11 6.5 11z"/><circle cx="12" cy="10" r="2.3"/>',
    heart: '<path d="M12 19.5s-7.5-4.4-7.5-9.7A4.2 4.2 0 0 1 12 7.4a4.2 4.2 0 0 1 7.5 2.4c0 5.3-7.5 9.7-7.5 9.7z"/>',
    chat: '<path d="M5 6.5h14v9H10l-5 3.5z"/>',
    copy: '<rect x="8" y="8" width="11" height="11" rx="2"/><path d="M5 15V6.5A1.5 1.5 0 0 1 6.5 5H15"/>',
    key: '<circle cx="8.5" cy="14.5" r="3.5"/><path d="m11 12 8-8M16 7l2 2M14 9l1.5 1.5"/>',
    mail: '<rect x="4" y="6" width="16" height="12" rx="2"/><path d="m4.5 7 7.5 6 7.5-6"/>',
    sparkle: '<path d="M12 4v4M12 16v4M4 12h4M16 12h4M6.5 6.5l2.5 2.5M15 15l2.5 2.5M6.5 17.5 9 15M15 9l2.5-2.5"/>',
    book: '<path d="M5 5.5A1.5 1.5 0 0 1 6.5 4H19v14H6.5A1.5 1.5 0 0 0 5 19.5z"/><path d="M5 19.5A1.5 1.5 0 0 0 6.5 21H19"/>',
    back: '<path d="M14.5 6 8.5 12l6 6"/>',
    map: '<path d="m4 6.5 5-2 6 2 5-2v13l-5 2-6-2-5 2z"/><path d="M9 4.5v13M15 6.5v13"/>',
    camera: '<path d="M4 8.5A1.5 1.5 0 0 1 5.5 7h2.3l1.5-2h5.4l1.5 2h2.3A1.5 1.5 0 0 1 20 8.5v9a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 17.5z"/><circle cx="12" cy="13" r="3.3"/>'
  };
  const d = paths[name] || paths.home;
  return `<span class="ico-svg" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">${d}</svg></span>`;
}

class SessionError extends Error {}

async function readError(res) {
  const text = await res.text();
  if (!text) return res.status === 403 ? "Your role cannot do that." : res.status === 404 ? "That item no longer exists." : `Request failed (${res.status}).`;
  try {
    const body = JSON.parse(text);
    if (typeof body === "string") return body;
    if (body.errors) return Object.values(body.errors).flat().join(" ");
    return body.message || body.detail || body.title || text;
  } catch { return text; }
}

async function api(path, options = {}) {
  // Fetch keeps the UI on the same origin as the API (MDN, 2025).
  // A FormData body needs the browser to set its own multipart boundary header.
  const headers = { ...(options.body instanceof FormData ? {} : { "Content-Type": "application/json" }), ...(options.headers || {}) };
  if (state.user?.token) headers.Authorization = `Bearer ${state.user.token}`;
  const res = await fetch(path, { ...options, headers });
  // A rejected token after a Render restart must drop the saved session (OWASP, 2023).
  if (res.status === 401 && state.user?.token) {
    save(null);
    sessionNotice = "Your session ended. Sign in again to continue.";
    throw new SessionError("Session expired");
  }
  if (!res.ok) throw new Error(await readError(res));
  if (res.status === 204) return null;
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

const save = user => { state.user = user; user ? localStorage.setItem("reslink.user", JSON.stringify(user)) : localStorage.removeItem("reslink.user"); };
const esc = v => String(v ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const val = id => document.getElementById(id)?.value ?? "";
const initial = name => (name || "R").trim().charAt(0).toUpperCase();
const initials = name => (name || "R").split(" ").filter(Boolean).map(p => p[0]).join("").slice(0, 2).toUpperCase();
const tagClass = value => {
  const v = (value || "").toLowerCase();
  if (["resolved", "like new", "member", "used", "student", "departed"].includes(v)) return "leaf";
  if (["pending", "open", "medium", "fair", "sports", "maintenance", "featured"].includes(v)) return "gold";
  if (["in progress", "acknowledged", "good", "social", "onsite", "electronics"].includes(v)) return "sky";
  if (["high", "urgent", "security", "voided", "cultural"].includes(v)) return "rose";
  if (["academic", "admin", "announcement", "poll", "textbooks"].includes(v)) return "plum";
  return "slate";
};
const prettyStatus = s => s === "Open" ? "Pending" : s === "InProgress" ? "In Progress" : s === "OnSite" ? "On site" : s;
const fmtDate = iso => {
  if (!iso) return "";
  const d = new Date(iso);
  return d.toLocaleString(undefined, { weekday: "short", month: "short", day: "numeric", hour: "numeric", minute: "2-digit" });
};
const timeAgo = iso => {
  const mins = Math.round((Date.now() - new Date(iso)) / 60000);
  if (mins < 1) return "Just now";
  if (mins < 60) return `${mins} min ago`;
  if (mins < 1440) return `${Math.round(mins / 60)} h ago`;
  return new Date(iso).toLocaleDateString(undefined, { month: "short", day: "numeric" });
};

function pageHead(title, sub, action = "", eyebrow = "") {
  return `<div class="page-head"><div>${eyebrow ? `<div class="eyebrow">${eyebrow}</div>` : ""}<h1>${title}</h1>${sub ? `<p>${sub}</p>` : ""}</div>${action}</div>`;
}
const emptyState = (title, text, name = "sparkle", tint = "leaf") =>
  `<div class="empty"><div class="tile ${tint}">${icon(name)}</div><b>${title}</b><p>${text}</p></div>`;
const pills = (items, active, attr = "filter") =>
  `<div class="pills">${items.map(([value, label]) => `<button class="pill ${active === value ? "active" : ""}" data-${attr}="${value}">${label}</button>`).join("")}</div>`;
const toggleBtn = (id, label) => `<button class="btn" id="${id}">${icon("plus")}<span>${label}</span></button>`;

function toast(message, kind = "ok") {
  const box = document.getElementById("toasts");
  const el = document.createElement("div");
  el.className = `toast ${kind}`;
  el.innerHTML = `${icon(kind === "error" ? "alert" : "check")}<div>${esc(message)}</div>`;
  box.appendChild(el);
  setTimeout(() => { el.classList.add("leaving"); setTimeout(() => el.remove(), 220); }, kind === "error" ? 5200 : 3400);
}

function openDialog(inner) {
  const back = document.createElement("div");
  back.className = "dialog-back";
  back.innerHTML = `<div class="dialog" role="dialog" aria-modal="true">${inner}</div>`;
  const close = () => { back.remove(); document.removeEventListener("keydown", onKey); };
  const onKey = e => { if (e.key === "Escape") close(); };
  back.addEventListener("click", e => { if (e.target === back || e.target.closest("[data-close]")) close(); });
  document.addEventListener("keydown", onKey);
  document.body.appendChild(back);
  return { el: back, close };
}

// Any failed button action shows its reason instead of failing silently.
window.addEventListener("unhandledrejection", e => {
  if (e.reason instanceof SessionError) { render(); return; }
  toast(e.reason?.message || "Something went wrong. Try again.", "error");
});

// Location is asked for only when the panic button is pressed; a denial or timeout still sends the alert (MDN, 2025).
function currentPosition() {
  if (!navigator.geolocation) return Promise.resolve(null);
  return new Promise(resolve => navigator.geolocation.getCurrentPosition(
    p => resolve(p.coords), () => resolve(null), { enableHighAccuracy: true, timeout: 8000, maximumAge: 30000 }));
}

const mapLink = a => a.latitude != null && a.longitude != null
  ? `<a class="link" href="https://www.google.com/maps?q=${a.latitude},${a.longitude}" target="_blank" rel="noopener">${icon("map")}Open in Maps</a>`
  : "";

function navButtons(nav) {
  let section = "";
  return nav.map(([id, label]) => {
    const head = navSection[id] !== section ? `<div class="nav-label">${navSection[id]}</div>` : "";
    section = navSection[id];
    return `${head}<button class="${state.page === id ? "active" : ""}" data-page="${id}" title="${label}">${icon(id)}<span>${label}</span></button>`;
  }).join("");
}

async function render() {
  const resetToken = location.pathname === "/reset-password" ? new URLSearchParams(location.search).get("token") : null;
  if (resetToken) {
    app.innerHTML = authShell(resetHtml());
    bindReset(resetToken);
    return;
  }
  // The sidebar is only shown once a token is stored (OWASP, 2023).
  if (!state.user?.token) {
    if (state.user) save(null);
    app.innerHTML = authShell(authCard());
    bindAuth();
    if (sessionNotice) {
      const note = document.getElementById("auth-error");
      if (note) note.textContent = sessionNotice;
      sessionNotice = "";
    }
    return;
  }
  const nav = navByRole[state.user.role] || navByRole.Student;
  const primary = nav.slice(0, 4);
  const extra = nav.slice(4);
  document.body.classList.toggle("menu-open", state.menu);
  app.innerHTML = `
    <div class="shell">
      <header class="topbar">
        <img src="brand/horizontal.png" alt="ResLink" />
        <button class="avatar" id="open-menu" aria-label="Open menu">${initials(state.user.fullName)}</button>
      </header>
      ${state.menu || state.more ? `<div class="scrim${state.menu ? "" : " under"}" id="scrim"></div>` : ""}
      <aside class="sidebar${state.menu ? " open" : ""}">
        <div class="side-brand"><img class="full" src="brand/horizontal.png" alt="ResLink" /><img class="mark" src="brand/mark.png" alt="ResLink" /></div>
        <nav class="nav">${navButtons(nav)}</nav>
        <div class="side-foot">
          <div class="avatar">${initials(state.user.fullName)}</div>
          <div class="who"><b>${esc(state.user.fullName)}</b><small>${esc(state.user.role)}${state.user.room ? ` · ${esc(state.user.room)}` : ""}</small></div>
          <button class="icon-btn" id="logout" title="Sign out" aria-label="Sign out">${icon("logout")}</button>
        </div>
      </aside>
      <main class="main" id="main"></main>
      <nav class="bottom-nav" style="grid-template-columns:repeat(${primary.length + (extra.length ? 1 : 0)},1fr)">
        ${primary.map(([id]) => `<button class="${state.page === id ? "active" : ""}" data-page="${id}">${icon(id)}<span>${shortLabel[id]}</span></button>`).join("")}
        ${extra.length ? `<button id="more-btn" class="${extra.some(([id]) => id === state.page) || state.more ? "active" : ""}">${icon("more")}<span>More</span></button>` : ""}
      </nav>
      ${state.more ? `<div class="more-sheet">${extra.map(([id, label]) => `<button class="${state.page === id ? "active" : ""}" data-page="${id}"><span class="tile ${featureTint[id]}">${icon(id)}</span>${label}</button>`).join("")}</div>` : ""}
    </div>`;
  document.querySelectorAll("[data-page]").forEach(b => b.onclick = () => { state.page = b.dataset.page; state.filter = "All"; state.tab = "overview"; state.menu = false; state.more = false; render(); window.scrollTo(0, 0); });
  document.getElementById("logout").onclick = () => { save(null); state.menu = false; state.auth = "signin"; render(); };
  document.getElementById("open-menu").onclick = () => { state.menu = true; state.more = false; render(); };
  const scrim = document.getElementById("scrim");
  if (scrim) scrim.onclick = () => { state.menu = false; state.more = false; render(); };
  const more = document.getElementById("more-btn");
  if (more) more.onclick = () => { state.more = !state.more; render(); };
  await draw();
}

function authShell(card) {
  return `<div class="auth">
    <section class="auth-brand">
      <div class="auth-grid-lines"></div>
      <div>
        <img class="auth-lockup" src="brand/lockup.png" alt="ResLink. Find. Connect. Live." />
        <div class="auth-copy">
          <h2>Residence life, <em>connected.</em></h2>
          <p>One place for community, events, safety and maintenance, for students and the staff who look after them.</p>
        </div>
        <ul class="auth-points">
          <li><span class="tile">${icon("community")}</span><div><b>Community and events</b>Posts, polls, study groups and RSVPs in one feed.</div></li>
          <li><span class="tile">${icon("security")}</span><div><b>Help when it matters</b>A panic button that sends your room and live location.</div></li>
          <li><span class="tile">${icon("maintenance")}</span><div><b>Repairs you can follow</b>Report an issue and track it until it is fixed.</div></li>
        </ul>
      </div>
      <p class="auth-foot">© ${new Date().getFullYear()} ResLink</p>
    </section>
    <header class="auth-mobile-head"><img src="brand/lockup.png" alt="ResLink. Find. Connect. Live." /><p>Community, safety and maintenance for your residence.</p></header>
    <div class="auth-side"><div class="auth-card" id="auth-card">${card}</div></div>
  </div>`;
}

const demoAccounts = [
  ["Student", "student@reslink.app", "Student123!", ""],
  ["Admin", "admin@reslink.app", "Admin123!", "plum"],
  ["Security", "security@reslink.app", "Security123!", "rose"],
  ["Maintenance", "maintenance@reslink.app", "Maintenance123!", "gold"]
];

function authCard() {
  const tab = state.auth;
  if (tab === "forgot") {
    return `<button class="link back-link" id="to-signin">${icon("back")}Back to sign in</button>
      <h1>Reset your password</h1>
      <p class="lead">Enter your account email and we will send a one-time link. If email is not set up at your residence yet, ask the residence admin for a reset link.</p>
      <form class="stack" id="forgot-form">
        <label>Email<input id="f-email" type="email" autocomplete="email" placeholder="you@example.com" required /></label>
        <p class="note" id="f-msg"></p>
        <button class="btn lg wide" type="submit" id="send-f">${icon("mail")}<span>Send reset link</span></button>
      </form>`;
  }
  const tabs = `<div class="segmented" role="tablist">
    ${[["signin", "Sign in"], ["student", "Student"], ["staff", "Staff"]].map(([id, label]) => `<button type="button" role="tab" class="${tab === id ? "active" : ""}" data-auth="${id}">${label}</button>`).join("")}
  </div>`;
  if (tab === "student") {
    return `<h1>Join your residence</h1><p class="lead">Create a student account in under a minute.</p>${tabs}
      <form class="form-grid" id="student-form">
        <label class="full">Full name<input id="s-name" autocomplete="name" required /></label>
        <label class="full">Email<input id="s-email" type="email" autocomplete="email" required /></label>
        <label class="full">Password<input id="s-password" type="password" autocomplete="new-password" minlength="8" required /></label>
        <label>Student number<input id="s-number" required /></label>
        <label>Room<input id="s-room" placeholder="e.g. A-214" required /></label>
        <label class="full">Residence<select id="s-res"><option value="">Loading residences…</option></select></label>
        <p class="error full" id="s-error"></p>
        <button class="btn lg wide full" type="submit" id="reg-s">Create student account</button>
      </form>`;
  }
  if (tab === "staff") {
    return `<h1>Staff sign-up</h1><p class="lead">Admins, security and maintenance staff need the access code from the residence.</p>${tabs}
      <form class="form-grid" id="staff-form">
        <label class="full">Full name<input id="t-name" autocomplete="name" required /></label>
        <label class="full">Email<input id="t-email" type="email" autocomplete="email" required /></label>
        <label class="full">Password<input id="t-password" type="password" autocomplete="new-password" minlength="8" required /></label>
        <label>Staff ID<input id="t-staff" required /></label>
        <label>Role<select id="t-role"><option>Admin</option><option>Security</option><option>Maintenance</option></select></label>
        <label class="full">Access code<input id="t-code" type="password" autocomplete="off" required /></label>
        <label class="full">Residence<select id="t-res"><option value="">Loading residences…</option></select></label>
        <p class="error full" id="t-error"></p>
        <button class="btn lg wide full" type="submit" id="reg-t">Create staff account</button>
      </form>`;
  }
  return `<h1>Welcome back</h1><p class="lead">Sign in to open the dashboard for your role.</p>${tabs}
    <form class="stack" id="login-form">
      <label>Email<input id="email" type="email" autocomplete="email" placeholder="you@example.com" required /></label>
      <div>
        <div class="field-row"><label for="password">Password</label><button type="button" class="link" id="show-f">Forgot password?</button></div>
        <input id="password" type="password" autocomplete="current-password" required style="margin-top:6px" />
      </div>
      <p class="error" id="auth-error"></p>
      <button class="btn lg wide" type="submit" id="login">Sign in</button>
    </form>
    <div class="demo"><p>Explore with a demo account</p><div class="chips">
      ${demoAccounts.map(([role, email, password, tint]) => `<button type="button" class="chip ${tint}" data-email="${email}" data-password="${password}"><i></i>${role}</button>`).join("")}
    </div></div>`;
}

function resetHtml() {
  return `<h1>Set a new password</h1>
    <p class="lead">This link works once and expires 30 minutes after it was created.</p>
    <form class="stack" id="reset-form">
      <label>New password<input id="r-pass" type="password" autocomplete="new-password" required /></label>
      <label>Confirm password<input id="r-pass2" type="password" autocomplete="new-password" required /></label>
      <p class="error" id="r-error"></p>
      <button class="btn lg wide" type="submit" id="r-save">${icon("key")}<span>Save password</span></button>
    </form>
    <p style="margin-top:18px"><button class="link" id="r-back">${icon("back")}Back to sign in</button></p>`;
}

function bindReset(token) {
  const back = () => { history.replaceState(null, "", "/"); render(); };
  document.getElementById("r-back").onclick = back;
  document.getElementById("reset-form").onsubmit = async e => {
    e.preventDefault();
    const error = document.getElementById("r-error");
    if (val("r-pass").length < 8) { error.textContent = "Use at least 8 characters."; return; }
    if (val("r-pass") !== val("r-pass2")) { error.textContent = "The passwords do not match."; return; }
    try {
      await api("/api/auth/reset-password", { method: "POST", body: JSON.stringify({ token, newPassword: val("r-pass") }) });
      sessionNotice = "Password saved. Sign in with your new password.";
      back();
    } catch (err) { error.textContent = err.message; }
  };
}

function busy(button, on, text) {
  if (!button) return;
  if (on) { button.dataset.label = button.innerHTML; button.disabled = true; button.textContent = text; }
  else { button.disabled = false; button.innerHTML = button.dataset.label || button.innerHTML; }
}

function signedIn(user) {
  save(user);
  state.page = "home";
  state.auth = "signin";
  render();
}

function bindAuth() {
  const card = document.getElementById("auth-card");
  const switchTo = tab => { state.auth = tab; card.innerHTML = authCard(); bindAuth(); };
  card.querySelectorAll("[data-auth]").forEach(b => b.onclick = () => switchTo(b.dataset.auth));

  if (state.auth === "forgot") {
    document.getElementById("to-signin").onclick = () => switchTo("signin");
    document.getElementById("forgot-form").onsubmit = async e => {
      e.preventDefault();
      const msg = document.getElementById("f-msg");
      const button = document.getElementById("send-f");
      busy(button, true, "Sending…");
      try {
        const res = await api("/api/auth/forgot-password", { method: "POST", body: JSON.stringify({ email: val("f-email") }) });
        msg.textContent = res.message;
      } catch (err) { msg.textContent = err.message; }
      finally { busy(button, false); }
    };
    return;
  }

  if (state.auth === "student" || state.auth === "staff") {
    const [selectId, errorId] = state.auth === "student" ? ["s-res", "s-error"] : ["t-res", "t-error"];
    api("/api/auth/residences").then(items => {
      const select = document.getElementById(selectId);
      if (!select) return;
      select.innerHTML = items.length
        ? items.map(r => `<option value="${r.id}">${esc(r.name)}</option>`).join("")
        : `<option value="">No residences yet</option>`;
      if (!items.length) document.getElementById(errorId).textContent = "No residences are set up yet. Ask the residence admin to add one.";
    }).catch(() => {
      const error = document.getElementById(errorId);
      if (error) error.textContent = "Residences could not be loaded. Check that the API is running, then refresh.";
    });
  }

  if (state.auth === "student") {
    document.getElementById("student-form").onsubmit = async e => {
      e.preventDefault();
      const button = document.getElementById("reg-s");
      busy(button, true, "Creating account…");
      try { signedIn(await api("/api/auth/register/student", { method: "POST", body: JSON.stringify({ fullName: val("s-name"), email: val("s-email"), password: val("s-password"), studentNumber: val("s-number"), room: val("s-room"), residenceId: val("s-res") }) })); }
      catch (err) { document.getElementById("s-error").textContent = err.message; busy(button, false); }
    };
    return;
  }

  if (state.auth === "staff") {
    document.getElementById("staff-form").onsubmit = async e => {
      e.preventDefault();
      const button = document.getElementById("reg-t");
      busy(button, true, "Creating account…");
      try { signedIn(await api("/api/auth/register/staff", { method: "POST", body: JSON.stringify({ fullName: val("t-name"), email: val("t-email"), password: val("t-password"), staffId: val("t-staff"), accessCode: val("t-code"), role: val("t-role"), residenceId: val("t-res") }) })); }
      catch { document.getElementById("t-error").textContent = "Invalid access code or details."; busy(button, false); }
    };
    return;
  }

  const login = async (email, password) => {
    const button = document.getElementById("login");
    busy(button, true, "Signing in…");
    try { signedIn(await api("/api/auth/login", { method: "POST", body: JSON.stringify({ email, password }) })); }
    catch { document.getElementById("auth-error").textContent = "Invalid email or password."; busy(button, false); }
  };
  document.getElementById("login-form").onsubmit = e => { e.preventDefault(); login(val("email"), val("password")); };
  document.getElementById("show-f").onclick = () => switchTo("forgot");
  card.querySelectorAll(".chip").forEach(c => c.onclick = () => {
    document.getElementById("email").value = c.dataset.email;
    document.getElementById("password").value = c.dataset.password;
    login(c.dataset.email, c.dataset.password);
  });
}

function skeleton() {
  return `<div class="skeleton">
    <div class="sk" style="height:34px;width:min(320px,70%)"></div>
    <div class="sk" style="height:150px"></div>
    <div class="grid-3"><div class="sk" style="height:200px"></div><div class="sk" style="height:200px"></div><div class="sk" style="height:200px"></div></div>
  </div>`;
}

async function draw() {
  const main = document.getElementById("main");
  main.innerHTML = skeleton();
  try {
    const pages = { home, community, events, market, groups, maintenance, security, rewards, admin, profile, notifications };
    await (pages[state.page] || home)(main);
  } catch (e) {
    if (e instanceof SessionError || !state.user?.token) { render(); return; }
    if (main) main.innerHTML = `${pageHead("Unable to load", "")}${emptyState("This page could not load", esc(e.message), "alert", "rose")}`;
  }
}

function greeting() {
  const h = new Date().getHours();
  return h < 12 ? "Good morning" : h < 17 ? "Good afternoon" : "Good evening";
}

function bindRsvp(root, refresh) {
  root.querySelectorAll(".rsvp").forEach(el => el.onclick = async () => {
    const going = el.dataset.on !== "true";
    await api(`/api/events/${el.dataset.id}/rsvp`, { method: going ? "POST" : "DELETE" });
    toast(going ? "You're on the list." : "RSVP cancelled.");
    refresh();
  });
}

// Every role loads announcements; Security/Maintenance skip the community feed shortcuts.
async function home(main) {
  const dash = await api("/api/dashboard");
  const events = await api("/api/events").catch(() => []);
  const posts = await api("/api/posts").catch(() => []);
  const announcements = posts.filter(p => p.kind === "Announcement");
  const tickets = ["Student", "Admin", "Maintenance"].includes(state.user.role) ? await api("/api/maintenance").catch(() => []) : [];
  const pending = tickets.filter(t => t.status !== "Resolved" && t.status !== "Cancelled").length;
  const first = dash.fullName.split(" ")[0];
  if (dash.points !== state.user.points) { state.user.points = dash.points; save(state.user); }
  const actions = [
    ["community", "New post"], ["events", "Events"], ["market", "Marketplace"],
    ["groups", "Study groups"], ["maintenance", "Report an issue"], ["rewards", "Rewards"], ["security", "Security"]
  ].filter(a => (navByRole[state.user.role] || []).some(n => n[0] === a[0])).slice(0, 6);
  const canSeeFeed = ["Student", "Admin"].includes(state.user.role);
  const a = dash.analytics;
  const chips = state.user.role === "Student"
    ? [["Points", dash.points, "gold"], ["Room", esc(state.user.room || "—"), ""], ["Open requests", pending, ""]]
    : a ? [["Open tickets", a.openTickets, ""], ["Complaints", a.openComplaints, ""], ["Panic alerts", a.openEmergencies, a.openEmergencies ? "gold" : ""]]
      : [["Points", dash.points, "gold"]];
  const subtitle = {
    Student: "Here's what's happening at your residence today.",
    Admin: "Here's how the residence is doing today.",
    Security: "Visitors, alerts and complaints waiting for the desk.",
    Maintenance: "Requests waiting for your team."
  }[state.user.role] || "";
  const recent = posts.filter(p => p.kind !== "Announcement").slice(0, 3);

  main.innerHTML = `
    <section class="hero">
      <img class="hero-mark" src="brand/mark.png" alt="" />
      <div>
        <div class="date">${new Date().toLocaleDateString(undefined, { weekday: "long", month: "long", day: "numeric" })}</div>
        <h1>${greeting()}, ${esc(first)}</h1>
        <p>${subtitle}</p>
      </div>
      <div class="hero-chips">${chips.map(([label, value, tint]) => `<div class="hero-chip ${tint}"><small>${label}</small><b>${value}</b></div>`).join("")}</div>
    </section>
    ${announcements.slice(0, 2).map(p => `<div class="notice"><span class="tile plum">${icon("notifications")}</span><div><b>${esc(p.title)}</b><p>${esc(p.body)}</p></div></div>`).join("")}
    ${actions.length ? `<div class="section-head"><h2>Quick actions</h2></div>
    <div class="actions">${actions.map(([id, label]) => `<button class="action" data-go="${id}"><span class="tile ${featureTint[id]}">${icon(id)}</span>${label}</button>`).join("")}</div>` : ""}
    ${events.length ? `<div class="section-head"><h2>Upcoming events</h2><button class="link" data-go="events">View all</button></div>
    <div class="grid-3">${events.slice(0, 3).map(eventCard).join("")}</div>` : ""}
    ${canSeeFeed && recent.length ? `<div class="section-head"><h2>From the community</h2><button class="link" data-go="community">Open feed</button></div>
    <div class="list">${recent.map(p => `<div class="list-row"><div class="avatar">${initial(p.authorName)}</div><div class="grow"><b>${esc(p.authorName)} <span class="tag ${tagClass(p.authorRole)}">${esc(p.authorRole)}</span></b><div class="muted">${esc(p.body)}</div></div><span class="muted small">${timeAgo(p.createdAt)}</span></div>`).join("")}</div>` : ""}`;
  main.querySelectorAll("[data-go]").forEach(b => b.onclick = () => { state.page = b.dataset.go; state.filter = "All"; render(); });
  bindRsvp(main, () => home(main));
}

// Seeded events use saved stock photos so the cards do not depend on another host (Unsplash, 2026).
function eventPhoto(e) {
  const title = (e.title || "").toLowerCase();
  if (title.includes("movie")) return "events/movie.jpg";
  if (title.includes("braai")) return "events/braai.jpg";
  if (title.includes("football")) return "events/football.jpg";
  if (title.includes("study") || title.includes("exam")) return "events/study.jpg";
  if (title.includes("yoga")) return "events/yoga.jpg";
  if (title.includes("heritage")) return "events/heritage.jpg";
  if (title.includes("meeting")) return "events/meeting.jpg";
  const byCategory = { Social: "events/braai.jpg", Sports: "events/football.jpg", Academic: "events/study.jpg", Cultural: "events/heritage.jpg", Meeting: "events/meeting.jpg" };
  return byCategory[e.category] || "events/meeting.jpg";
}

function eventCard(e) {
  const d = new Date(e.startsAt);
  return `<article class="card flush hover">
    <div class="media">
      <img src="${eventPhoto(e)}" alt="" loading="lazy" />
      <div class="tags"><span class="tag glass">${esc(e.category)}</span>${e.isFeatured ? `<span class="tag gold">Featured</span>` : ""}</div>
      <div class="date-badge"><small>${d.toLocaleDateString(undefined, { month: "short" })}</small><b>${d.getDate()}</b></div>
    </div>
    <div class="body">
      <h3>${esc(e.title)}</h3>
      <div class="meta">
        <span>${icon("clock")}${fmtDate(e.startsAt)}</span>
        <span>${icon("pin")}${esc(e.location)}</span>
        <span>${icon("groups")}${e.rsvpCount} attending</span>
      </div>
      ${state.user.role === "Student" ? `<div class="card-foot"><button class="btn wide ${e.hasRsvp ? "ghost" : ""} rsvp" data-id="${e.id}" data-on="${e.hasRsvp}">${e.hasRsvp ? "Cancel RSVP" : "RSVP"}</button></div>` : ""}
    </div></article>`;
}

async function community(main) {
  const posts = await api("/api/posts");
  const filtered = state.filter === "All" ? posts : posts.filter(p => p.kind === state.filter);
  const canPost = ["Student", "Admin"].includes(state.user.role);
  const kind = ["Post", "Discussion", "Poll"].includes(state.tab) ? state.tab : "Post";
  main.innerHTML = `
    ${pageHead("Community", "Share updates, start discussions and run polls with your residence.")}
    ${canPost ? `<div class="card composer mb">
      <div class="post">
        <div class="avatar">${initial(state.user.fullName)}</div>
        <div class="grow stack">
          <div class="row">${["Post", "Discussion", "Poll"].map(k => `<button class="pill ${kind === k ? "active" : ""}" data-kind="${k}">${k}</button>`).join("")}</div>
          <input id="p-title" placeholder="Title (optional)" />
          <textarea id="p-body" placeholder="What's on your mind, ${esc(state.user.fullName.split(" ")[0])}?"></textarea>
          <label id="poll-opts" class="${kind === "Poll" ? "" : "hidden"}">Poll options, separated by commas<input id="p-opts" value="Braai, Movie night" /></label>
          <div class="row" style="justify-content:flex-end"><button class="btn" id="share">Share</button></div>
        </div>
      </div>
    </div>` : ""}
    ${pills(["All", "Post", "Discussion", "Poll", "Announcement"].map(f => [f, f === "All" ? "All posts" : f + "s"]), state.filter)}
    ${filtered.length ? filtered.map(p => {
      const total = (p.pollOptions || []).reduce((sum, o) => sum + o.votes, 0);
      return `<article class="card mb"><div class="post">
        <div class="avatar">${initial(p.authorName)}</div>
        <div class="grow">
          <div class="post-head"><b>${esc(p.authorName)}</b><span class="tag ${tagClass(p.authorRole)}">${esc(p.authorRole)}</span>${p.kind !== "Post" ? `<span class="tag ${tagClass(p.kind)}">${esc(p.kind)}</span>` : ""}<span class="muted small">· ${timeAgo(p.createdAt)}</span></div>
          ${p.title ? `<h3>${esc(p.title)}</h3>` : ""}
          <p class="post-body">${esc(p.body)}</p>
          ${(p.pollOptions || []).length ? `<div class="poll">${p.pollOptions.map(o => {
            const pct = total ? Math.round(o.votes / total * 100) : 0;
            return `<button class="poll-option vote" data-id="${p.id}" data-i="${o.index}"><i style="width:${pct}%"></i><span>${esc(o.label)}</span><span class="muted">${pct}% · ${o.votes}</span></button>`;
          }).join("")}</div>` : ""}
          <div class="post-stats"><span>${icon("heart")}${p.likeCount}</span><span>${icon("chat")}${p.comments.length} ${p.comments.length === 1 ? "comment" : "comments"}</span></div>
          ${p.comments.length ? `<div class="comments">${p.comments.map(c => `<div><b>${esc(c.authorName)}</b> <span class="muted">${esc(c.body)}</span></div>`).join("")}</div>` : ""}
          ${canPost ? `<div class="reply"><input id="c-${p.id}" placeholder="Write a reply" /><button class="btn ghost comment" data-id="${p.id}">Reply</button></div>` : ""}
          ${state.user.role === "Admin" || p.authorId === state.user.userId ? `<div style="margin-top:10px"><button class="link del" data-id="${p.id}" style="color:var(--rose)">Remove post</button></div>` : ""}
        </div>
      </div></article>`;
    }).join("") : emptyState("Nothing here yet", "Be the first to post in this category.", "community")}`;
  main.querySelectorAll("[data-filter]").forEach(b => b.onclick = () => { state.filter = b.dataset.filter; community(main); });
  main.querySelectorAll("[data-kind]").forEach(b => b.onclick = () => {
    state.tab = b.dataset.kind;
    main.querySelectorAll("[data-kind]").forEach(x => x.classList.toggle("active", x === b));
    document.getElementById("poll-opts").classList.toggle("hidden", state.tab !== "Poll");
  });
  const share = document.getElementById("share");
  if (share) share.onclick = async () => {
    if (!val("p-body").trim()) { toast("Write something before sharing.", "error"); return; }
    const k = ["Post", "Discussion", "Poll"].includes(state.tab) ? state.tab : "Post";
    await api("/api/posts", { method: "POST", body: JSON.stringify({ title: val("p-title") || "Update", body: val("p-body"), kind: k, isPoll: k === "Poll", pollOptions: val("p-opts").split(",").map(s => s.trim()) }) });
    toast("Shared with your residence.");
    community(main);
  };
  main.querySelectorAll(".vote").forEach(el => el.onclick = async () => { await api(`/api/posts/${el.dataset.id}/vote`, { method: "POST", body: JSON.stringify({ optionIndex: Number(el.dataset.i) }) }); community(main); });
  main.querySelectorAll(".comment").forEach(el => el.onclick = async () => {
    if (!val(`c-${el.dataset.id}`).trim()) return;
    await api(`/api/posts/${el.dataset.id}/comments`, { method: "POST", body: JSON.stringify({ body: val(`c-${el.dataset.id}`) }) });
    community(main);
  });
  main.querySelectorAll(".del").forEach(el => el.onclick = async () => { await api(`/api/posts/${el.dataset.id}`, { method: "DELETE" }); toast("Post removed."); community(main); });
}

async function events(main) {
  const items = await api("/api/events");
  const filtered = state.filter === "All" ? items : items.filter(e => e.category === state.filter);
  main.innerHTML = `
    ${pageHead("Events", "Discover what's on and save your spot.", state.user.role === "Admin" ? toggleBtn("toggle-create", "Create event") : "")}
    <div class="card form-card hidden" id="create-box">
      <h3>New event</h3>
      <div class="form-grid">
        <label class="full">Title<input id="e-title" /></label>
        <label class="full">Description<textarea id="e-desc"></textarea></label>
        <label>Location<input id="e-loc" /></label>
        <label>Starts at<input id="e-when" type="datetime-local" /></label>
        <label>Category<select id="e-cat"><option>Social</option><option>Academic</option><option>Sports</option><option>Cultural</option><option>Meeting</option><option>Other</option></select></label>
      </div>
      <div class="form-actions"><button class="btn ghost" data-cancel="create-box">Cancel</button><button class="btn" id="create-event">Publish event</button></div>
    </div>
    ${pills(["All", "Social", "Academic", "Sports", "Cultural", "Meeting", "Other"].map(f => [f, f === "All" ? "All events" : f]), state.filter)}
    ${filtered.length ? `<div class="grid-3">${filtered.map(eventCard).join("")}</div>` : emptyState("No events here yet", "Check back soon or try another category.", "events", "sky")}`;
  main.querySelectorAll("[data-filter]").forEach(b => b.onclick = () => { state.filter = b.dataset.filter; events(main); });
  bindToggles(main, { "toggle-create": "create-box" });
  const create = document.getElementById("create-event");
  if (create) create.onclick = async () => {
    if (!val("e-title").trim() || !val("e-when")) { toast("Add a title and a start time.", "error"); return; }
    await api("/api/events", { method: "POST", body: JSON.stringify({ title: val("e-title"), description: val("e-desc"), location: val("e-loc"), startsAt: new Date(val("e-when")).toISOString(), category: val("e-cat") }) });
    toast("Event published.");
    events(main);
  };
  bindRsvp(main, () => events(main));
}

function bindToggles(root, map) {
  Object.entries(map).forEach(([buttonId, boxId]) => {
    const button = document.getElementById(buttonId);
    if (button) button.onclick = () => { document.getElementById(boxId).classList.toggle("hidden"); document.getElementById(boxId).querySelector("input,textarea")?.focus(); };
  });
  root.querySelectorAll("[data-cancel]").forEach(b => b.onclick = () => document.getElementById(b.dataset.cancel).classList.add("hidden"));
}

const marketTint = { Textbooks: "plum", Electronics: "sky", Furniture: "gold", Clothing: "rose", Food: "leaf", Services: "slate" };
const marketIcon = { Textbooks: "book", Electronics: "sparkle", Food: "rewards", Services: "maintenance" };

// Marketplace, groups, and tickets expose undo so a lecturer tap is not final; reward redemptions are final.
async function market(main) {
  const items = await api("/api/marketplace");
  const filtered = state.filter === "All" ? items : items.filter(i => i.category === state.filter);
  const sell = state.user.role === "Student";
  main.innerHTML = `
    ${pageHead("Marketplace", "Buy, sell and swap with people in your residence.", sell ? toggleBtn("toggle-sell", "Sell an item") : "")}
    <div class="card form-card hidden" id="sell-box">
      <h3>List an item</h3>
      <div class="form-grid">
        <label class="full">Item<input id="m-title" placeholder="e.g. Calculus textbook" /></label>
        <label class="full">Description<textarea id="m-desc"></textarea></label>
        <label>Category<select id="m-cat"><option>Textbooks</option><option>Electronics</option><option>Furniture</option><option>Clothing</option><option>Food</option><option>Services</option><option>Other</option></select></label>
        <label>Condition<select id="m-cond"><option>Like New</option><option>Good</option><option>Fair</option></select></label>
        <label>Price (R)<input id="m-price" type="number" min="0" value="150" /></label>
      </div>
      <div class="form-actions"><button class="btn ghost" data-cancel="sell-box">Cancel</button><button class="btn" id="list-item">List item</button></div>
    </div>
    ${pills(["All", "Textbooks", "Electronics", "Furniture", "Clothing", "Food", "Services", "Other"].map(f => [f, f === "All" ? "Everything" : f]), state.filter)}
    ${filtered.length ? `<div class="grid-3">${filtered.map(i => {
      const mine = i.sellerId === state.user.userId || state.user.role === "Admin";
      return `<article class="card flush hover">
        <div class="media-art ${marketTint[i.category] || "slate"}">${icon(marketIcon[i.category] || "market")}</div>
        <div class="body">
          <div class="between"><h3>${esc(i.title)}</h3><span class="price">${i.price === 0 ? "Free" : "R" + i.price}</span></div>
          <p class="muted small">${esc(i.description)}</p>
          <div class="tags"><span class="tag ${tagClass(i.condition)}">${esc(i.condition)}</span><span class="tag slate">${esc(i.category)}</span>${i.isSold ? `<span class="tag rose">Sold</span>` : ""}</div>
          <div class="meta"><span>${icon("profile")}${esc(i.sellerName)}${i.sellerRoom ? " · Room " + esc(i.sellerRoom) : ""}</span></div>
          ${sell && mine ? `<div class="card-foot">${i.isSold ? `<button class="btn ghost sm unsold" data-id="${i.id}">Mark available</button>` : `<button class="btn soft sm sold" data-id="${i.id}">${icon("check")}Mark sold</button>`}</div>` : ""}
        </div></article>`;
    }).join("")}</div>` : emptyState("No listings yet", "List something you no longer need.", "market", "plum")}`;
  main.querySelectorAll("[data-filter]").forEach(b => b.onclick = () => { state.filter = b.dataset.filter; market(main); });
  bindToggles(main, { "toggle-sell": "sell-box" });
  const l = document.getElementById("list-item"); if (l) l.onclick = async () => {
    if (!val("m-title").trim()) { toast("Give the item a name.", "error"); return; }
    await api("/api/marketplace", { method: "POST", body: JSON.stringify({ title: val("m-title"), description: val("m-desc"), category: val("m-cat"), condition: val("m-cond"), price: Number(val("m-price")) }) });
    toast("Your item is listed.");
    market(main);
  };
  main.querySelectorAll(".sold").forEach(el => el.onclick = async () => { await api(`/api/marketplace/${el.dataset.id}/sold`, { method: "PUT" }); market(main); });
  main.querySelectorAll(".unsold").forEach(el => el.onclick = async () => { await api(`/api/marketplace/${el.dataset.id}/unsold`, { method: "PUT" }); market(main); });
}

async function groups(main) {
  const items = await api("/api/study-groups");
  const mine = state.filter === "Mine" ? items.filter(g => g.isMember) : items;
  main.innerHTML = `
    ${pageHead("Study Groups", "Find people taking the same courses and study together.", state.user.role === "Student" || state.user.role === "Admin" ? toggleBtn("toggle-g", "Create group") : "")}
    <div class="card form-card hidden" id="g-box">
      <h3>New study group</h3>
      <div class="form-grid">
        <label>Name<input id="g-name" /></label><label>Course code<input id="g-code" placeholder="e.g. INSY7231" /></label>
        <label class="full">Topic<input id="g-topic" /></label>
        <label class="full">Description<textarea id="g-desc"></textarea></label>
        <label>Schedule<input id="g-sch" placeholder="Tuesdays 18:00" /></label><label>Location<input id="g-loc" /></label>
      </div>
      <div class="form-actions"><button class="btn ghost" data-cancel="g-box">Cancel</button><button class="btn" id="g-create">Create group</button></div>
    </div>
    ${pills([["All", "All groups"], ["Mine", `My groups <span class="count">${items.filter(g => g.isMember).length}</span>`]], state.filter === "Mine" ? "Mine" : "All")}
    ${mine.length ? `<div class="grid-3">${mine.map(g => `<article class="card hover" style="display:flex;flex-direction:column;gap:10px">
      <div class="between"><span class="tile leaf">${icon("book")}</span><span class="tag plum">${esc(g.courseCode)}</span></div>
      <div><h3>${esc(g.name)}</h3><p class="muted small">${esc(g.topic)}</p></div>
      <p class="small" style="color:var(--ink-2)">${esc(g.description)}</p>
      <div class="meta"><span>${icon("clock")}${esc(g.schedule)}</span><span>${icon("pin")}${esc(g.location)}</span></div>
      <div><div class="between small muted" style="margin-bottom:6px"><span>Members</span><span>${g.memberCount}/${g.maxMembers}</span></div><div class="progress"><i style="width:${Math.min(100, Math.round(g.memberCount / Math.max(1, g.maxMembers) * 100))}%"></i></div></div>
      <div class="card-foot">${g.isMember ? `<span class="tag leaf dot">Member</span><button class="btn ghost sm leave" data-id="${g.id}">Leave</button>` : `<button class="btn wide join" data-id="${g.id}">Join group</button>`}</div>
    </article>`).join("")}</div>` : emptyState(state.filter === "Mine" ? "You haven't joined a group yet" : "No study groups yet", "Create one and invite your classmates.", "groups")}`;
  main.querySelectorAll("[data-filter]").forEach(b => b.onclick = () => { state.filter = b.dataset.filter; groups(main); });
  bindToggles(main, { "toggle-g": "g-box" });
  const c = document.getElementById("g-create"); if (c) c.onclick = async () => {
    if (!val("g-name").trim()) { toast("Give the group a name.", "error"); return; }
    await api("/api/study-groups", { method: "POST", body: JSON.stringify({ name: val("g-name"), topic: val("g-topic"), description: val("g-desc"), courseCode: val("g-code"), schedule: val("g-sch"), location: val("g-loc") }) });
    toast("Study group created.");
    groups(main);
  };
  main.querySelectorAll(".join").forEach(el => el.onclick = async () => { await api(`/api/study-groups/${el.dataset.id}/join`, { method: "POST" }); toast("You joined the group."); groups(main); });
  main.querySelectorAll(".leave").forEach(el => el.onclick = async () => { await api(`/api/study-groups/${el.dataset.id}/leave`, { method: "DELETE" }); groups(main); });
}

// Redrawing on a canvas turns any photo the browser can open into a JPEG well under the 5 MB limit (MDN, 2025).
async function shrinkPhoto(file, maxEdge = 1600) {
  const bitmap = await createImageBitmap(file, { imageOrientation: "from-image" }).catch(() => null);
  if (!bitmap) throw new Error("That file is not a photo this browser can open.");
  const scale = Math.min(1, maxEdge / Math.max(bitmap.width, bitmap.height));
  const canvas = document.createElement("canvas");
  canvas.width = Math.round(bitmap.width * scale);
  canvas.height = Math.round(bitmap.height * scale);
  canvas.getContext("2d").drawImage(bitmap, 0, 0, canvas.width, canvas.height);
  bitmap.close();
  return new Promise((resolve, reject) => canvas.toBlob(b => b ? resolve(b) : reject(new Error("Could not prepare the photo.")), "image/jpeg", 0.82));
}

async function maintenance(main) {
  const tickets = await api("/api/maintenance");
  const map = { All: () => true, Pending: t => t.status === "Open", "In Progress": t => t.status === "InProgress", Resolved: t => t.status === "Resolved", Cancelled: t => t.status === "Cancelled" };
  const filtered = tickets.filter(map[state.filter] || map.All);
  const student = state.user.role === "Student";
  const staff = ["Maintenance", "Admin"].includes(state.user.role);
  const count = f => tickets.filter(map[f]).length;
  main.innerHTML = `
    ${pageHead("Maintenance", staff ? "Work through requests from residents." : "Report an issue and follow it until it's fixed.", student ? toggleBtn("toggle-t", "New request") : "")}
    <div class="card form-card hidden" id="t-box">
      <h3>Report an issue</h3>
      <div class="form-grid">
        <label class="full">What's wrong?<input id="t-title" placeholder="e.g. Leaking tap in bathroom" /></label>
        <label class="full">Details<textarea id="t-desc"></textarea></label>
        <label class="full">Location<input id="t-loc" value="${esc(state.user.room ? "Room " + state.user.room : "")}" /></label>
        <label>Category<select id="t-cat"><option>Plumbing</option><option>Electrical</option><option>Furniture</option><option>General</option></select></label>
        <label>Priority<select id="t-pri"><option>Medium</option><option>High</option><option>Low</option></select></label>
        <div class="full photo-field">
          <span class="photo-label">Photo <span class="muted">(optional)</span></span>
          <label class="photo-drop" id="t-photo-drop">
            <input type="file" id="t-photo" accept="image/*" />
            <img id="t-photo-preview" alt="" hidden />
            <span class="photo-hint" id="t-photo-hint">${icon("camera")}<b>Take or choose a photo</b><small>JPEG or PNG. Large photos are shrunk before upload.</small></span>
          </label>
          <button type="button" class="btn ghost sm" id="t-photo-clear" hidden>Remove photo</button>
        </div>
      </div>
      <div class="form-actions"><button class="btn ghost" data-cancel="t-box">Cancel</button><button class="btn" id="new-ticket">Submit request</button></div>
    </div>
    ${pills(["All", "Pending", "In Progress", "Resolved", "Cancelled"].map(f => [f, `${f}<span class="count">${count(f)}</span>`]), state.filter)}
    ${filtered.length ? `<div class="list">${filtered.map(t => `<div class="list-row" style="align-items:flex-start">
      <span class="tile gold">${icon("maintenance")}</span>
      <div class="grow">
        <div class="tags" style="margin-bottom:6px"><b style="margin-right:4px">${esc(t.title)}</b><span class="tag dot ${tagClass(prettyStatus(t.status))}">${prettyStatus(t.status)}</span><span class="tag ${tagClass(t.priority)}">${esc(t.priority)}</span></div>
        <div class="muted">${esc(t.category)} · ${esc(t.location)} · ${esc(t.reporterName)} · ${timeAgo(t.createdAt)}</div>
        ${t.description ? `<p class="small" style="margin-top:6px;color:var(--ink-2)">${esc(t.description)}</p>` : ""}
        ${t.photoUrl ? `<a class="ticket-photo" href="${esc(t.photoUrl)}" target="_blank" rel="noopener"><img src="${esc(t.photoUrl)}" alt="Photo of ${esc(t.title)}" loading="lazy" /></a>` : ""}
        ${t.resolutionNotes ? `<p class="small muted" style="margin-top:4px">Notes: ${esc(t.resolutionNotes)}</p>` : ""}
      </div>
      <div class="end">
        ${staff ? `
          ${t.status === "Open" ? `<button class="btn sm st" data-id="${t.id}" data-s="InProgress">Start work</button>` : ""}
          ${t.status !== "Resolved" ? `<button class="btn soft sm st" data-id="${t.id}" data-s="Resolved">${icon("check")}Resolve</button>` : ""}
          ${t.status !== "Open" ? `<button class="btn ghost sm st" data-id="${t.id}" data-s="Open">Reopen</button>` : ""}
          ${t.status !== "Cancelled" ? `<button class="btn ghost sm st" data-id="${t.id}" data-s="Cancelled">Cancel</button>` : ""}` : ""}
        ${student && t.status === "Open" ? `<button class="btn ghost sm cancel-t" data-id="${t.id}">Cancel request</button>` : ""}
      </div>
    </div>`).join("")}</div>` : emptyState("No requests here", student ? "Everything working? Great. Report an issue any time." : "Nothing waiting in this list.", "maintenance", "gold")}`;
  main.querySelectorAll("[data-filter]").forEach(b => b.onclick = () => { state.filter = b.dataset.filter; maintenance(main); });
  bindToggles(main, { "toggle-t": "t-box" });
  const photoInput = document.getElementById("t-photo");
  const photoPreview = document.getElementById("t-photo-preview");
  const photoClear = document.getElementById("t-photo-clear");
  const showPhoto = file => {
    if (photoPreview.src) URL.revokeObjectURL(photoPreview.src);
    photoPreview.hidden = !file;
    photoClear.hidden = !file;
    document.getElementById("t-photo-hint").hidden = !!file;
    if (file) photoPreview.src = URL.createObjectURL(file); else photoPreview.removeAttribute("src");
  };
  if (photoInput) {
    photoInput.onchange = () => showPhoto(photoInput.files[0]);
    photoClear.onclick = () => { photoInput.value = ""; showPhoto(null); };
  }
  const nt = document.getElementById("new-ticket"); if (nt) nt.onclick = async () => {
    if (!val("t-title").trim()) { toast("Describe the issue first.", "error"); return; }
    nt.disabled = true;
    try {
      let photoUrl = null;
      const file = photoInput.files[0];
      if (file) {
        const form = new FormData();
        form.append("file", await shrinkPhoto(file), "ticket.jpg");
        photoUrl = (await api("/api/maintenance/photos", { method: "POST", body: form })).url;
      }
      await api("/api/maintenance", { method: "POST", body: JSON.stringify({ title: val("t-title"), description: val("t-desc"), location: val("t-loc"), category: val("t-cat"), priority: val("t-pri"), photoUrl }) });
      toast("Request sent to maintenance.");
      maintenance(main);
    } catch (err) {
      if (err instanceof SessionError) throw err;
      toast(err.message || "Could not send the request.", "error");
      nt.disabled = false;
    }
  };
  main.querySelectorAll(".st").forEach(el => el.onclick = async () => {
    await api(`/api/maintenance/${el.dataset.id}/status`, { method: "PUT", body: JSON.stringify({ status: el.dataset.s, resolutionNotes: "Updated by staff" }) });
    maintenance(main);
  });
  main.querySelectorAll(".cancel-t").forEach(el => el.onclick = async () => {
    await api(`/api/maintenance/${el.dataset.id}/status`, { method: "PUT", body: JSON.stringify({ status: "Cancelled", resolutionNotes: "Cancelled by student" }) });
    maintenance(main);
  });
}

async function security(main) {
  if (state.user.role === "Student") {
    const mineAlerts = await api("/api/emergencies/mine").catch(() => []);
    const mineNoise = await api("/api/noise/mine").catch(() => []);
    main.innerHTML = `
      ${pageHead("Security", "Get help fast, report noise, or let the desk know a visitor is coming.")}
      <div class="split">
        <div class="card panic-card">
          <span class="tile rose">${icon("alert")}</span>
          <h2 style="margin-top:14px;font-size:22px">Panic button</h2>
          <p class="muted" style="margin-top:6px">Security gets your name, room ${esc(state.user.room || "")} and, if you allow it, your live location.</p>
          <button class="panic-btn" id="panic">${icon("alert")}Send panic alert</button>
          <ol class="steps">
            <li><span><b>The desk is alerted at once.</b>Your alert opens at the top of the security queue.</span></li>
            <li><span><b>Your location goes with it.</b>Allow location when asked so officers can find you outside your room.</span></li>
            <li><span><b>Pressed it by mistake?</b>Mark it as a false alarm below and the desk is told.</span></li>
          </ol>
        </div>
        <div class="stack">
          <div class="card"><h3 style="margin-bottom:14px">Noise complaint</h3><div class="stack">
            <label>Location<input id="n-loc" placeholder="e.g. Block C, 2nd floor" /></label><label>What is happening?<input id="n-desc" /></label>
            <label class="check"><input id="n-anon" type="checkbox" checked /> Report anonymously</label>
            <div><button class="btn" id="n-send">Submit complaint</button></div></div></div>
          <div class="card"><h3 style="margin-bottom:14px">Register a visitor</h3><div class="form-grid">
            <label>Visitor name<input id="v-name" /></label><label>Your room<input id="v-room" value="${esc(state.user.room || "")}" /></label>
            <label class="full">Purpose<input id="v-purpose" placeholder="e.g. Study session" /></label></div>
            <div class="form-actions" style="justify-content:flex-start"><button class="btn" id="v-send">Register visitor</button></div></div>
        </div>
      </div>
      ${mineAlerts.length ? `<div class="section-head"><h2>Your panic alerts</h2></div><div class="list">
        ${mineAlerts.map(a => `<div class="list-row"><span class="tile rose">${icon("alert")}</span><div class="grow"><b>${esc(a.location)}</b><div class="muted">${fmtDate(a.createdAt)}</div></div>
        <div class="end"><span class="tag dot ${a.status === "Open" ? "rose" : tagClass(a.status)}">${prettyStatus(a.status)}</span>${a.status === "Open" ? `<button class="btn ghost sm wd-em" data-id="${a.id}">False alarm</button>` : ""}</div></div>`).join("")}</div>` : ""}
      ${mineNoise.length ? `<div class="section-head"><h2>Your noise reports</h2></div><div class="list">
        ${mineNoise.map(n => `<div class="list-row"><span class="tile slate">${icon("notifications")}</span><div class="grow"><b>${esc(n.location)}</b><div class="muted">${fmtDate(n.createdAt)}</div></div>
        <div class="end"><span class="tag dot ${tagClass(prettyStatus(n.status))}">${prettyStatus(n.status)}</span>${n.status === "Open" ? `<button class="btn ghost sm wd-ns" data-id="${n.id}">Withdraw</button>` : ""}</div></div>`).join("")}</div>` : ""}`;
    document.getElementById("panic").onclick = async e => {
      const button = e.currentTarget;
      button.disabled = true;
      button.textContent = "Getting your location…";
      const coords = await currentPosition();
      button.textContent = "Sending alert…";
      try {
        await api("/api/emergencies", { method: "POST", body: JSON.stringify({ message: "Panic button activated", latitude: coords?.latitude ?? null, longitude: coords?.longitude ?? null }) });
        toast(coords ? "Alert sent with your location. Security is on the way." : "Alert sent with your room. Security is on the way.");
      } finally { security(main); }
    };
    document.getElementById("n-send").onclick = async () => {
      if (!val("n-loc").trim()) { toast("Add where the noise is.", "error"); return; }
      await api("/api/noise", { method: "POST", body: JSON.stringify({ location: val("n-loc"), description: val("n-desc"), isAnonymous: document.getElementById("n-anon").checked }) });
      toast("Complaint sent to security.");
      security(main);
    };
    document.getElementById("v-send").onclick = async () => {
      if (!val("v-name").trim()) { toast("Add the visitor's name.", "error"); return; }
      await api("/api/visitors", { method: "POST", body: JSON.stringify({ visitorName: val("v-name"), hostRoom: val("v-room"), purpose: val("v-purpose") }) });
      toast("Visitor registered with the desk.");
      ["v-name", "v-purpose"].forEach(id => document.getElementById(id).value = "");
    };
    main.querySelectorAll(".wd-em").forEach(el => el.onclick = async () => { await api(`/api/emergencies/${el.dataset.id}/withdraw`, { method: "PUT" }); security(main); });
    main.querySelectorAll(".wd-ns").forEach(el => el.onclick = async () => { await api(`/api/noise/${el.dataset.id}/withdraw`, { method: "PUT" }); security(main); });
    return;
  }
  const visitors = await api("/api/visitors").catch(() => []);
  const alerts = await api("/api/emergencies");
  const noise = await api("/api/noise");
  const tab = state.tab === "incidents" ? "incidents" : "visitors";
  const openIncidents = alerts.filter(a => a.status === "Open").length + noise.filter(n => n.status === "Open").length;
  main.innerHTML = `
    ${pageHead("Security desk", "Visitors, panic alerts and noise complaints for the residence.", toggleBtn("toggle-v", "Register visitor"))}
    <div class="card form-card hidden" id="v-box">
      <h3>Register a visitor</h3>
      <div class="form-grid"><label>Visitor name<input id="v-name" /></label><label>Host room<input id="v-room" /></label><label class="full">Purpose<input id="v-purpose" /></label></div>
      <div class="form-actions"><button class="btn ghost" data-cancel="v-box">Cancel</button><button class="btn" id="v-send">Register</button></div>
    </div>
    ${pills([["visitors", `Visitors<span class="count">${visitors.filter(v => v.status === "OnSite").length}</span>`], ["incidents", `Incidents<span class="count">${openIncidents}</span>`]], tab, "tab")}
    ${tab === "visitors"
      ? (visitors.length ? `<div class="list">${visitors.map(v => `<div class="list-row"><div class="avatar">${initial(v.visitorName)}</div><div class="grow"><b>${esc(v.visitorName)}</b><div class="muted">Visiting ${esc(v.hostName)} · Room ${esc(v.hostRoom)} · ${esc(v.purpose)}</div></div>
          <div class="end">${v.status === "OnSite" ? `<span class="tag sky dot">On site</span><button class="btn ghost sm v-out" data-id="${v.id}">Check out</button>` : `<span class="tag slate">${esc(prettyStatus(v.status))}</span>`}</div></div>`).join("")}</div>`
        : emptyState("No visitors logged today", "Visitors registered by residents or the desk appear here.", "groups", "sky"))
      : `${alerts.length ? `<div class="section-head" style="margin-top:0"><h2>Panic alerts</h2></div><div class="list mb">${alerts.map(a => `<div class="list-row" style="align-items:flex-start">
          <span class="tile rose">${icon("alert")}</span>
          <div class="grow"><b>${esc(a.reporterName)}</b><div class="muted">${esc(a.location)} · ${fmtDate(a.createdAt)}</div><p class="small" style="margin-top:4px;color:var(--ink-2)">${esc(a.message)}</p>${mapLink(a) ? `<div style="margin-top:6px">${mapLink(a)}</div>` : ""}</div>
          <div class="end"><span class="tag dot ${a.status === "Open" ? "rose" : tagClass(a.status)}">${prettyStatus(a.status)}</span>
            ${a.status === "Open" ? `<button class="btn sm em" data-id="${a.id}" data-s="Acknowledged">Acknowledge</button>` : ""}
            ${a.status !== "Resolved" ? `<button class="btn soft sm em" data-id="${a.id}" data-s="Resolved">${icon("check")}Resolve</button>` : ""}
            ${a.status !== "Open" ? `<button class="btn ghost sm em" data-id="${a.id}" data-s="Open">Reopen</button>` : ""}
            ${a.status !== "Withdrawn" ? `<button class="btn ghost sm em" data-id="${a.id}" data-s="Withdrawn">False alarm</button>` : ""}</div></div>`).join("")}</div>` : ""}
        ${noise.length ? `<div class="section-head"><h2>Noise complaints</h2></div><div class="list">${noise.map(n => `<div class="list-row" style="align-items:flex-start">
          <span class="tile slate">${icon("notifications")}</span>
          <div class="grow"><b>${esc(n.location)}</b><div class="muted">${esc(n.reporterName)} · ${timeAgo(n.createdAt)}</div><p class="small" style="margin-top:4px;color:var(--ink-2)">${esc(n.description)}</p></div>
          <div class="end"><span class="tag dot ${tagClass(prettyStatus(n.status))}">${prettyStatus(n.status)}</span>
            ${n.status !== "Resolved" ? `<button class="btn soft sm ns" data-id="${n.id}" data-s="Resolved">${icon("check")}Resolve</button>` : ""}
            ${n.status !== "Open" ? `<button class="btn ghost sm ns" data-id="${n.id}" data-s="Open">Reopen</button>` : ""}
            ${n.status !== "Withdrawn" ? `<button class="btn ghost sm ns" data-id="${n.id}" data-s="Withdrawn">Withdraw</button>` : ""}</div></div>`).join("")}</div>` : ""}
        ${!alerts.length && !noise.length ? emptyState("All quiet", "No panic alerts or noise complaints.", "security", "leaf") : ""}`}`;
  main.querySelectorAll("[data-tab]").forEach(b => b.onclick = () => { state.tab = b.dataset.tab; security(main); });
  bindToggles(main, { "toggle-v": "v-box" });
  document.getElementById("v-send").onclick = async () => {
    if (!val("v-name").trim()) { toast("Add the visitor's name.", "error"); return; }
    await api("/api/visitors", { method: "POST", body: JSON.stringify({ visitorName: val("v-name"), hostRoom: val("v-room"), purpose: val("v-purpose") }) });
    toast("Visitor registered.");
    security(main);
  };
  main.querySelectorAll(".em").forEach(el => el.onclick = async () => { await api(`/api/emergencies/${el.dataset.id}/status`, { method: "PUT", body: JSON.stringify({ status: el.dataset.s }) }); security(main); });
  main.querySelectorAll(".ns").forEach(el => el.onclick = async () => { await api(`/api/noise/${el.dataset.id}/status`, { method: "PUT", body: JSON.stringify({ status: el.dataset.s }) }); security(main); });
  main.querySelectorAll(".v-out").forEach(el => el.onclick = async () => { await api(`/api/visitors/${el.dataset.id}/status`, { method: "PUT", body: JSON.stringify({ status: "Departed" }) }); security(main); });
}

const rewardIcon = { Food: "rewards", Merchandise: "market", Services: "maintenance", Experiences: "events", Discounts: "sparkle" };
const rewardTint = { Food: "gold", Merchandise: "plum", Services: "sky", Experiences: "rose", Discounts: "leaf" };

async function rewards(main) {
  const items = await api("/api/rewards");
  const redemptions = state.user.role === "Student" ? await api("/api/rewards/redemptions").catch(() => []) : [];
  const filtered = state.filter === "All" ? items : items.filter(r => r.category === state.filter);
  const student = state.user.role === "Student";
  const profile = await api("/api/profile").catch(() => null);
  if (profile && profile.points !== state.user.points) { state.user.points = profile.points; save(state.user); }

  main.innerHTML = `
    <section class="balance">
      <div>
        <small>Your reward balance</small>
        <div class="points">${state.user.points}<span>points</span></div>
        <p>Earn points by attending events, posting and helping others in your residence.</p>
      </div>
      <div class="hero-chips">
        <div class="hero-chip gold"><small>Redeemable now</small><b>${items.filter(r => state.user.points >= r.pointsCost).length}</b></div>
        <div class="hero-chip"><small>In the catalogue</small><b>${items.length}</b></div>
      </div>
    </section>

    <div class="section-head"><h2>Reward catalogue</h2>${state.user.role === "Admin" ? toggleBtn("toggle-r", "Add reward") : ""}</div>
    <div class="card form-card hidden" id="r-box">
      <h3>New reward</h3>
      <div class="form-grid">
        <label class="full">Name<input id="r-name" /></label>
        <label class="full">Description<input id="r-desc" /></label>
        <label>Category<select id="r-cat"><option>Food</option><option>Merchandise</option><option>Services</option><option>Experiences</option><option>Discounts</option></select></label>
        <label>Points<input id="r-cost" type="number" min="1" value="50" /></label>
      </div>
      <div class="form-actions"><button class="btn ghost" data-cancel="r-box">Cancel</button><button class="btn" id="r-add">Add reward</button></div>
    </div>
    ${pills(["All", "Food", "Merchandise", "Services", "Experiences", "Discounts"].map(f => [f, f === "All" ? "All rewards" : f]), state.filter)}
    ${filtered.length ? `<div class="grid-3">${filtered.map(r => {
      const need = r.pointsCost - state.user.points;
      const pct = Math.min(100, Math.round(state.user.points / Math.max(1, r.pointsCost) * 100));
      return `<article class="card hover" style="display:flex;flex-direction:column;gap:10px">
        <div class="between"><span class="tile ${rewardTint[r.category] || "gold"}">${icon(rewardIcon[r.category] || "rewards")}</span><span class="tag gold">${r.pointsCost} pts</span></div>
        <div><h3>${esc(r.name)}</h3><p class="muted small" style="margin-top:4px">${esc(r.description)}</p></div>
        <div class="muted small">${r.stock} left · ${esc(r.category)}</div>
        ${student ? `<div class="card-foot" style="flex-direction:column;align-items:stretch">
          ${need > 0 ? `<div class="progress"><i style="width:${pct}%"></i></div><button class="btn wide" disabled>${need} more points needed</button>`
            : `<button class="btn wide redeem" data-id="${r.id}" data-cost="${r.pointsCost}" data-name="${esc(r.name)}">Redeem</button>`}
        </div>` : ""}
      </article>`;
    }).join("")}</div>` : emptyState("No rewards in this category", "Try another category.", "rewards", "gold")}

    ${redemptions.length ? `<div class="section-head"><h2>Your vouchers</h2></div>
      <div class="list">${redemptions.map(r => `<div class="list-row">
        <span class="tile gold">${icon("rewards")}</span>
        <div class="grow"><b>${esc(r.rewardName)}</b><div class="muted">${r.pointsCost} pts · ${fmtDate(r.createdAt)}</div></div>
        <div class="end"><span class="voucher">${esc(r.voucherCode)}</span><span class="tag dot ${r.isUsed ? "leaf" : r.isVoided ? "slate" : "gold"}">${r.isUsed ? "Used" : r.isVoided ? "Voided" : "Ready to use"}</span></div>
      </div>`).join("")}</div>` : ""}`;

  main.querySelectorAll("[data-filter]").forEach(b => b.onclick = () => { state.filter = b.dataset.filter; rewards(main); });
  bindToggles(main, { "toggle-r": "r-box" });
  const ra = document.getElementById("r-add");
  if (ra) ra.onclick = async () => {
    if (!val("r-name").trim()) { toast("Give the reward a name.", "error"); return; }
    await api("/api/rewards", { method: "POST", body: JSON.stringify({ name: val("r-name"), description: val("r-desc"), category: val("r-cat"), pointsCost: Number(val("r-cost")) }) });
    toast("Reward added.");
    rewards(main);
  };
  main.querySelectorAll(".redeem").forEach(el => el.onclick = () => {
    const confirm = openDialog(`<h3>Redeem ${el.dataset.name}?</h3>
      <p>This uses ${el.dataset.cost} of your ${state.user.points} points. Redemptions are final, so only the residence admin can void a voucher.</p>
      <div class="form-actions"><button class="btn ghost" data-close>Not now</button><button class="btn" id="confirm-redeem">Redeem for ${el.dataset.cost} pts</button></div>`);
    confirm.el.querySelector("#confirm-redeem").onclick = async () => {
      confirm.close();
      const result = await api(`/api/rewards/${el.dataset.id}/redeem`, { method: "POST" });
      state.user.points -= Number(el.dataset.cost);
      save(state.user);
      openDialog(`<h3>Enjoy your reward</h3><p>Show this voucher code when you collect ${el.dataset.name}. You can find it again under Your vouchers.</p>
        <div class="code">${esc(result.voucherCode)}</div>
        <div class="form-actions"><button class="btn" data-close>Done</button></div>`);
      rewards(main);
    };
  });
}

async function admin(main) {
  const users = await api("/api/admin/users");
  const analytics = await api("/api/admin/analytics");
  const posts = await api("/api/posts");
  const events = await api("/api/events");
  const market = await api("/api/marketplace");
  const rewards = await api("/api/rewards");
  const redemptionAdmin = await api("/api/rewards/redemptions/admin");
  const tab = ["residents", "announcements", "redemptions"].includes(state.tab) ? state.tab : "overview";
  const pendingCodes = redemptionAdmin.filter(r => !r.isUsed && !r.isVoided).length;

  main.innerHTML = `
    ${pageHead("Admin Panel", "Manage residents, announcements and rewards for your residence.", "", esc(state.user.residenceName || "Residence"))}
    ${pills([["overview", "Overview"], ["residents", `Residents<span class="count">${users.length}</span>`], ["announcements", "Announcements"], ["redemptions", `Redemptions<span class="count">${pendingCodes}</span>`]], tab, "tab")}
    ${tab === "overview" ? `
      <div class="grid-3">
        ${metric("Residents", analytics.students, "groups", "leaf")}
        ${metric("Open maintenance requests", analytics.openTickets, "maintenance", "gold")}
        ${metric("Events created", events.length, "events", "sky")}
        ${metric("Community posts", posts.length, "community", "leaf")}
        ${metric("Marketplace listings", market.length, "market", "plum")}
        ${metric("Active rewards", rewards.length, "rewards", "gold")}
      </div>
    ` : tab === "residents" ? `
      <div class="list">${users.map(u => `<div class="list-row">
        <div class="avatar">${initial(u.fullName)}</div>
        <div class="grow"><b>${esc(u.fullName)}</b><div class="muted">${esc(u.email)}${u.room ? " · Room " + esc(u.room) : ""}</div></div>
        <div class="end">
          <span class="tag ${u.isActive ? tagClass(u.role) : "slate"}">${esc(u.isActive ? u.role : "Inactive")}</span>
          <button class="btn ghost sm reset-link" data-id="${u.id}" data-name="${esc(u.fullName)}">${icon("key")}Reset link</button>
          <button class="btn ${u.isActive ? "subtle-danger" : "soft"} sm act" data-id="${u.id}" data-on="${u.isActive}">${u.isActive ? "Deactivate" : "Activate"}</button>
        </div>
      </div>`).join("")}</div>
    ` : tab === "announcements" ? `
      <div class="card form-card">
        <h3>New announcement</h3>
        <textarea id="a-body" placeholder="Write an announcement for every resident"></textarea>
        <div class="form-actions"><button class="btn" id="a-post">Post announcement</button></div>
      </div>
      ${posts.filter(p => p.kind === "Announcement").map(p => `<div class="notice"><span class="tile plum">${icon("notifications")}</span><div><b>${esc(p.title)}</b><p>${esc(p.body)}</p><p class="muted small">${fmtDate(p.createdAt)}</p></div></div>`).join("") || emptyState("No announcements yet", "Post one above and every resident will see it on their dashboard.", "notifications", "plum")}
    ` : `
      ${redemptionAdmin.length ? `<div class="list">${redemptionAdmin.map(r => `<div class="list-row">
        <span class="tile gold">${icon("rewards")}</span>
        <div class="grow"><b>${esc(r.userName)}</b><div class="muted">${esc(r.rewardName)} · ${r.pointsCost} pts</div></div>
        <div class="end">
          <span class="voucher">${esc(r.voucherCode)}</span>
          ${r.isUsed ? `<span class="tag leaf dot">Used</span>` : r.isVoided ? `<span class="tag slate dot">Voided</span>` : `<span class="tag gold dot">Pending</span>
            <button class="btn sm mark-used" data-id="${r.id}">${icon("check")}Mark used</button>
            <button class="btn subtle-danger sm void-redemption" data-id="${r.id}">Void</button>`}
        </div>
      </div>`).join("")}</div>` : emptyState("No reward redemptions yet", "Vouchers appear here when students redeem rewards.", "rewards", "gold")}
    `}`;

  main.querySelectorAll("[data-tab]").forEach(b => b.onclick = () => { state.tab = b.dataset.tab; admin(main); });
  main.querySelectorAll(".reset-link").forEach(el => el.onclick = async () => {
    const result = await api(`/api/admin/users/${el.dataset.id}/reset-link`, { method: "POST" });
    const copied = await navigator.clipboard?.writeText(result.link).then(() => true, () => false);
    const box = openDialog(`<h3>Reset link for ${el.dataset.name}</h3>
      <p>Send this one-time link to the resident, for example on WhatsApp. It works once and expires in 30 minutes.</p>
      <div class="copy-row"><input id="reset-url" readonly value="${esc(result.link)}" /><button class="btn" id="copy-reset">${icon("copy")}<span>${copied ? "Copied" : "Copy"}</span></button></div>
      <div class="form-actions"><button class="btn ghost" data-close>Done</button></div>`);
    const input = box.el.querySelector("#reset-url");
    input.onfocus = () => input.select();
    box.el.querySelector("#copy-reset").onclick = async e => {
      input.select();
      const ok = await navigator.clipboard?.writeText(result.link).then(() => true, () => false);
      if (!ok) document.execCommand("copy");
      e.currentTarget.querySelector("span").textContent = "Copied";
    };
  });
  main.querySelectorAll(".act").forEach(el => el.onclick = async () => {
    await api(`/api/admin/users/${el.dataset.id}/active`, { method: "PUT", body: JSON.stringify({ isActive: el.dataset.on !== "true" }) });
    admin(main);
  });
  const ap = document.getElementById("a-post");
  if (ap) ap.onclick = async () => {
    if (!val("a-body").trim()) { toast("Write the announcement first.", "error"); return; }
    await api("/api/posts", { method: "POST", body: JSON.stringify({ title: "Announcement", body: val("a-body"), kind: "Announcement", isPoll: false }) });
    toast("Announcement posted.");
    admin(main);
  };
  main.querySelectorAll(".mark-used").forEach(el => el.onclick = async () => { await api(`/api/rewards/redemptions/${el.dataset.id}/mark-used`, { method: "POST" }); admin(main); });
  main.querySelectorAll(".void-redemption").forEach(el => el.onclick = async () => { await api(`/api/rewards/redemptions/${el.dataset.id}/void`, { method: "POST" }); toast("Voucher voided and points returned."); admin(main); });
}
const metric = (label, n, name, tint) => `<div class="card stat"><span class="tile ${tint}">${icon(name)}</span><div><b>${n}</b><span>${label}</span></div></div>`;

async function notifications(main) {
  const items = await api("/api/notifications");
  main.innerHTML = `${pageHead("Notifications", "Residence alerts and updates on your work.")}
    ${items.length ? `<div class="list">${items.map(n => `<div class="list-row" style="align-items:flex-start">
      <span class="tile ${n.isRead ? "slate" : "sky"}">${icon("notifications")}</span>
      <div class="grow"><b>${esc(n.title)}</b><div class="muted">${esc(n.body)}</div></div>
      <div class="end">${n.isRead ? "" : `<span class="tag sky dot">New</span>`}<span class="muted small">${timeAgo(n.createdAt)}</span></div>
    </div>`).join("")}</div>` : emptyState("You're all caught up", "New alerts and ticket updates will appear here.", "notifications", "sky")}`;
}

function profile(main) {
  const u = state.user;
  const details = [
    ["Email", u.email], ["Role", u.role], ["Residence", u.residenceName], ["Room", u.room],
    ["Student number", u.studentNumber], ["Staff ID", u.staffId]
  ].filter(([, v]) => v);
  main.innerHTML = `
    ${pageHead("Profile", "Your account and residence details.")}
    <div class="card mb">
      <div class="profile-hero">
        <div class="big">${initials(u.fullName)}</div>
        <div><h2 style="font-size:22px">${esc(u.fullName)}</h2><div class="tags" style="margin-top:6px"><span class="tag ${tagClass(u.role)}">${esc(u.role)}</span><span class="tag gold">${u.points} points</span></div></div>
      </div>
      <div class="kv">${details.map(([k, v]) => `<div><small>${k}</small><b>${esc(v)}</b></div>`).join("")}</div>
    </div>
    <button class="btn subtle-danger lg" id="logout2">${icon("logout")}<span>Sign out of ResLink</span></button>`;
  document.getElementById("logout2").onclick = () => { save(null); state.auth = "signin"; render(); };
}

render();
