import * as THREE from 'three';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';

const TOKEN_KEY = 'ld_token';

function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function setToken(t) {
  if (t) localStorage.setItem(TOKEN_KEY, t);
  else localStorage.removeItem(TOKEN_KEY);
}

async function api(path, options = {}) {
  const headers = { ...(options.headers || {}) };
  if (options.body && typeof options.body === 'string' && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }
  const tok = getToken();
  if (tok) headers['Authorization'] = `Bearer ${tok}`;
  const res = await fetch(path, { ...options, headers });
  if (res.status === 401) {
    setToken(null);
    showAuth();
    throw new Error('Сессия истекла');
  }
  if (!res.ok) {
    let msg = res.statusText;
    try {
      const pd = await res.json();
      if (pd.detail) msg = pd.detail;
    } catch { /* ignore */ }
    throw new Error(msg);
  }
  if (res.status === 204) return null;
  const ct = res.headers.get('content-type');
  if (ct && ct.includes('application/json')) return res.json();
  return res.text();
}

/* ——— Auth UI ——— */
const authPanel = document.getElementById('auth-panel');
const appPanel = document.getElementById('app-panel');

function showAuth() {
  authPanel.classList.add('visible');
  appPanel.classList.remove('visible');
}

function showApp() {
  authPanel.classList.remove('visible');
  appPanel.classList.add('visible');
}

document.getElementById('tab-login').onclick = () => {
  document.getElementById('tab-login').classList.add('active');
  document.getElementById('tab-reg').classList.remove('active');
  document.getElementById('form-login').style.display = 'block';
  document.getElementById('form-reg').style.display = 'none';
};
document.getElementById('tab-reg').onclick = () => {
  document.getElementById('tab-reg').classList.add('active');
  document.getElementById('tab-login').classList.remove('active');
  document.getElementById('form-reg').style.display = 'block';
  document.getElementById('form-login').style.display = 'none';
};

document.getElementById('btn-login').onclick = async () => {
  const el = document.getElementById('login-error');
  el.style.display = 'none';
  try {
    const data = await api('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        email: document.getElementById('login-email').value,
        password: document.getElementById('login-password').value,
      }),
    });
    setToken(data.token);
    await bootstrapApp(data.user);
  } catch (e) {
    el.textContent = e.message;
    el.style.display = 'block';
  }
};

document.getElementById('btn-reg').onclick = async () => {
  const el = document.getElementById('reg-error');
  el.style.display = 'none';
  try {
    const data = await api('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        email: document.getElementById('reg-email').value,
        password: document.getElementById('reg-password').value,
        displayName: document.getElementById('reg-name').value || null,
      }),
    });
    setToken(data.token);
    await bootstrapApp(data.user);
  } catch (e) {
    el.textContent = e.message;
    el.style.display = 'block';
  }
};

document.getElementById('btn-logout').onclick = () => {
  setToken(null);
  showAuth();
};

/* ——— Three.js ——— */
const canvas = document.getElementById('viewport');
const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true });
renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
renderer.shadowMap.enabled = true;

const scene = new THREE.Scene();
scene.background = new THREE.Color(0x0f1419);

const camera = new THREE.PerspectiveCamera(50, 1, 0.1, 500);
camera.position.set(12, 10, 14);

const controls = new OrbitControls(camera, canvas);
controls.enableDamping = true;
controls.dampingFactor = 0.06;
controls.maxPolarAngle = Math.PI / 2 - 0.05;
controls.target.set(0, 0, 0);

const hemi = new THREE.HemisphereLight(0x9ec8ff, 0x3d5c3d, 0.55);
scene.add(hemi);
const dir = new THREE.DirectionalLight(0xffffff, 0.85);
dir.position.set(10, 20, 8);
dir.castShadow = true;
dir.shadow.mapSize.set(2048, 2048);
dir.shadow.camera.near = 0.5;
dir.shadow.camera.far = 60;
dir.shadow.camera.left = -25;
dir.shadow.camera.right = 25;
dir.shadow.camera.top = 25;
dir.shadow.camera.bottom = -25;
scene.add(dir);

