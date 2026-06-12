/* ============================================================
   NEOP 用户端 - API 工具 & 公共脚本
   ============================================================ */

const API_BASE = 'http://localhost:8080';

/* ---- Storage ---- */
function getToken() {
  return localStorage.getItem('neop_token');
}

function setToken(token) {
  localStorage.setItem('neop_token', token);
}

function removeToken() {
  localStorage.removeItem('neop_token');
  localStorage.removeItem('neop_user');
}

function getUser() {
  try {
    return JSON.parse(localStorage.getItem('neop_user') || 'null');
  } catch { return null; }
}

function setUser(user) {
  localStorage.setItem('neop_user', JSON.stringify(user));
}

/* ---- API Request ---- */
async function api(path, options = {}) {
  const { method = 'GET', body, auth = true } = options;
  const headers = { 'Content-Type': 'application/json' };

  const token = getToken();
  if (auth && token) {
    headers['Authorization'] = 'Bearer ' + token;
  }

  const config = { method, headers };
  if (body) config.body = JSON.stringify(body);

  try {
    const res = await fetch(API_BASE + path, config);
    const data = await res.json();
    return data;
  } catch (err) {
    return { code: -1, msg: '网络错误：' + err.message, data: null };
  }
}

/* ---- Public APIs (no auth) ---- */
const PublicAPI = {
  taskList: (params = {}) => {
    const qs = new URLSearchParams();
    if (params.current) qs.set('current', params.current);
    if (params.size) qs.set('size', params.size);
    if (params.keyword) qs.set('keyword', params.keyword);
    return api('/api/task/list?' + qs.toString(), { auth: false });
  },
  taskInfo: (id) => api('/api/task/info/' + id, { auth: false }),
  categoryList: () => api('/api/shop/category/list', { auth: false }),
  productList: (params = {}) => {
    const qs = new URLSearchParams();
    if (params.current) qs.set('current', params.current);
    if (params.size) qs.set('size', params.size);
    if (params.categoryId) qs.set('categoryId', params.categoryId);
    return api('/api/shop/product/list?' + qs.toString(), { auth: false });
  },
  productInfo: (id) => api('/api/shop/product/info/' + id, { auth: false }),
  productSearch: (params = {}) => {
    const qs = new URLSearchParams();
    if (params.current) qs.set('current', params.current);
    if (params.size) qs.set('size', params.size);
    if (params.keyword) qs.set('keyword', params.keyword);
    return api('/api/shop/product/search?' + qs.toString(), { auth: false });
  },
};

/* ---- Auth APIs ---- */
const AuthAPI = {
  login: (phone, password) => api('/api/auth/login', {
    method: 'POST', body: { phone, password }, auth: false,
  }),
  register: (phone, password, inviteCode) => api('/api/auth/register', {
    method: 'POST', body: { phone, password, inviteCode }, auth: false,
  }),
  sendCode: (phone) => api('/api/auth/send-code', {
    method: 'POST', body: { phone }, auth: false,
  }),
};

/* ---- Authenticated APIs ---- */
const UserAPI = {
  info: () => api('/api/user/info'),
  update: (data) => api('/api/user/update', { method: 'PUT', body: data }),
  pointsBalance: () => api('/api/user/points/balance'),
  pointsDetail: (params = {}) => {
    const qs = new URLSearchParams();
    if (params.current) qs.set('current', params.current);
    if (params.size) qs.set('size', params.size);
    if (params.type) qs.set('type', params.type);
    return api('/api/user/points/detail?' + qs.toString());
  },
  signInfo: () => api('/api/user/sign/info'),
  signDo: () => api('/api/user/sign/do', { method: 'POST' }),
  inviteInfo: () => api('/api/user/invite/info'),
  inviteList: (params = {}) => {
    const qs = new URLSearchParams();
    if (params.current) qs.set('current', params.current);
    if (params.size) qs.set('size', params.size);
    return api('/api/user/invite/list?' + qs.toString());
  },
};

const TaskAPI = {
  receive: (taskId) => api('/api/task/receive', {
    method: 'POST', body: { taskId }
  }),
  submit: (receiveId, submitImages, submitNote) => api('/api/task/submit', {
    method: 'POST', body: { receiveId, submitImages, submitNote }
  }),
  myList: (params = {}) => {
    const qs = new URLSearchParams();
    if (params.current) qs.set('current', params.current);
    if (params.size) qs.set('size', params.size);
    if (params.auditStatus !== undefined && params.auditStatus !== '') qs.set('auditStatus', params.auditStatus);
    return api('/api/task/my/list?' + qs.toString());
  },
  withdrawApply: (receiveId) => api('/api/task/withdraw/apply', {
    method: 'POST', body: { receiveId }
  }),
  withdrawLog: (params = {}) => {
    const qs = new URLSearchParams();
    if (params.current) qs.set('current', params.current);
    if (params.size) qs.set('size', params.size);
    return api('/api/task/withdraw/log?' + qs.toString());
  },
};

