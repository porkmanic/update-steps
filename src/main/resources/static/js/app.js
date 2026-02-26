// 状态管理
const state = {
    token: localStorage.getItem('token'),
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    currentVersion: null,
    versions: [],
    archivedVersions: [],
    users: [],
    steps: [],
    filterMySteps: false // 只看自己筛选状态
};

// 切换只看自己筛选
function toggleMyStepsFilter() {
    state.filterMySteps = document.getElementById('filter-my-steps').checked;
    renderSteps();
}

// API基础URL
const API_BASE = '/api';

// API请求封装
async function api(endpoint, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
    });

    if (response.status === 401 || response.status === 403) {
        logout();
        throw new Error('登录已过期，请重新登录');
    }

    const data = await response.json();

    if (!data.success) {
        throw new Error(data.message || '操作失败');
    }

    return data;
}

// 初始化
document.addEventListener('DOMContentLoaded', async () => {
    if (state.token && state.user) {
        showMainPage();
        await loadVersions();

        // 恢复上次选中的版本
        const savedVersionId = sessionStorage.getItem('currentVersionId');
        if (savedVersionId) {
            try {
                await openVersion(parseInt(savedVersionId));
            } catch (e) {
                // 版本可能已被删除，清除保存的ID
                sessionStorage.removeItem('currentVersionId');
            }
        }
    } else {
        showLoginPage();
    }

    setupEventListeners();
});

// 事件监听
function setupEventListeners() {
    // 登录表单
    document.getElementById('login-form').addEventListener('submit', handleLogin);

    // 退出登录
    document.getElementById('logout-btn').addEventListener('click', logout);

    // 修改密码
    document.getElementById('change-password-btn').addEventListener('click', showChangePasswordModal);

    // 导航菜单
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const view = item.dataset.view;
            switchView(view);
        });
    });

    // 创建版本
    document.getElementById('create-version-btn').addEventListener('click', showCreateVersionModal);

    // 创建用户
    document.getElementById('create-user-btn').addEventListener('click', showCreateUserModal);

    // 返回列表
    document.getElementById('back-to-list').addEventListener('click', () => {
        document.getElementById('version-detail-view').classList.add('hidden');
        document.getElementById('versions-view').classList.remove('hidden');
        // 清除保存的版本ID
        sessionStorage.removeItem('currentVersionId');
        loadVersions();
    });

    // 添加步骤
    document.getElementById('add-step-btn').addEventListener('click', showAddStepModal);

    // UAT按钮
    document.getElementById('uat-btn').addEventListener('click', handleUatToggle);

    // 归档按钮
    document.getElementById('archive-btn').addEventListener('click', handleArchiveToggle);

    // 删除版本按钮
    document.getElementById('delete-version-btn').addEventListener('click', handleDeleteVersion);

    // 模态框关闭
    document.getElementById('modal-close').addEventListener('click', closeModal);
    document.getElementById('modal-overlay').addEventListener('click', (e) => {
        if (e.target === e.currentTarget) closeModal();
    });
}

// 登录处理
async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const result = await api('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });

        state.token = result.data.token;
        state.user = {
            id: result.data.id,
            username: result.data.username,
            name: result.data.name,
            displayName: result.data.displayName,
            role: result.data.role
        };

        localStorage.setItem('token', state.token);
        localStorage.setItem('user', JSON.stringify(state.user));

        showMainPage();
        loadVersions();
        showToast('登录成功', 'success');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 退出登录
function logout() {
    state.token = null;
    state.user = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    showLoginPage();
}

// 页面切换
function showLoginPage() {
    document.getElementById('login-page').classList.remove('hidden');
    document.getElementById('main-page').classList.add('hidden');
}

function showMainPage() {
    document.getElementById('login-page').classList.add('hidden');
    document.getElementById('main-page').classList.remove('hidden');

    // 更新用户信息显示 - 优先显示姓名
    document.getElementById('user-name').textContent = state.user.displayName || state.user.username;
    document.getElementById('user-role').textContent = state.user.role === 'ADMIN' ? '管理员' : '开发者';
    document.getElementById('user-role').className = `user-badge ${state.user.role.toLowerCase()}`;

    // 根据角色显示/隐藏管理功能
    const isAdmin = state.user.role === 'ADMIN';
    document.querySelectorAll('.admin-only').forEach(el => {
        el.style.display = isAdmin ? '' : 'none';
    });
}

// 视图切换
function switchView(view) {
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.toggle('active', item.dataset.view === view);
    });

    document.querySelectorAll('.view').forEach(v => v.classList.add('hidden'));
    document.getElementById(`${view}-view`).classList.remove('hidden');

    if (view === 'versions') loadVersions();
    else if (view === 'archived') loadArchivedVersions();
    else if (view === 'users') loadUsers();
}