const groundGeo = new THREE.PlaneGeometry(80, 80);
const groundMat = new THREE.MeshStandardMaterial({
  color: 0x1e2a24,
  roughness: 0.92,
  metalness: 0.05,
});
const ground = new THREE.Mesh(groundGeo, groundMat);
ground.rotation.x = -Math.PI / 2;
ground.receiveShadow = true;
ground.name = 'ground';
scene.add(ground);

const grid = new THREE.GridHelper(80, 80, 0x2d4a3d, 0x1a2820);
grid.position.y = 0.01;
scene.add(grid);

const objectsGroup = new THREE.Group();
objectsGroup.name = 'objectsRoot';
scene.add(objectsGroup);

const raycaster = new THREE.Raycaster();
const pointer = new THREE.Vector2();

function resize() {
  const wrap = document.getElementById('viewport-wrap');
  const w = wrap.clientWidth;
  const h = wrap.clientHeight;
  camera.aspect = w / h;
  camera.updateProjectionMatrix();
  renderer.setSize(w, h, false);
}
window.addEventListener('resize', resize);

function animate() {
  requestAnimationFrame(animate);
  controls.update();
  renderer.render(scene, camera);
}

function createShapeMesh(shapeType, colorHex, sx, sy, sz) {
  const color = new THREE.Color(colorHex || '#888888');
  let geo;
  switch (shapeType) {
    case 'box':
      geo = new THREE.BoxGeometry(1, 1, 1);
      break;
    case 'sphere':
      geo = new THREE.SphereGeometry(0.5, 28, 18);
      break;
    case 'cylinder':
      geo = new THREE.CylinderGeometry(0.42, 0.42, 1, 20);
      break;
    case 'cone':
      geo = new THREE.ConeGeometry(0.48, 1, 20);
      break;
    case 'plane':
      geo = new THREE.PlaneGeometry(1, 1);
      break;
    default:
      geo = new THREE.BoxGeometry(1, 1, 1);
  }
  const mat = new THREE.MeshStandardMaterial({ color, roughness: 0.75, metalness: 0.1 });
  const mesh = new THREE.Mesh(geo, mat);
  mesh.castShadow = true;
  mesh.receiveShadow = true;
  mesh.scale.set(sx, sy, sz);
  if (shapeType === 'plane') {
    mesh.rotation.x = -Math.PI / 2;
  }
  return mesh;
}

/** Смещение по Y, чтобы «низ» объекта стоял на земле */
function groundOffsetY(shapeType, sy) {
  switch (shapeType) {
    case 'box':
      return sy * 0.5;
    case 'sphere':
      return sy * 0.25;
    case 'cylinder':
      return sy * 0.5;
    case 'cone':
      return sy * 0.5;
    case 'plane':
      return 0.02;
    default:
      return sy * 0.5;
  }
}

function addPlacedObject(data) {
  const {
    id,
    catalogDetailId,
    userLibraryItemId,
    shapeType,
    position,
    rotation,
    scale,
    color,
  } = data;
  const sx = scale[0],
    sy = scale[1],
    sz = scale[2];
  const mesh = createShapeMesh(shapeType, color, sx, sy, sz);
  const group = new THREE.Group();
  mesh.position.y = groundOffsetY(shapeType, sy);
  group.add(mesh);
  group.position.set(position[0], position[1], position[2]);
  group.rotation.set(rotation[0], rotation[1], rotation[2]);
  group.userData = {
    instanceId: id,
    catalogDetailId: catalogDetailId ?? null,
    userLibraryItemId: userLibraryItemId ?? null,
    shapeType,
    colorHex: color,
  };
  objectsGroup.add(group);
  return group;
}

