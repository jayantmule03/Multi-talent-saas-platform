requireAuth();

const user = Storage.getUser();
document.getElementById('userName').textContent = user?.fullName || 'User';
document.getElementById('userEmail').textContent = user?.email || '';

document.getElementById('logoutLink').addEventListener('click', (e) => {
  e.preventDefault();
  logout();
});

const projectGrid = document.getElementById('projectGrid');
const emptyState = document.getElementById('emptyState');
const projectModal = document.getElementById('projectModal');
const projectForm = document.getElementById('projectForm');
const modalAlert = document.getElementById('modalAlert');
const modalTitle = document.getElementById('modalTitle');

function openModal(project = null) {
  modalAlert.innerHTML = '';
  document.getElementById('projectId').value = project?.id || '';
  document.getElementById('projectName').value = project?.name || '';
  document.getElementById('projectDescription').value = project?.description || '';
  modalTitle.textContent = project ? 'Edit Project' : 'New Project';
  projectModal.classList.add('open');
}

function closeModal() {
  projectModal.classList.remove('open');
  projectForm.reset();
}

document.getElementById('newProjectBtn').addEventListener('click', () => openModal());
document.getElementById('cancelModalBtn').addEventListener('click', closeModal);

function statusBadgeLabel(status) {
  return status.replace('_', ' ');
}

function renderProjects(projects) {
  projectGrid.innerHTML = '';
  emptyState.style.display = projects.length === 0 ? 'block' : 'none';

  document.getElementById('statTotal').textContent = projects.length;
  document.getElementById('statInProgress').textContent =
    projects.filter(p => p.status === 'IN_PROGRESS').length;
  document.getElementById('statCompleted').textContent =
    projects.filter(p => p.status === 'COMPLETED').length;

  projects.forEach(project => {
    const card = document.createElement('div');
    card.className = 'project-card';
    card.innerHTML = `
      <span class="badge">${statusBadgeLabel(project.status)}</span>
      <h3 style="margin-top:8px;">${escapeHtml(project.name)}</h3>
      <p>${escapeHtml(project.description || 'No description provided')}</p>
      <div class="project-actions">
        <button data-action="edit" data-id="${project.id}">Edit</button>
        <button data-action="delete" data-id="${project.id}" class="danger">Delete</button>
      </div>
    `;
    projectGrid.appendChild(card);
  });

  projectGrid.querySelectorAll('button[data-action="edit"]').forEach(btn => {
    btn.addEventListener('click', () => {
      const project = projects.find(p => p.id === btn.dataset.id);
      openModal(project);
    });
  });

  projectGrid.querySelectorAll('button[data-action="delete"]').forEach(btn => {
    btn.addEventListener('click', async () => {
      if (!confirm('Delete this project? This cannot be undone.')) return;
      try {
        await Api.deleteProject(btn.dataset.id);
        loadProjects();
      } catch (err) {
        alert(err.message);
      }
    });
  });
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

async function loadProjects() {
  try {
    const projects = await Api.getProjects();
    renderProjects(projects);
  } catch (err) {
    if (err.message.includes('401') || err.message.toLowerCase().includes('unauthorized')) {
      logout();
    }
  }
}

projectForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const id = document.getElementById('projectId').value;
  const payload = {
    name: document.getElementById('projectName').value,
    description: document.getElementById('projectDescription').value
  };

  const saveBtn = document.getElementById('saveProjectBtn');
  saveBtn.disabled = true;

  try {
    if (id) {
      await Api.updateProject(id, payload);
    } else {
      await Api.createProject(payload);
    }
    closeModal();
    loadProjects();
  } catch (err) {
    modalAlert.innerHTML = `<div class="alert alert-error">${err.message}</div>`;
  } finally {
    saveBtn.disabled = false;
  }
});

loadProjects();