// 加载版本列表
async function loadVersions() {
    try {
        const result = await api('/versions');
        state.versions = result.data;
        renderVersions();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 渲染版本列表
function renderVersions() {
    const container = document.getElementById('versions-list');

    if (state.versions.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
                </svg>
                <h4>暂无开发中的版本</h4>
                <p>点击"新建版本"开始管理更新步骤</p>
            </div>
        `;
        return;
    }

    container.innerHTML = state.versions.map(version => `
        <div class="version-card" onclick="openVersion(${version.id})">
            <div class="version-card-header">
                <span class="version-number">${version.versionNumber}</span>
                <span class="status-badge ${version.status.toLowerCase()}">${getStatusText(version.status)}</span>
            </div>
            <div class="version-meta">
                <div class="version-meta-item">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
                    </svg>
                    <span>${version.stepCount} 个步骤</span>
                    ${version.newStepCount > 0 ? `<span class="status-badge new">+${version.newStepCount} 新增</span>` : ''}
                </div>
                <div class="version-meta-item">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                    </svg>
                    <span>创建于 ${formatDate(version.createdAt)}</span>
                </div>
            </div>
        </div>
    `).join('');
}

// 加载归档版本
async function loadArchivedVersions() {
    try {
        const result = await api('/versions/archived');
        state.archivedVersions = result.data;
        renderArchivedVersions();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 渲染归档版本
function renderArchivedVersions() {
    const container = document.getElementById('archived-list');

    if (state.archivedVersions.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4"/>
                </svg>
                <h4>暂无历史版本</h4>
                <p>归档的版本将显示在这里</p>
            </div>
        `;
        return;
    }

    container.innerHTML = state.archivedVersions.map(version => `
        <div class="version-card" onclick="openVersion(${version.id})">
            <div class="version-card-header">
                <span class="version-number">${version.versionNumber}</span>
                <span class="status-badge archived">已归档</span>
            </div>
            <div class="version-meta">
                <div class="version-meta-item">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
                    </svg>
                    <span>${version.stepCount} 个步骤</span>
                </div>
                <div class="version-meta-item">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                    </svg>
                    <span>归档于 ${formatDate(version.archivedAt)}</span>
                </div>
            </div>
        </div>
    `).join('');
}

// 打开版本详情
async function openVersion(id) {
    try {
        const result = await api(`/versions/${id}`);
        state.currentVersion = result.data;

        // 保存当前版本ID到sessionStorage，刷新后恢复
        sessionStorage.setItem('currentVersionId', id.toString());

        document.getElementById('versions-view').classList.add('hidden');
        document.getElementById('archived-view').classList.add('hidden');
        document.getElementById('version-detail-view').classList.remove('hidden');

        document.getElementById('version-title').textContent = state.currentVersion.versionNumber;
        document.getElementById('version-status').textContent = getStatusText(state.currentVersion.status);
        document.getElementById('version-status').className = `status-badge ${state.currentVersion.status.toLowerCase()}`;

        // 更新UAT按钮
        const uatBtn = document.getElementById('uat-btn');
        if (state.currentVersion.status === 'UAT') {
            uatBtn.textContent = '取消UAT';
            uatBtn.className = 'btn btn-secondary admin-only';
        } else if (state.currentVersion.status === 'DEV') {
            uatBtn.textContent = '开始UAT';
            uatBtn.className = 'btn btn-secondary admin-only';
        } else {
            uatBtn.style.display = 'none';
        }

        // 更新归档按钮
        const archiveBtn = document.getElementById('archive-btn');
        if (state.currentVersion.status === 'ARCHIVED') {
            archiveBtn.textContent = '恢复';
            archiveBtn.className = 'btn btn-secondary admin-only';
        } else {
            archiveBtn.textContent = '归档';
            archiveBtn.className = 'btn btn-warning admin-only';
        }

        // 添加步骤按钮在归档状态下隐藏
        document.getElementById('add-step-btn').style.display =
            state.currentVersion.status === 'ARCHIVED' ? 'none' : '';

        // 重新应用admin-only
        const isAdmin = state.user.role === 'ADMIN';
        document.querySelectorAll('.admin-only').forEach(el => {
            el.style.display = isAdmin ? '' : 'none';
        });

        await loadSteps();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 加载步骤
async function loadSteps() {
    try {
        const result = await api(`/steps/version/${state.currentVersion.id}`);
        state.steps = result.data;
        renderSteps();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 渲染步骤
function renderSteps() {
    const container = document.getElementById('steps-list');
    const isAdmin = state.user.role === 'ADMIN';

    // 应用筛选
    let filteredSteps = state.steps;
    if (state.filterMySteps) {
        filteredSteps = state.steps.filter(step => step.userId === state.user.id);
    }

    if (filteredSteps.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7v4m0 0l-2-2m2 2l2-2"/>
                </svg>
                <h4>${state.filterMySteps ? '没有您创建的步骤' : '暂无更新步骤'}</h4>
                <p>${state.filterMySteps ? '关闭筛选查看全部步骤' : '点击"添加步骤"开始记录'}</p>
            </div>
        `;
        return;
    }

    container.innerHTML = filteredSteps.map(step => {
        // 可编辑条件：本人创建或管理员 + 未锁定 + 未归档 + UAT未确认执行
        const canEdit = (step.userId === state.user.id || isAdmin) && !step.locked && !step.uatConfirmed && state.currentVersion.status !== 'ARCHIVED';
        const canDelete = canEdit;

        return `
            <div class="step-card ${step.locked ? 'locked' : ''} ${step.afterUat ? 'after-uat' : ''}">
                <div class="step-header">
                    <div class="step-author">
                        <div class="step-avatar">${(step.userDisplayName || step.username).charAt(0).toUpperCase()}</div>
                        <div class="step-author-info">
                            <span class="step-author-name">${step.userDisplayName || step.username}</span>
                            <span class="step-time">${formatDateTime(step.createdAt)}</span>
                        </div>
                    </div>
                    <div class="step-badges">
                        ${step.locked ? '<span class="status-badge uat">已锁定</span>' : ''}
                        ${step.afterUat ? '<span class="status-badge new">UAT后新增</span>' : ''}
                        ${step.uatConfirmed ? '<span class="status-badge" style="background: rgba(16, 185, 129, 0.15); color: #10b981;">UAT已执行</span>' : ''}
                        ${step.prodConfirmed ? '<span class="status-badge" style="background: rgba(99, 102, 241, 0.15); color: #6366f1;">生产已执行</span>' : ''}
                    </div>
                </div>
                <div class="step-content">${step.content}</div>
                ${step.attachments && step.attachments.length > 0 ? `
                    <div class="step-attachments">
                        <div class="attachment-list">
                            ${step.attachments.map(att => `
                                <a href="${att.filepath}" target="_blank" class="attachment-item">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <path d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"/>
                                    </svg>
                                    ${att.filename}
                                </a>
                            `).join('')}
                        </div>
                    </div>
                ` : ''}
                <div class="step-actions">
                    ${canEdit ? `<button class="btn btn-sm btn-secondary" onclick="editStep(${step.id})">编辑</button>` : ''}
                    ${canDelete ? `<button class="btn btn-sm btn-danger" onclick="deleteStep(${step.id})">删除</button>` : ''}
                    ${isAdmin && !step.uatConfirmed ? `<button class="btn btn-sm btn-secondary" onclick="confirmUat(${step.id})">确认UAT执行</button>` : ''}
                    ${isAdmin && step.uatConfirmed && !step.prodConfirmed ? `<button class="btn btn-sm btn-secondary" onclick="cancelUatConfirm(${step.id})">取消UAT确认</button>` : ''}
                    ${isAdmin && step.uatConfirmed && !step.prodConfirmed ? `<button class="btn btn-sm btn-primary" onclick="confirmProd(${step.id})">确认生产执行</button>` : ''}
                    ${isAdmin && step.prodConfirmed ? `<button class="btn btn-sm btn-secondary" onclick="cancelProdConfirm(${step.id})">取消生产确认</button>` : ''}
                </div>
            </div>
        `;
    }).join('');
}

// 确认UAT执行
async function confirmUat(stepId) {
    try {
        await api(`/steps/${stepId}/confirm-uat`, { method: 'POST' });
        showToast('UAT执行已确认', 'success');
        await loadSteps();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 取消UAT确认
async function cancelUatConfirm(stepId) {
    try {
        await api(`/steps/${stepId}/cancel-uat-confirm`, { method: 'POST' });
        showToast('UAT确认已取消', 'success');
        await loadSteps();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 确认生产执行
async function confirmProd(stepId) {
    try {
        await api(`/steps/${stepId}/confirm-prod`, { method: 'POST' });
        showToast('生产执行已确认', 'success');
        await loadSteps();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 取消生产确认
async function cancelProdConfirm(stepId) {
    try {
        await api(`/steps/${stepId}/cancel-prod-confirm`, { method: 'POST' });
        showToast('生产确认已取消', 'success');
        await loadSteps();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 加载用户列表
async function loadUsers() {
    try {
        const result = await api('/users');
        state.users = result.data;
        renderUsers();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 渲染用户列表
function renderUsers() {
    const container = document.getElementById('users-list');

    container.innerHTML = `
        <table class="users-table">
            <thead>
                <tr>
                    <th>用户名</th>
                    <th>姓名</th>
                    <th>角色</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                ${state.users.map(user => `
                    <tr>
                        <td>${user.username}</td>
                        <td>${user.name || '-'}</td>
                        <td>
                            <span class="user-badge ${user.role.toLowerCase()}">
                                ${user.role === 'ADMIN' ? '管理员' : '开发者'}
                            </span>
                        </td>
                        <td>${formatDate(user.createdAt)}</td>
                        <td>
                            <button class="btn btn-sm btn-secondary" onclick="resetPassword(${user.id})">重置密码</button>
                            ${user.role !== 'ADMIN' || state.users.filter(u => u.role === 'ADMIN').length > 1 ?
            `<button class="btn btn-sm btn-danger" onclick="deleteUser(${user.id})">删除</button>` : ''}
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

// 模态框操作
function showModal(title, body, footer, isLarge = false) {
    document.getElementById('modal-title').textContent = title;
    document.getElementById('modal-body').innerHTML = body;
    document.getElementById('modal-footer').innerHTML = footer;
    document.getElementById('modal').className = isLarge ? 'modal large' : 'modal';
    document.getElementById('modal-overlay').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('modal-overlay').classList.add('hidden');
}

// 修改密码模态框
function showChangePasswordModal() {
    showModal('修改密码', `
        <form id="change-password-form">
            <div class="form-group">
                <label>旧密码</label>
                <input type="password" id="old-password" required placeholder="请输入旧密码">
            </div>
            <div class="form-group">
                <label>新密码</label>
                <input type="password" id="new-password" required placeholder="请输入新密码">
            </div>
        </form>
    `, `
        <button class="btn btn-secondary" onclick="closeModal()">取消</button>
        <button class="btn btn-primary" onclick="changePassword()">确认修改</button>
    `);
}

async function changePassword() {
    const oldPassword = document.getElementById('old-password').value;
    const newPassword = document.getElementById('new-password').value;

    try {
        await api('/auth/change-password', {
            method: 'POST',
            body: JSON.stringify({ oldPassword, newPassword })
        });
        closeModal();
        showToast('密码修改成功', 'success');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 创建版本模态框
function showCreateVersionModal() {
    showModal('新建版本', `
        <form id="create-version-form" onsubmit="event.preventDefault(); createVersion();">
            <div class="form-group">
                <label>版本号</label>
                <input type="text" id="version-number" required placeholder="例如：v1.0.0">
            </div>
        </form>
    `, `
        <button class="btn btn-secondary" onclick="closeModal()">取消</button>
        <button class="btn btn-primary" onclick="createVersion()">创建</button>
    `);

    // 输入框可以回车提交
    setTimeout(() => {
        const input = document.getElementById('version-number');
        if (input) {
            input.focus();
            input.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    createVersion();
                }
            });
        }
    }, 100);
}

async function createVersion() {
    const versionNumber = document.getElementById('version-number').value;

    try {
        await api('/versions', {
            method: 'POST',
            body: JSON.stringify({ versionNumber })
        });
        closeModal();
        showToast('版本创建成功', 'success');
        loadVersions();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 创建用户模态框 - 支持姓名和角色选择
function showCreateUserModal() {
    showModal('添加用户', `
        <form id="create-user-form">
            <div class="form-group">
                <label>用户名 <span style="color: var(--error);">*</span></label>
                <input type="text" id="new-username" required placeholder="请输入用户名">
            </div>
            <div class="form-group">
                <label>姓名</label>
                <input type="text" id="new-name" placeholder="请输入姓名（可选）">
            </div>
            <div class="form-group">
                <label>角色</label>
                <select id="new-role" style="width: 100%; padding: 14px 18px; background: var(--bg-tertiary); border: 1px solid var(--border-color); border-radius: var(--radius-md); color: var(--text-primary); font-size: 1rem;">
                    <option value="DEVELOPER">普通开发</option>
                    <option value="ADMIN">管理员</option>
                </select>
            </div>
            <p style="color: var(--text-muted); font-size: 0.875rem;">初始密码：nbcb,111</p>
        </form>
    `, `
        <button class="btn btn-secondary" onclick="closeModal()">取消</button>
        <button class="btn btn-primary" onclick="createUser()">创建</button>
    `);
}

async function createUser() {
    const username = document.getElementById('new-username').value;
    const name = document.getElementById('new-name').value;
    const role = document.getElementById('new-role').value;

    try {
        await api('/users', {
            method: 'POST',
            body: JSON.stringify({ username, name, role })
        });
        closeModal();
        showToast('用户创建成功，初始密码为 nbcb,111', 'success');
        loadUsers();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 重置密码
async function resetPassword(userId) {
    if (!confirm('确定要重置该用户的密码吗？')) return;

    try {
        await api(`/users/${userId}/reset-password`, { method: 'POST' });
        showToast('密码已重置为 nbcb,111', 'success');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 删除用户
async function deleteUser(userId) {
    if (!confirm('确定要删除该用户吗？')) return;

    try {
        await api(`/users/${userId}`, { method: 'DELETE' });
        showToast('用户删除成功', 'success');
        loadUsers();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// UAT状态切换
async function handleUatToggle() {
    const endpoint = state.currentVersion.status === 'UAT' ?
        `/versions/${state.currentVersion.id}/cancel-uat` :
        `/versions/${state.currentVersion.id}/start-uat`;

    try {
        await api(endpoint, { method: 'POST' });
        showToast(state.currentVersion.status === 'UAT' ? 'UAT已取消' : 'UAT已开始，现有步骤已锁定', 'success');
        await openVersion(state.currentVersion.id);
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 归档/恢复切换
async function handleArchiveToggle() {
    const endpoint = state.currentVersion.status === 'ARCHIVED' ?
        `/versions/${state.currentVersion.id}/unarchive` :
        `/versions/${state.currentVersion.id}/archive`;

    try {
        await api(endpoint, { method: 'POST' });
        showToast(state.currentVersion.status === 'ARCHIVED' ? '版本已恢复' : '版本已归档', 'success');

        // 返回列表
        document.getElementById('version-detail-view').classList.add('hidden');
        document.getElementById('versions-view').classList.remove('hidden');
        loadVersions();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 删除版本
async function handleDeleteVersion() {
    if (!state.currentVersion) return;

    const msg = state.currentVersion.status === 'ARCHIVED'
        ? '确定要删除该历史版本吗？\n删除后将无法恢复，且所有关联的步骤和附件都将永久删除。'
        : '确定要删除该版本吗？\n删除后将无法恢复，且所有关联的步骤和附件都将永久删除。';

    if (!confirm(msg)) return;

    try {
        await api(`/versions/${state.currentVersion.id}`, { method: 'DELETE' });
        showToast('版本已删除', 'success');

        // 返回列表
        document.getElementById('version-detail-view').classList.add('hidden');
        // 如果是在查看归档版本时删除，应该返回归档列表？
        // 但目前逻辑是统一返回主视图（开发中版本），或者根据 previous view？
        // 简单处理：如果当前是归档状态，刷新归档列表并显示归档视图
        // 但 openVersion 时 hides other views.

        // 简单起见，统一返回主页，用户如果要看历史需重新点击
        document.getElementById('versions-view').classList.remove('hidden');
        // 既然 loadVersions 是加载开发中版本，那么如果删除了归档版本，回到开发中列表也可以。
        // 为了体验更好，可以 check status

        if (state.currentVersion.status === 'ARCHIVED') {
            document.getElementById('versions-view').classList.add('hidden');
            document.getElementById('archived-view').classList.remove('hidden');
            loadArchivedVersions();
        } else {
            loadVersions();
        }

        // 清除保存的ID
        sessionStorage.removeItem('currentVersionId');

    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 添加步骤模态框
let quillEditor = null;
let pendingFiles = []; // 待上传的文件
let existingAttachments = []; // 编辑时的现有附件
let attachmentsToDelete = []; // 要删除的附件ID

function showAddStepModal() {
    pendingFiles = [];
    existingAttachments = [];
    attachmentsToDelete = [];

    showModal('添加更新步骤', `
        <div class="editor-container">
            <div id="step-editor"></div>
        </div>
        <div class="attachments-section" style="margin-top: 16px;">
            <div class="file-upload-area" onclick="document.getElementById('attachment-input').click()">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/>
                </svg>
                <p class="file-upload-text">点击上传附件</p>
            </div>
            <input type="file" id="attachment-input" style="display: none" multiple onchange="handleFileSelect(event)">
            <div id="pending-files-list" style="margin-top: 12px;"></div>
        </div>
    `, `
        <button class="btn btn-secondary" onclick="closeModal()">取消</button>
        <button class="btn btn-primary" onclick="saveStep()">保存</button>
    `, true);

    quillEditor = new Quill('#step-editor', {
        theme: 'snow',
        placeholder: '请输入更新步骤内容...',
        modules: {
            toolbar: [
                ['bold', 'italic', 'underline'],
                [{ 'list': 'ordered' }, { 'list': 'bullet' }],
                ['link', 'image'],
                ['clean']
            ]
        }
    });
}

// 处理文件选择 - 即时显示
function handleFileSelect(event) {
    const files = Array.from(event.target.files);
    pendingFiles = [...pendingFiles, ...files];
    renderPendingFiles();
}

// 渲染待上传文件列表
function renderPendingFiles() {
    const container = document.getElementById('pending-files-list');
    if (!container) return;

    if (pendingFiles.length === 0) {
        container.innerHTML = '';
        return;
    }

    container.innerHTML = `
        <div style="font-size: 0.875rem; color: var(--text-secondary); margin-bottom: 8px;">待上传附件：</div>
        <div class="attachment-list">
            ${pendingFiles.map((file, index) => `
                <div class="attachment-item" style="display: flex; align-items: center; gap: 8px;">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 16px; height: 16px;">
                        <path d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"/>
                    </svg>
                    <span style="flex: 1;">${file.name}</span>
                    <span style="color: var(--text-muted); font-size: 0.8rem;">${formatFileSize(file.size)}</span>
                    <button type="button" class="btn btn-sm btn-ghost" onclick="removePendingFile(${index})" style="padding: 4px 8px; color: var(--error);">
                        ✕
                    </button>
                </div>
            `).join('')}
        </div>
    `;
}

// 移除待上传文件
function removePendingFile(index) {
    pendingFiles.splice(index, 1);
    renderPendingFiles();
}

// 格式化文件大小
function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

async function saveStep() {
    const content = quillEditor.root.innerHTML;

    if (content === '<p><br></p>' || !content.trim()) {
        showToast('请输入步骤内容', 'warning');
        return;
    }

    try {
        const result = await api('/steps', {
            method: 'POST',
            body: JSON.stringify({
                versionId: state.currentVersion.id,
                content: content
            })
        });

        // 上传待上传的附件
        for (const file of pendingFiles) {
            const formData = new FormData();
            formData.append('file', file);

            await fetch(`${API_BASE}/steps/${result.data.id}/attachments`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${state.token}`
                },
                body: formData
            });
        }

        closeModal();
        showToast('步骤添加成功', 'success');
        await loadSteps();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 编辑步骤 - 支持附件管理
async function editStep(stepId) {
    const step = state.steps.find(s => s.id === stepId);
    if (!step) return;

    pendingFiles = [];
    existingAttachments = step.attachments ? [...step.attachments] : [];
    attachmentsToDelete = [];

    showModal('编辑更新步骤', `
        <div class="editor-container">
            <div id="step-editor"></div>
        </div>
        <div class="attachments-section" style="margin-top: 16px;">
            <div id="existing-attachments-list"></div>
            <div class="file-upload-area" onclick="document.getElementById('attachment-input').click()" style="margin-top: 12px;">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/>
                </svg>
                <p class="file-upload-text">点击添加更多附件</p>
            </div>
            <input type="file" id="attachment-input" style="display: none" multiple onchange="handleFileSelect(event)">
            <div id="pending-files-list" style="margin-top: 12px;"></div>
        </div>
    `, `
        <button class="btn btn-secondary" onclick="closeModal()">取消</button>
        <button class="btn btn-primary" onclick="updateStep(${stepId})">保存</button>
    `, true);

    quillEditor = new Quill('#step-editor', {
        theme: 'snow',
        modules: {
            toolbar: [
                ['bold', 'italic', 'underline'],
                [{ 'list': 'ordered' }, { 'list': 'bullet' }],
                ['link', 'image'],
                ['clean']
            ]
        }
    });

    quillEditor.root.innerHTML = step.content;

    // 渲染现有附件
    renderExistingAttachments();
}

// 渲染现有附件列表
function renderExistingAttachments() {
    const container = document.getElementById('existing-attachments-list');
    if (!container) return;

    const activeAttachments = existingAttachments.filter(att => !attachmentsToDelete.includes(att.id));

    if (activeAttachments.length === 0) {
        container.innerHTML = '';
        return;
    }

    container.innerHTML = `
        <div style="font-size: 0.875rem; color: var(--text-secondary); margin-bottom: 8px;">现有附件：</div>
        <div class="attachment-list">
            ${activeAttachments.map(att => `
                <div class="attachment-item" style="display: flex; align-items: center; gap: 8px;">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 16px; height: 16px;">
                        <path d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"/>
                    </svg>
                    <a href="${att.filepath}" target="_blank" style="flex: 1; color: var(--text-primary);">${att.filename}</a>
                    <button type="button" class="btn btn-sm btn-ghost" onclick="markAttachmentForDelete(${att.id})" style="padding: 4px 8px; color: var(--error);">
                        ✕
                    </button>
                </div>
            `).join('')}
        </div>
    `;
}

// 标记附件待删除
function markAttachmentForDelete(attachmentId) {
    attachmentsToDelete.push(attachmentId);
    renderExistingAttachments();
}

async function updateStep(stepId) {
    const content = quillEditor.root.innerHTML;

    try {
        // 更新步骤内容
        await api(`/steps/${stepId}`, {
            method: 'PUT',
            body: JSON.stringify({ content })
        });

        // 删除标记要删除的附件
        for (const attachmentId of attachmentsToDelete) {
            try {
                await fetch(`${API_BASE}/attachments/${attachmentId}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${state.token}`
                    }
                });
            } catch (e) {
                console.error('删除附件失败:', e);
            }
        }

        // 上传新附件
        for (const file of pendingFiles) {
            const formData = new FormData();
            formData.append('file', file);

            await fetch(`${API_BASE}/steps/${stepId}/attachments`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${state.token}`
                },
                body: formData
            });
        }

        closeModal();
        showToast('步骤更新成功', 'success');
        await loadSteps();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 删除步骤
async function deleteStep(stepId) {
    if (!confirm('确定要删除该步骤吗？')) return;

    try {
        await api(`/steps/${stepId}`, { method: 'DELETE' });
        showToast('步骤删除成功', 'success');
        await loadSteps();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 工具函数
function getStatusText(status) {
    const map = { DEV: '开发中', UAT: 'UAT中', ARCHIVED: '已归档' };
    return map[status] || status;
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN');
}

function formatDateTime(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleString('zh-CN');
}

// Toast通知
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 20px; height: 20px;">
            ${type === 'success' ? '<path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>' :
            type === 'error' ? '<path d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"/>' :
                '<path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>'}
        </svg>
        <span>${message}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideIn 0.3s ease reverse';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}