function serializeScene() {
  const objects = [];
  objectsGroup.children.forEach((group) => {
    const u = group.userData;
    if (!u.instanceId) return;
    const mesh = group.children[0];
    const col = mesh?.material?.color;
    const row = {
      id: u.instanceId,
      shapeType: u.shapeType,
      position: group.position.toArray(),
      rotation: [group.rotation.x, group.rotation.y, group.rotation.z],
      scale: mesh ? [mesh.scale.x, mesh.scale.y, mesh.scale.z] : [1, 1, 1],
      color: col ? '#' + col.getHexString() : u.colorHex || '#888888',
    };
    if (u.catalogDetailId != null) row.catalogDetailId = u.catalogDetailId;
    if (u.userLibraryItemId != null) row.userLibraryItemId = u.userLibraryItemId;
    objects.push(row);
  });
  return JSON.stringify({ version: 1, objects });
}

function loadSceneFromJson(jsonStr) {
  while (objectsGroup.children.length) {
    const o = objectsGroup.children[0];
    objectsGroup.remove(o);
    if (o.children) o.children.forEach((c) => { if (c.geometry) c.geometry.dispose(); if (c.material) c.material.dispose(); });
  }
  let data;
  try {
    data = JSON.parse(jsonStr || '{"version":1,"objects":[]}');
  } catch {
    data = { version: 1, objects: [] };
  }
  const objs = data.objects || [];
  objs.forEach((o) => {
    addPlacedObject({
      id: o.id || crypto.randomUUID(),
      catalogDetailId: o.catalogDetailId ?? null,
      userLibraryItemId: o.userLibraryItemId ?? null,
      shapeType: o.shapeType || 'box',
      position: o.position || [0, 0, 0],
      rotation: o.rotation || [0, 0, 0],
      scale: o.scale || [1, 1, 1],
      color: o.color || '#888888',
    });
  });
}

/* ——— Selection & placement ——— */
let selectedCatalog = null;
let selectedGroup = null;

const selectionPanel = document.getElementById('selection-panel');
const btnDel = document.getElementById('btn-delete-selected');
const transformRow = document.getElementById('transform-row');

const ROT_STEP = Math.PI / 12; // 15°
const MOVE_STEP = 0.25;
const MOVE_STEP_Y = 0.2; // высота над плоскостью земли (ось Y в Three.js)

function rotationYDegrees(group) {
  let deg = Math.round((group.rotation.y * 180) / Math.PI);
  return ((deg % 360) + 360) % 360;
}

function rotationZDegrees(group) {
  let deg = Math.round((group.rotation.z * 180) / Math.PI);
  return ((deg % 360) + 360) % 360;
}

function rotateSelectedY(delta) {
  if (!selectedGroup) return;
  selectedGroup.rotation.y += delta;
}

function rotateSelectedZ(delta) {
  if (!selectedGroup) return;
  selectedGroup.rotation.z += delta;
}

function moveSelectedOnGround(dx, dz) {
  if (!selectedGroup) return;
  selectedGroup.position.x += dx;
  selectedGroup.position.z += dz;
}

function moveSelectedVertical(dy) {
  if (!selectedGroup) return;
  selectedGroup.position.y += dy;
}

function snapSelectedToGroundLevel() {
  if (!selectedGroup) return;
  selectedGroup.position.y = 0;
}

function refreshSelectionLabel() {
  if (!selectedGroup || !selectedGroup.children[0]) return;
  const g = selectedGroup;
  const x = g.position.x.toFixed(2);
  const y = g.position.y.toFixed(2);
  const z = g.position.z.toFixed(2);
  selectionPanel.textContent =
    `ID: ${g.userData.instanceId.slice(0, 8)}… · ${g.userData.shapeType} · позиция X ${x} Y ${y} Z ${z} · поворот ∠Y ${rotationYDegrees(g)}° ∠Z ${rotationZDegrees(g)}°`;
}

