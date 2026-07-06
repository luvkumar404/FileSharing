const API_BASE_URL = "";
const TOKEN_KEY = "fileSharingToken";
const USER_KEY = "fileSharingUser";

const authSection = document.getElementById("authSection");
const fileSection = document.getElementById("fileSection");
const registerForm = document.getElementById("registerForm");
const loginForm = document.getElementById("loginForm");
const uploadForm = document.getElementById("uploadForm");
const fileInput = document.getElementById("fileInput");
const filesList = document.getElementById("filesList");
const logoutBtn = document.getElementById("logoutBtn");
const messageBox = document.getElementById("messageBox");
const userLabel = document.getElementById("userLabel");

document.addEventListener("DOMContentLoaded", initializePage);
registerForm.addEventListener("submit", handleRegister);
loginForm.addEventListener("submit", handleLogin);
uploadForm.addEventListener("submit", handleUpload);
logoutBtn.addEventListener("click", logout);

function initializePage() {
    if (getToken()) {
        showFileSection();
        loadFiles();
    } else {
        showAuthSection();
    }
}

async function handleRegister(event) {
    event.preventDefault();

    const requestBody = {
        name: document.getElementById("registerName").value.trim(),
        email: document.getElementById("registerEmail").value.trim(),
        password: document.getElementById("registerPassword").value
    };

    try {
        const data = await request("/api/auth/register", {
            method: "POST",
            body: JSON.stringify(requestBody)
        });

        saveSession(data);
        registerForm.reset();
        showMessage("Account created successfully.", "success");
        showFileSection();
        loadFiles();
    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function handleLogin(event) {
    event.preventDefault();

    const requestBody = {
        email: document.getElementById("loginEmail").value.trim(),
        password: document.getElementById("loginPassword").value
    };

    try {
        const data = await request("/api/auth/login", {
            method: "POST",
            body: JSON.stringify(requestBody)
        });

        saveSession(data);
        loginForm.reset();
        showMessage("Logged in successfully.", "success");
        showFileSection();
        loadFiles();
    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function handleUpload(event) {
    event.preventDefault();

    if (!fileInput.files.length) {
        showMessage("Please select a file to upload.", "error");
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);

    try {
        await request("/api/files/upload", {
            method: "POST",
            body: formData,
            auth: true
        });

        uploadForm.reset();
        showMessage("File uploaded successfully.", "success");
        loadFiles();
    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function loadFiles() {
    try {
        const files = await request("/api/files", {
            method: "GET",
            auth: true
        });
        renderFiles(files);
    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function deleteFile(fileId) {
    const confirmed = window.confirm("Delete this file?");
    if (!confirmed) {
        return;
    }

    try {
        await request(`/api/files/${fileId}`, {
            method: "DELETE",
            auth: true
        });

        showMessage("File deleted successfully.", "success");
        loadFiles();
    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function shareFile(fileId) {
    const minutes = window.prompt("Link expiry time in minutes", "60");
    if (!minutes) {
        return;
    }

    const expiresInMinutes = Number(minutes);
    if (!Number.isInteger(expiresInMinutes) || expiresInMinutes < 1) {
        showMessage("Expiry time must be a positive number of minutes.", "error");
        return;
    }

    try {
        const data = await request(`/api/files/${fileId}/share`, {
            method: "POST",
            auth: true,
            body: JSON.stringify({ expiresInMinutes })
        });

        await copyToClipboard(data.publicUrl);
        showMessage(`Share link copied: ${data.publicUrl}`, "success");
    } catch (error) {
        showMessage(error.message, "error");
    }
}

function renderFiles(files) {
    filesList.innerHTML = "";

    if (!files.length) {
        filesList.innerHTML = '<div class="empty-state">No files uploaded yet.</div>';
        return;
    }

    files.forEach((file) => {
        const card = document.createElement("article");
        card.className = "file-card";

        const info = document.createElement("div");
        const title = document.createElement("p");
        title.className = "file-name";
        title.textContent = file.originalFileName;

        const meta = document.createElement("p");
        meta.className = "file-meta";
        meta.textContent = `${formatBytes(file.fileSize)} · ${file.fileType || "Unknown type"} · ${formatDate(file.createdAt)}`;

        info.appendChild(title);
        info.appendChild(meta);

        const actions = document.createElement("div");
        actions.className = "file-actions";

        const openLink = document.createElement("a");
        openLink.href = file.cloudinarySecureUrl;
        openLink.target = "_blank";
        openLink.rel = "noopener noreferrer";
        openLink.textContent = "Open";

        const shareButton = document.createElement("button");
        shareButton.type = "button";
        shareButton.textContent = "Share";
        shareButton.addEventListener("click", () => shareFile(file.id));

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "danger-btn";
        deleteButton.textContent = "Delete";
        deleteButton.addEventListener("click", () => deleteFile(file.id));

        actions.appendChild(openLink);
        actions.appendChild(shareButton);
        actions.appendChild(deleteButton);

        card.appendChild(info);
        card.appendChild(actions);
        filesList.appendChild(card);
    });
}

async function request(path, options = {}) {
    const headers = {};

    if (!(options.body instanceof FormData)) {
        headers["Content-Type"] = "application/json";
    }

    if (options.auth) {
        headers.Authorization = `Bearer ${getToken()}`;
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers: {
            ...headers,
            ...options.headers
        }
    });

    if (response.status === 401 || response.status === 403) {
        logout();
        throw new Error("Your session has expired. Please login again.");
    }

    if (!response.ok) {
        throw new Error(await readErrorMessage(response));
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

async function readErrorMessage(response) {
    try {
        const data = await response.json();
        if (data.errors) {
            return Object.values(data.errors).join(" ");
        }
        return data.message || "Request failed.";
    } catch (error) {
        return "Request failed.";
    }
}

function saveSession(data) {
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, JSON.stringify({
        name: data.name,
        email: data.email
    }));
}

function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    filesList.innerHTML = "";
    showAuthSection();
    showMessage("Logged out.", "success");
}

function showAuthSection() {
    authSection.classList.remove("hidden");
    fileSection.classList.add("hidden");
    logoutBtn.classList.add("hidden");
}

function showFileSection() {
    authSection.classList.add("hidden");
    fileSection.classList.remove("hidden");
    logoutBtn.classList.remove("hidden");

    const user = getUser();
    userLabel.textContent = user ? `Logged in as ${user.name} (${user.email})` : "";
}

function showMessage(message, type) {
    messageBox.textContent = message;
    messageBox.className = `message ${type}`;
    messageBox.classList.remove("hidden");
}

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function getUser() {
    const userJson = localStorage.getItem(USER_KEY);
    return userJson ? JSON.parse(userJson) : null;
}

async function copyToClipboard(text) {
    if (navigator.clipboard) {
        await navigator.clipboard.writeText(text);
    }
}

function formatBytes(bytes) {
    if (!bytes) {
        return "0 B";
    }

    const units = ["B", "KB", "MB", "GB"];
    let size = bytes;
    let unitIndex = 0;

    while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024;
        unitIndex++;
    }

    return `${size.toFixed(size >= 10 || unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
}

function formatDate(value) {
    if (!value) {
        return "Unknown date";
    }
    return new Date(value).toLocaleString();
}