/* ---- Utils ---- */
function formatDate(str) {
  if (!str) return '-';
  const d = new Date(str);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function formatMoney(val) {
  if (val == null) return '¥0.00';
  return '¥' + Number(val).toFixed(2);
}

function formatPoint(val) {
  if (val == null) return '0';
  return Number(val).toLocaleString();
}

function truncate(str, len = 50) {
  if (!str) return '';
  return str.length > len ? str.slice(0, len) + '...' : str;
}

function showToast(msg, type = 'success') {
  const existing = document.querySelector('.toast');
  if (existing) existing.remove();
  const toast = document.createElement('div');
  toast.className = 'toast toast-' + type;
  toast.textContent = msg;
  document.body.appendChild(toast);
  requestAnimationFrame(() => toast.classList.add('show'));
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 2500);
}

function statusText(status, map) {
  return (map && map[status]) ? map[status] : '未知';
}

function handleAuthError(res) {
  if (res.code === 401) {
    removeToken();
    showToast('登录已过期，请重新登录', 'error');
    setTimeout(() => {
      window.location.href = 'user.html';
    }, 1500);
    return true;
  }
  return false;
}

/* ---- Render Header ---- */
function renderHeader(currentPage) {
  const header = document.querySelector('[data-header]');
  if (!header) return;

  const user = getUser();
  const userHtml = user
    ? `<div class="user-badge">
         <div class="avatar-mini">${(user.nickname || 'U')[0]}</div>
         <span>${user.nickname || '用户'}</span>
         <button class="btn btn-sm btn-outline" onclick="doLogout()" style="padding:3px 10px;font-size:0.75rem;">退出</button>
       </div>`
    : `<a href="user.html" class="btn btn-primary btn-sm">登录</a>`;

  header.innerHTML = `
    <div class="header">
      <div class="header-inner">
        <a href="index.html" class="logo">NEO<span>P</span></a>
        <button class="nav-toggle" onclick="this.nextElementSibling.classList.toggle('open')">☰</button>
        <nav class="nav-links">
          <a href="index.html" class="${currentPage === 'index' ? 'active' : ''}">首页</a>
          <a href="task.html" class="${currentPage === 'task' ? 'active' : ''}">任务大厅</a>
          <a href="product.html" class="${currentPage === 'product' ? 'active' : ''}">积分商城</a>
          <a href="user.html" class="${currentPage === 'user' ? 'active' : ''}">个人中心</a>
        </nav>
        <div class="user-actions">${userHtml}</div>
      </div>
    </div>
  `;
}

function doLogout() {
  removeToken();
  showToast('已退出登录');
  setTimeout(() => location.reload(), 800);
}

/* ---- Pagination Component ---- */
function renderPagination(container, page, total, onChange) {
  const pages = Math.ceil(total / page.size) || 0;
  if (pages <= 1) { container.innerHTML = ''; return; }

  let html = '';
  html += `<button class="page-btn" ${page.current <= 1 ? 'disabled' : ''} onclick="void(0)" data-p="${page.current - 1}">上一页</button>`;

  const maxShow = 5;
  let start = Math.max(1, page.current - Math.floor(maxShow / 2));
  let end = Math.min(pages, start + maxShow - 1);
  if (end - start < maxShow - 1) start = Math.max(1, end - maxShow + 1);

  for (let i = start; i <= end; i++) {
    html += `<button class="page-btn${i === page.current ? ' active' : ''}" onclick="void(0)" data-p="${i}">${i}</button>`;
  }

  html += `<button class="page-btn" ${page.current >= pages ? 'disabled' : ''} onclick="void(0)" data-p="${page.current + 1}">下一页</button>`;
  html += `<span class="text-muted text-sm" style="margin-left:8px;">共 ${total} 条</span>`;

  container.innerHTML = html;
  container.querySelectorAll('.page-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const p = parseInt(btn.dataset.p);
      if (p > 0 && p <= pages) onChange(p);
    });
  });
}

/* ---- Auto-init header ---- */
document.addEventListener('DOMContentLoaded', () => {
  const page = document.body.dataset.page || '';
  renderHeader(page);
});