function setSelectedGroup(g) {
  if (selectedGroup && selectedGroup.children[0]) {
    selectedGroup.children[0].material.emissive.setHex(0x000000);
  }
  selectedGroup = g;
  if (g && g.children[0]) {
    g.children[0].material.emissive.setHex(0x224422);
    refreshSelectionLabel();
    btnDel.style.display = 'block';
    transformRow.style.display = 'block';
  } else {
    selectionPanel.textContent = 'Нет выбора';
    btnDel.style.display = 'none';
    transformRow.style.display = 'none';
  }
}

document.getElementById('btn-x-min').onclick = () => {
  moveSelectedOnGround(-MOVE_STEP, 0);
  refreshSelectionLabel();
};
document.getElementById('btn-x-plus').onclick = () => {
  moveSelectedOnGround(MOVE_STEP, 0);
  refreshSelectionLabel();
};
document.getElementById('btn-depth-min').onclick = () => {
  moveSelectedOnGround(0, -MOVE_STEP);
  refreshSelectionLabel();
};
document.getElementById('btn-depth-plus').onclick = () => {
  moveSelectedOnGround(0, MOVE_STEP);
  refreshSelectionLabel();
};
document.getElementById('btn-y-min').onclick = () => {
  moveSelectedVertical(-MOVE_STEP_Y);
  refreshSelectionLabel();
};
document.getElementById('btn-y-plus').onclick = () => {
  moveSelectedVertical(MOVE_STEP_Y);
  refreshSelectionLabel();
};
document.getElementById('btn-y-ground').onclick = () => {
  snapSelectedToGroundLevel();
  refreshSelectionLabel();
};

document.getElementById('btn-rot-ccw').onclick = () => {
  rotateSelectedY(-ROT_STEP);
  refreshSelectionLabel();
};
document.getElementById('btn-rot-cw').onclick = () => {
  rotateSelectedY(ROT_STEP);
  refreshSelectionLabel();
};
document.getElementById('btn-rotz-ccw').onclick = () => {
  rotateSelectedZ(-ROT_STEP);
  refreshSelectionLabel();
};
document.getElementById('btn-rotz-cw').onclick = () => {
  rotateSelectedZ(ROT_STEP);
  refreshSelectionLabel();
};

document.addEventListener('keydown', (e) => {
  if (e.target.closest('input, textarea, select')) return;
  if (!selectedGroup || !appPanel.classList.contains('visible')) return;

  if (e.key === 'ArrowLeft') {
    e.preventDefault();
    moveSelectedOnGround(-MOVE_STEP, 0);
    refreshSelectionLabel();
    return;
  }
  if (e.key === 'ArrowRight') {
    e.preventDefault();
    moveSelectedOnGround(MOVE_STEP, 0);
    refreshSelectionLabel();
    return;
  }
  if (e.key === 'ArrowUp') {
    e.preventDefault();
    moveSelectedVertical(MOVE_STEP_Y);
    refreshSelectionLabel();
    return;
  }
  if (e.key === 'ArrowDown') {
    e.preventDefault();
    moveSelectedVertical(-MOVE_STEP_Y);
    refreshSelectionLabel();
    return;
  }
  if (e.key === 'w' || e.key === 'W') {
    e.preventDefault();
    moveSelectedOnGround(0, -MOVE_STEP);
    refreshSelectionLabel();
    return;
  }
  if (e.key === 's' || e.key === 'S') {
    e.preventDefault();
    moveSelectedOnGround(0, MOVE_STEP);
    refreshSelectionLabel();
    return;
  }

  if (e.key === '[' || e.key === ']') {
    e.preventDefault();
    const d = e.key === '[' ? -ROT_STEP : ROT_STEP;
    if (e.altKey) {
      rotateSelectedZ(d);
    } else {
      rotateSelectedY(d);
    }
    refreshSelectionLabel();
  }
});

btnDel.onclick = () => {
  if (selectedGroup) {
    objectsGroup.remove(selectedGroup);
    if (selectedGroup.children[0]) {
      selectedGroup.children[0].geometry.dispose();
      selectedGroup.children[0].material.dispose();
    }
    setSelectedGroup(null);
  }
};

function setPointerFromEvent(e) {
  const rect = canvas.getBoundingClientRect();
  pointer.x = ((e.clientX - rect.left) / rect.width) * 2 - 1;
  pointer.y = -((e.clientY - rect.top) / rect.height) * 2 + 1;
}

canvas.addEventListener('pointerdown', (e) => {
  if (e.button !== 0) return;
  setPointerFromEvent(e);
  raycaster.setFromCamera(pointer, camera);
  const objMeshes = [];
  objectsGroup.traverse((ch) => {
    if (ch.isMesh) objMeshes.push(ch);
  });
  const hitsObj = raycaster.intersectObjects(objMeshes, false);
  if (hitsObj.length) {
    let g = hitsObj[0].object.parent;
    while (g && g.parent !== objectsGroup) g = g.parent;
    if (g && g.parent === objectsGroup) {
      setSelectedGroup(g);
      e.stopPropagation();
    }
    return;
  }
  const hitsGround = raycaster.intersectObject(ground, false);
  if (hitsGround.length && selectedCatalog) {
    const p = hitsGround[0].point;
    const c = selectedCatalog;
    addPlacedObject({
      id: crypto.randomUUID(),
      catalogDetailId: c.kind === 'system' ? c.catalogDetailId : null,
      userLibraryItemId: c.kind === 'user' ? c.userLibraryItemId : null,
      shapeType: c.shapeType,
      position: [p.x, 0, p.z],
      rotation: [0, 0, 0],
      scale: [c.defaultScaleX, c.defaultScaleY, c.defaultScaleZ],
      color: c.colorHex || '#66aa66',
    });
    setSelectedGroup(null);
  }
});

document.getElementById('btn-reset-cam').onclick = () => {
  camera.position.set(12, 10, 14);
  controls.target.set(0, 0, 0);
};

/* ——— App state ——— */
let systemCatalog = [];
let myLibraryItems = [];
let browseItems = [];
let projects = [];
let currentProjectId = null;

function toSystemEntry(c) {
  return {
    kind: 'system',
    catalogDetailId: c.id,
    name: c.name,
    category: c.category,
    shapeType: c.shapeType,
    colorHex: c.colorHex,
    defaultScaleX: c.defaultScaleX,
    defaultScaleY: c.defaultScaleY,
    defaultScaleZ: c.defaultScaleZ,
  };
}

function toUserEntry(item) {
  return {
    kind: 'user',
    userLibraryItemId: item.id,
    name: item.name,
    category: item.category,
    shapeType: item.shapeType,
    colorHex: item.colorHex,
    defaultScaleX: item.defaultScaleX,
    defaultScaleY: item.defaultScaleY,
    defaultScaleZ: item.defaultScaleZ,
  };
}

function highlightCatalogSelection(el) {
  document.querySelectorAll('.cat-row, .lib-row').forEach((e) => e.classList.remove('selected-cat'));
  if (el) el.classList.add('selected-cat');
}

function selectCatalogEntry(entry, el) {
  selectedCatalog = entry;
  highlightCatalogSelection(el);
}

async function refreshUserLibraries() {
  myLibraryItems = await api('/api/my-library/items');
  browseItems = await api('/api/library/browse');
  renderCatalogSections();
}

function renderCatalogSections() {
  const list = document.getElementById('catalog-list');
  list.innerHTML = '';
  systemCatalog.forEach((c) => {
    const div = document.createElement('div');
    div.className = 'cat-row';
    div.innerHTML = `<strong>${escapeHtml(c.name)}</strong><small>${escapeHtml(c.category)} · ${escapeHtml(c.shapeType)}</small>`;
    div.onclick = () => selectCatalogEntry(toSystemEntry(c), div);
    list.appendChild(div);
  });

  const myList = document.getElementById('my-library-list');
  myList.innerHTML = '';
  if (!myLibraryItems.length) {
    myList.innerHTML = '<p style="font-size:0.75rem;color:var(--muted)">Пока пусто</p>';
  }
  myLibraryItems.forEach((item) => {
    const wrap = document.createElement('div');
    wrap.className = 'lib-row';
    const main = document.createElement('div');
    main.className = 'lib-row-main';
    main.innerHTML = `<strong>${escapeHtml(item.name)}</strong><small>${escapeHtml(item.category)} · ${escapeHtml(item.shapeType)}${item.sourceItemId != null ? ' · импорт' : ''}</small>`;
    main.onclick = () => selectCatalogEntry(toUserEntry(item), wrap);
    const tools = document.createElement('div');
    tools.className = 'lib-row-tools';
    const lab = document.createElement('label');
    lab.style.cssText = 'display:flex;align-items:center;gap:0.35rem;cursor:pointer;margin:0;flex:1;';
    const cb = document.createElement('input');
    cb.type = 'checkbox';
    cb.checked = item.publicForImport;
    cb.onclick = (ev) => ev.stopPropagation();
    cb.onchange = async () => {
      try {
        await api(`/api/my-library/items/${item.id}`, {
          method: 'PATCH',
          body: JSON.stringify({ publicForImport: cb.checked }),
        });
        await refreshUserLibraries();
      } catch (e) {
        alert(e.message);
        cb.checked = item.publicForImport;
      }
    };
    lab.appendChild(cb);
    lab.appendChild(document.createTextNode(' импорт другим'));
    const del = document.createElement('button');
    del.type = 'button';
    del.textContent = 'Удалить';
    del.onclick = async (ev) => {
      ev.stopPropagation();
      if (!confirm('Удалить объект из вашей библиотеки?')) return;
      try {
        await api(`/api/my-library/items/${item.id}`, { method: 'DELETE' });
        if (selectedCatalog?.kind === 'user' && selectedCatalog.userLibraryItemId === item.id) {
          selectedCatalog = null;
          highlightCatalogSelection(null);
        }
        await refreshUserLibraries();
      } catch (e) {
        alert(e.message);
      }
    };
    tools.appendChild(lab);
    tools.appendChild(del);
    wrap.appendChild(main);
    wrap.appendChild(tools);
    myList.appendChild(wrap);
  });

  const br = document.getElementById('browse-library-list');
  br.innerHTML = '';
  if (!browseItems.length) {
    br.innerHTML = '<p style="font-size:0.75rem;color:var(--muted)">Нет опубликованных объектов</p>';
  }
  browseItems.forEach((item) => {
    const wrap = document.createElement('div');
    wrap.className = 'lib-row';
    const main = document.createElement('div');
    main.className = 'lib-row-main';
    main.innerHTML = `<strong>${escapeHtml(item.name)}</strong><small>от ${escapeHtml(item.ownerLabel)} · ${escapeHtml(item.shapeType)}</small>`;
    const tools = document.createElement('div');
    tools.className = 'lib-row-tools';
    const imp = document.createElement('button');
    imp.type = 'button';
    imp.textContent = 'Импорт';
    imp.style.marginLeft = '0';
    imp.onclick = async () => {
      try {
        await api(`/api/my-library/items/import/${item.id}`, { method: 'POST' });
        await refreshUserLibraries();
        alert('Скопировано в «Моя библиотека»');
      } catch (e) {
        alert(e.message);
      }
    };
    tools.appendChild(imp);
    wrap.appendChild(main);
    wrap.appendChild(tools);
    br.appendChild(wrap);
  });
}

document.getElementById('btn-lib-add').onclick = async () => {
  const name = document.getElementById('lib-name').value.trim();
  if (!name) {
    alert('Укажите название');
    return;
  }
  const category = document.getElementById('lib-category').value.trim() || 'Своё';
  const desc = document.getElementById('lib-desc').value.trim();
  const colorRaw = document.getElementById('lib-color').value.trim();
  const sx = parseFloat(document.getElementById('lib-sx').value);
  const sy = parseFloat(document.getElementById('lib-sy').value);
  const sz = parseFloat(document.getElementById('lib-sz').value);
  try {
    await api('/api/my-library/items', {
      method: 'POST',
      body: JSON.stringify({
        name,
        category,
        description: desc || null,
        shapeType: document.getElementById('lib-shape').value,
        colorHex: colorRaw || null,
        defaultScaleX: Number.isFinite(sx) ? sx : 1,
        defaultScaleY: Number.isFinite(sy) ? sy : 1,
        defaultScaleZ: Number.isFinite(sz) ? sz : 1,
        publicForImport: document.getElementById('lib-public').checked,
      }),
    });
    document.getElementById('lib-name').value = '';
    document.getElementById('lib-desc').value = '';
    document.getElementById('lib-public').checked = false;
    await refreshUserLibraries();
  } catch (e) {
    alert(e.message);
  }
};

async function refreshMe() {
  const me = await api('/api/me');
  document.getElementById('user-email').textContent = me.email;
  document.getElementById('tier-label').textContent = me.subscriptionTier;
}

document.querySelectorAll('[data-tier]').forEach((btn) => {
  btn.onclick = async () => {
    try {
      await api('/api/me/subscription', {
        method: 'POST',
        body: JSON.stringify({ tier: btn.dataset.tier, periodEndIso: null }),
      });
      await refreshMe();
    } catch (e) {
      alert(e.message);
    }
  };
});

async function refreshProjects() {
  projects = await api('/api/projects');
  const list = document.getElementById('project-list');
  list.innerHTML = '';
  projects.forEach((p) => {
    const div = document.createElement('div');
    div.className = 'project-row' + (p.id === currentProjectId ? ' active' : '');
    div.textContent = p.name;
    div.onclick = () => openProject(p.id);
    list.appendChild(div);
  });
}

async function openProject(id) {
  currentProjectId = id;
  const p = await api(`/api/projects/${id}`);
  loadSceneFromJson(p.sceneState);
  await refreshProjects();
}

document.getElementById('btn-new-project').onclick = async () => {
  const name = prompt('Название проекта', 'Участок');
  if (!name) return;
  try {
    const p = await api('/api/projects', {
      method: 'POST',
      body: JSON.stringify({ name, description: '' }),
    });
    currentProjectId = p.id;
    loadSceneFromJson(p.sceneState);
    await refreshProjects();
  } catch (e) {
    alert(e.message);
  }
};

document.getElementById('btn-save').onclick = async () => {
  if (!currentProjectId) {
    alert('Выберите или создайте проект');
    return;
  }
  try {
    await api(`/api/projects/${currentProjectId}/scene`, {
      method: 'PUT',
      body: JSON.stringify({ sceneState: serializeScene() }),
    });
    await refreshProjects();
    alert('Сохранено');
  } catch (e) {
    alert(e.message);
  }
};

function escapeHtml(s) {
  const d = document.createElement('div');
  d.textContent = s;
  return d.innerHTML;
}

async function bootstrapApp(user) {
  showApp();
  document.getElementById('user-email').textContent = user.email;
  document.getElementById('tier-label').textContent = user.subscriptionTier;
  systemCatalog = await fetch('/api/catalog/details').then((r) => r.json());
  await refreshUserLibraries();
  await refreshProjects();
  if (projects.length) await openProject(projects[0].id);
  else loadSceneFromJson('{"version":1,"objects":[]}');
  resize();
  animate();
}

if (getToken()) {
  api('/api/me')
    .then((me) => bootstrapApp(me))
    .catch(() => showAuth());
} else {
  showAuth();
}
