const API = '/api';

function j(o) {
    return JSON.stringify(o);
}

async function fetchJson(url, opts = {}) {
    const o = Object.assign({headers: {'Content-Type': 'application/json'}}, opts);
    const r = await fetch(url, o);
    const ct = r.headers.get('content-type') || '';
    const d = ct.includes('application/json') ? await r.json() : await r.text();
    if (!r.ok) throw new Error(typeof d === 'string' ? d : (d && d.error) || 'Request error');
    return d;
}

async function fetchPlain(url, opts = {}) {
    const r = await fetch(url, opts);
    if (!r.ok) throw new Error('Request error');
    return true;
}

const api = {
    bands: (q) => fetchJson(`${API}/bands${q}`),
    band: (id) => fetchJson(`${API}/bands/${id}`),
    createBand: (dto) => fetchJson(`${API}/bands`, {method: 'POST', body: j(dto)}),
    updateBand: (id, dto) => fetchJson(`${API}/bands/${id}`, {method: 'PUT', body: j(dto)}),
    deleteBand: (id) => fetchPlain(`${API}/bands/${id}`, {method: 'DELETE'}),

    persons: () => fetchJson(`${API}/persons`),
    createPerson: (dto) => fetchJson(`${API}/persons`, {method: 'POST', body: j(dto)}),
    deletePerson: (id) => fetchPlain(`${API}/persons/${id}`, {method: 'DELETE'}),

    albums: () => fetchJson(`${API}/albums`),
    createAlbum: (dto) => fetchJson(`${API}/albums`, {method: 'POST', body: j(dto)}),
    deleteAlbum: (id) => fetchPlain(`${API}/albums/${id}`, {method: 'DELETE'}),

    descStarts: (p) => fetchJson(`${API}/bands/search/descriptionStarts?prefix=${encodeURIComponent(p)}`),
    byGenre: (g) => fetchJson(`${API}/bands/search/byGenre?genre=${encodeURIComponent(g)}`),
    deleteByAlbumsCount: (v) =>
        fetchJson(`${API}/bands/ops/deleteByAlbumsCount?value=${encodeURIComponent(v)}`, {method: 'POST'}),
    decrement: (id, by) =>
        fetchJson(`${API}/bands/${id}/participants/decrement?by=${encodeURIComponent(by)}`, {method: 'POST'}),

    importFile: async (file) => {
        const formData = new FormData();
        formData.append('file', file);
        const r = await fetch(`${API}/import`, {method: 'POST', body: formData});
        const ct = r.headers.get('content-type') || '';
        const d = ct.includes('application/json') ? await r.json() : await r.text();
        if (!r.ok) throw new Error(typeof d === 'string' ? d : (d && d.error) || 'Import error');
        return d;
    },
    importHistory: (all) => fetchJson(`${API}/import/history?all=${all}`)
};

const state = {
    filters: {name: '', desc: '', album: '', front: '', genre: ''},
    sort: {prop: 'id', dir: 'asc'},
    page: 0,
    size: 10,
    totalPages: 1,
    totalElements: 0,
    content: [],
    albums: [],
    persons: [],
    dataLoaded: false,
    modalOpenSeq: 0,
    saving: {band: false, person: false, album: false},
    ws: {client: null, muteNext: false, skipFirst: true}
};

const els = {
    fName: document.getElementById('f-name'),
    fDesc: document.getElementById('f-desc'),
    fAlbum: document.getElementById('f-album'),
    fFront: document.getElementById('f-front'),
    fGenre: document.getElementById('f-genre'),

    btnApply: document.getElementById('btn-apply'),
    btnReset: document.getElementById('btn-reset'),

    sortProp: document.getElementById('sort-prop'),
    sortDir: document.getElementById('sort-dir'),
    pageSize: document.getElementById('page-size'),

    btnCreate: document.getElementById('btn-create'),
    btnImport: document.getElementById('btn-import'),
    btnImportHistory: document.getElementById('btn-import-history'),
    btnPersons: document.getElementById('btn-persons'),
    btnAlbums: document.getElementById('btn-albums'),

    btnOps: document.getElementById('btn-ops'),
    opsMenu: document.getElementById('ops-menu'),

    importModal: document.getElementById('import-modal'),
    importForm: document.getElementById('import-form'),
    importFile: document.getElementById('import-file'),
    importResult: document.getElementById('import-result'),
    importError: document.getElementById('import-error'),
    importCancel: document.getElementById('import-cancel'),
    importSubmit: document.getElementById('import-submit'),

    importHistoryModal: document.getElementById('import-history-modal'),
    importHistoryForm: document.getElementById('import-history-form'),
    importHistoryList: document.getElementById('import-history-list'),
    importHistoryAll: document.getElementById('history-all'),
    importHistoryError: document.getElementById('import-history-error'),
    importHistoryClose: document.getElementById('import-history-close'),

    tableBody: document.querySelector('#bands tbody'),

    prev: document.getElementById('prev'),
    next: document.getElementById('next'),
    pageInfo: document.getElementById('page-info'),

    bandModal: document.getElementById('band-modal'),
    bandForm: document.getElementById('band-form'),
    bandTitle: document.getElementById('band-title'),
    bName: document.getElementById('b-name'),
    bGenre: document.getElementById('b-genre'),
    bX: document.getElementById('b-x'),
    bY: document.getElementById('b-y'),
    bPart: document.getElementById('b-part'),
    bSingles: document.getElementById('b-singles'),
    bAlbums: document.getElementById('b-albums'),
    bDesc: document.getElementById('b-desc'),
    bBest: document.getElementById('b-best'),
    bFront: document.getElementById('b-front'),
    bandError: document.getElementById('band-error'),
    bandSave: document.getElementById('band-save'),
    bandCancel: document.getElementById('band-cancel'),

    personsModal: document.getElementById('persons-modal'),
    personsForm: document.getElementById('persons-form'),
    personsList: document.getElementById('persons-list'),
    personsClose: document.getElementById('persons-close'),
    addPerson: document.getElementById('add-person'),

    albumsModal: document.getElementById('albums-modal'),
    albumsForm: document.getElementById('albums-form'),
    albumsList: document.getElementById('albums-list'),
    albumsClose: document.getElementById('albums-close'),
    addAlbum: document.getElementById('add-album'),

    opsModal: document.getElementById('ops-modal'),
    opsTitle: document.getElementById('ops-title'),
    opsFields: document.getElementById('ops-fields'),
    opsRun: document.getElementById('ops-run'),
    opsCancel: document.getElementById('ops-cancel'),
    opsResult: document.getElementById('ops-result'),
    opsError: document.getElementById('ops-error')
};

function prettyError(e) {
    return e && e.message ? e.message : String(e);
}

function numOrNull(v) {
    const s = String(v ?? '').trim();
    return s === '' ? null : Number(s);
}

function fillSelectOptions(sel, items, valueKey, labelKey, allowNull) {
    sel.innerHTML = '';
    if (allowNull) {
        const o = document.createElement('option');
        o.value = '';
        o.textContent = '—';
        sel.appendChild(o);
    }
    for (const it of items) {
        const o = document.createElement('option');
        o.value = it[valueKey];
        o.textContent = it[labelKey];
        sel.appendChild(o);
    }
}

function currentQuery() {
    const p = new URLSearchParams();
    if (state.filters.name) p.append('nameEquals', state.filters.name);
    if (state.filters.desc) p.append('descriptionEquals', state.filters.desc);
    if (state.filters.album) p.append('albumNameEquals', state.filters.album);
    if (state.filters.front) p.append('frontManNameEquals', state.filters.front);
    if (state.filters.genre) p.append('genreEquals', state.filters.genre);
    p.append('page', state.page);
    p.append('size', state.size);
    p.append('sort', `${state.sort.prop},${state.sort.dir}`);
    return `?${p.toString()}`;
}

async function load() {
    const page = await api.bands(currentQuery());
    state.content = page.content || [];
    state.totalPages = page.totalPages || 1;
    state.totalElements = page.totalElements || 0;
    renderTable();
    renderPager();
}

function renderTable() {
    const tbody = els.tableBody;
    tbody.innerHTML = '';
    for (const b of state.content) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
      <td>${b.id}</td>
      <td>${b.name}</td>    
      <td><span class="badge">${b.genre}</span></td>
      <td>${b.albumsCount}</td>
      <td>${b.singlesCount}</td>
      <td>${b.frontManName || ''}</td>
      <td>${b.bestAlbumName || ''}</td>
      <td>${(b.creationDate || '').replace('T', ' ').substring(0, 19)}</td>
      <td class="row-actions">
        <button data-act="view" data-id="${b.id}">Просмотр</button>
        <div class="dropdown">
          <button data-act="more" data-id="${b.id}" aria-haspopup="true">…</button>
          <div class="menu">
            <button data-act="edit" data-id="${b.id}">Редактировать</button>
            <button data-act="dec" data-by="1" data-id="${b.id}">-1 участник</button>
            <button data-act="del" data-id="${b.id}">Удалить</button>
          </div>
        </div>
      </td>
    `;
        tbody.appendChild(tr);
    }
}

function renderPager() {
    els.pageInfo.textContent = `Стр. ${state.totalPages === 0 ? 0 : state.page + 1} из ${state.totalPages || 1}`;
    els.prev.disabled = state.page <= 0;
    els.next.disabled = state.page >= state.totalPages - 1;
}

function applyFilters() {
    state.filters = {
        name: els.fName.value.trim(),
        desc: els.fDesc.value.trim(),
        album: els.fAlbum.value.trim(),
        front: els.fFront.value.trim(),
        genre: els.fGenre.value
    };
    state.page = 0;
    load();
}

els.btnApply.onclick = applyFilters;

[els.fName, els.fDesc, els.fAlbum, els.fFront, els.fGenre].filter(Boolean).forEach((el) => {
    el.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            applyFilters();
        }
    });
});

els.btnReset.onclick = () => {
    els.fName.value = '';
    els.fDesc.value = '';
    els.fAlbum.value = '';
    els.fFront.value = '';
    els.fGenre.value = '';

    els.sortProp.value = 'id';
    els.sortDir.value = 'asc';
    els.pageSize.value = '10';

    state.filters = {name: '', desc: '', album: '', front: '', genre: ''};
    state.sort = {prop: 'id', dir: 'asc'};
    state.size = 10;
    state.page = 0;

    load();
};

els.sortProp.onchange = () => {
    state.sort.prop = els.sortProp.value;
    load();
};
els.sortDir.onchange = () => {
    state.sort.dir = els.sortDir.value;
    load();
};
els.pageSize.onchange = () => {
    state.size = Number(els.pageSize.value);
    state.page = 0;
    load();
};

els.prev.onclick = () => {
    if (state.page > 0) {
        state.page--;
        load();
    }
};
els.next.onclick = () => {
    if (state.page < state.totalPages - 1) {
        state.page++;
        load();
    }
};

els.tableBody.addEventListener('click', async (e) => {
    const btn = e.target.closest('button');
    if (!btn) return;

    const id = Number(btn.dataset.id);
    const act = btn.dataset.act;

    if (act === 'view') {
        try {
            const b = await api.band(id);
            openBandModal(b, false);
        } catch (err) {
            alert(prettyError(err));
        }
        return;
    }

    if (act === 'more') {
        const dd = btn.parentElement;
        document.querySelectorAll('.dropdown.open').forEach((d) => {
            if (d !== dd) d.classList.remove('open');
        });
        dd.classList.toggle('open');
        return;
    }

    if (act === 'edit') {
        try {
            const b = await api.band(id);
            openBandModal(b, true);
        } catch (err) {
            alert(prettyError(err));
        }
        return;
    }

    if (act === 'dec') {
        const by = Number(btn.dataset.by) || 1;
        btn.disabled = true;
        try {
            state.ws.muteNext = true;
            await api.decrement(id, by);
            document.querySelectorAll('.dropdown.open').forEach((d) => d.classList.remove('open'));
        } catch (err) {
            alert(prettyError(err));
            await load();
        } finally {
            btn.disabled = false;
        }
        return;
    }

    if (act === 'del') {
        try {
            state.ws.muteNext = true;
            await api.deleteBand(id);
            await load();
        } catch (err) {
            alert(prettyError(err));
        }
        return;
    }
});

document.addEventListener('click', (e) => {
    document.querySelectorAll('.dropdown.open').forEach((d) => {
        if (!d.contains(e.target)) d.classList.remove('open');
    });
});

els.btnCreate.onclick = () => openBandModal(null, false);
els.btnImport.onclick = () => openImportModal();
els.btnImportHistory.onclick = () => openImportHistoryModal();
els.btnPersons.onclick = () => openPersonsModal();
els.btnAlbums.onclick = () => openAlbumsModal();

els.btnOps.onclick = () => {
    els.opsMenu.parentElement.classList.toggle('open');
};
els.opsMenu.addEventListener('click', (e) => {
    const b = e.target.closest('button');
    if (!b) return;
    els.opsMenu.parentElement.classList.remove('open');
    openOpsModal(b.dataset.op);
});

els.bandCancel.onclick = () => {
    els.bandModal.close();
};
els.personsClose.onclick = () => {
    els.personsModal.close();
};
els.albumsClose.onclick = () => {
    els.albumsModal.close();
};
els.opsCancel.onclick = () => {
    els.opsModal.close();
};

els.bandSave.onclick = async () => {
    if (state.saving.band) return;
    if (!els.bandForm.checkValidity()) {
        els.bandForm.reportValidity();
        return;
    }
    state.saving.band = true;
    els.bandError.textContent = '';
    try {
        const dto = collectBandDto();
        state.ws.muteNext = true;
        if (dto.id) await api.updateBand(dto.id, dto);
        else await api.createBand(dto);
        await load();
        els.bandModal.close();
    } catch (e) {
        els.bandError.textContent = prettyError(e);
        els.bandForm.reportValidity();
    } finally {
        state.saving.band = false;
    }
};

function collectBandDto() {
    const id = els.bandForm.dataset.id ? Number(els.bandForm.dataset.id) : null;
    const bestId = els.bBest.value ? Number(els.bBest.value) : null;
    const frontId = els.bFront.value ? Number(els.bFront.value) : null;
    const y = Number(els.bY.value);
    if (y > 879) {
        throw new Error('Координата Y должна быть не больше 879');
    }

    return {
        id: id || undefined,
        name: els.bName.value.trim(),
        coordinates: {x: Number(els.bX.value), y: y},
        genre: els.bGenre.value,
        numberOfParticipants: numOrNull(els.bPart.value),
        singlesCount: Number(els.bSingles.value),
        description: els.bDesc.value.trim(),
        bestAlbumId: bestId,
        albumsCount: Number(els.bAlbums.value),
        frontManId: frontId
    };
}

function openBandModal(existing, isEdit) {
    const seq = ++state.modalOpenSeq;
    els.bandError.textContent = '';

    if (existing && !isEdit) {
        els.bandTitle.textContent = 'Просмотр';
        els.bName.value = existing.name || '';
        els.bGenre.value = existing.genre || 'ROCK';
        els.bX.value = existing.coordinates?.x ?? '';
        els.bY.value = existing.coordinates?.y ?? '';
        els.bPart.value = existing.numberOfParticipants ?? '';
        els.bSingles.value = existing.singlesCount ?? '';
        els.bAlbums.value = existing.albumsCount ?? '';
        els.bDesc.value = existing.description || '';
        els.bBest.innerHTML = `<option value="${existing.bestAlbumId ?? ''}">${existing.bestAlbumName || '(нет)'}</option>`;
        els.bFront.innerHTML = `<option value="${existing.frontManId ?? ''}">${existing.frontManName || ''}</option>`;
        els.bandForm.dataset.id = existing.id;
        els.bName.disabled = els.bGenre.disabled = els.bX.disabled = els.bY.disabled =
            els.bPart.disabled = els.bSingles.disabled = els.bAlbums.disabled =
                els.bDesc.disabled = els.bBest.disabled = els.bFront.disabled = true;
        els.bandModal.showModal();
        return;
    }

    const loadData = async () => {
        if (state.dataLoaded && state.albums && state.albums.length > 0 && state.persons && state.persons.length > 0) {
            fillSelectOptions(els.bBest, state.albums, 'id', 'name', true);
            fillSelectOptions(els.bFront, state.persons, 'id', 'name', false);
            if (seq !== state.modalOpenSeq) return;
            populateForm();
            return;
        }

        try {
            const [albums, persons] = await Promise.all([api.albums(), api.persons()]);
            if (seq !== state.modalOpenSeq) return;

            state.albums = albums || [];
            state.persons = persons || [];
            state.dataLoaded = true;

            fillSelectOptions(els.bBest, state.albums, 'id', 'name', true);
            fillSelectOptions(els.bFront, state.persons, 'id', 'name', false);
            populateForm();
        } catch (err) {
            if (seq !== state.modalOpenSeq) return;
            els.bandError.textContent = prettyError(err);
            els.bandModal.showModal();
        }
    };

    const populateForm = () => {

        els.bName.disabled = els.bGenre.disabled = els.bX.disabled = els.bY.disabled =
            els.bPart.disabled = els.bSingles.disabled = els.bAlbums.disabled =
                els.bDesc.disabled = els.bBest.disabled = els.bFront.disabled = false;

        if (existing) {
            els.bandTitle.textContent = 'Изменить группу';
            els.bName.value = existing.name || '';
            els.bGenre.value = existing.genre || 'ROCK';
            els.bX.value = existing.coordinates?.x ?? '';
            els.bY.value = existing.coordinates?.y ?? '';
            els.bPart.value = existing.numberOfParticipants ?? '';
            els.bSingles.value = existing.singlesCount ?? '';
            els.bAlbums.value = existing.albumsCount ?? '';
            els.bDesc.value = existing.description || '';
            els.bBest.value = existing.bestAlbumId ?? '';
            els.bFront.value = existing.frontManId ?? '';
            els.bandForm.dataset.id = existing.id;
        } else {
            els.bandTitle.textContent = 'Создать группу';
            els.bandForm.reset();
            els.bGenre.value = 'ROCK';
            els.bBest.value = '';
            els.bFront.value = state.persons.length ? String(state.persons[0].id) : '';
            delete els.bandForm.dataset.id;
        }

        els.bandModal.showModal();
    };

    loadData();

    els.bandModal.addEventListener(
        'close',
        () => {
            state.modalOpenSeq++;
        },
        {once: true}
    );
}

async function openPersonsModal() {
    await refreshPersonsList();
    els.personsModal.showModal();
}

async function refreshPersonsList() {
    const list = await api.persons();
    const c = els.personsList;
    c.innerHTML = '';

    for (const p of list) {
        const row = document.createElement('div');
        row.className = 'list-item';
        row.innerHTML = `
      <div>${p.id}. ${p.name} (${p.birthday}) • ${p.weight ?? ''} • ${p.nationality ?? ''}</div>
      <button data-id="${p.id}">Удалить</button>
    `;
        row.querySelector('button').onclick = async () => {
            try {
                await api.deletePerson(p.id);
                await refreshPersonsList();
            } catch (e) {
                document.getElementById('persons-error').textContent = prettyError(e);
            }
        };
        c.appendChild(row);
    }
}

els.addPerson.onclick = async () => {
    if (state.saving.person) return;
    if (!els.personsForm.checkValidity()) {
        els.personsForm.reportValidity();
        return;
    }
    state.saving.person = true;
    try {
        const dto = {
            name: document.getElementById('p-name').value.trim(),
            birthday: document.getElementById('p-bday').value,
            weight: numOrNull(document.getElementById('p-weight').value),
            nationality: document.getElementById('p-nat').value || null
        };
        await api.createPerson(dto);
        await refreshPersonsList();
        els.personsForm.reset();
    } catch (e) {
        document.getElementById('persons-error').textContent = prettyError(e);
    } finally {
        state.saving.person = false;
    }
};

async function openAlbumsModal() {
    await refreshAlbumsList();
    els.albumsModal.showModal();
}

async function refreshAlbumsList() {
    const list = await api.albums();
    const c = els.albumsList;
    c.innerHTML = '';

    for (const a of list) {
        const row = document.createElement('div');
        row.className = 'list-item';
        row.innerHTML = `
      <div>${a.id}. ${a.name} • ${a.sales}</div>
      <button data-id="${a.id}">Удалить</button>
    `;
        row.querySelector('button').onclick = async () => {
            try {
                await api.deleteAlbum(a.id);
                await refreshAlbumsList();
            } catch (e) {
                document.getElementById('albums-error').textContent = prettyError(e);
            }
        };
        c.appendChild(row);
    }
}

els.addAlbum.onclick = async () => {
    if (state.saving.album) return;
    if (!els.albumsForm.checkValidity()) {
        els.albumsForm.reportValidity();
        return;
    }
    state.saving.album = true;
    try {
        const dto = {
            name: document.getElementById('a-name').value.trim(),
            sales: Number(document.getElementById('a-sales').value)
        };
        await api.createAlbum(dto);
        await refreshAlbumsList();
        els.albumsForm.reset();
    } catch (e) {
        document.getElementById('albums-error').textContent = prettyError(e);
    } finally {
        state.saving.album = false;
    }
};

function openOpsModal(kind) {
    els.opsError.textContent = '';
    els.opsResult.textContent = '';
    els.opsFields.innerHTML = '';
    els.opsRun.dataset.kind = kind;

    if (kind === 'deleteByAlbums') {
        els.opsTitle.textContent = 'Удалить все с заданным albumsCount';
        const i = document.createElement('input');
        i.type = 'number';
        i.min = '1';
        i.placeholder = 'albumsCount';
        i.id = 'ops-v';
        els.opsFields.appendChild(i);
    } else if (kind === 'descStarts') {
        els.opsTitle.textContent = 'Найти: description начинается с...';
        const i = document.createElement('input');
        i.placeholder = 'prefix';
        i.id = 'ops-v';
        els.opsFields.appendChild(i);
    } else if (kind === 'byGenre') {
        els.opsTitle.textContent = 'Список по жанру';
        const s = document.createElement('select');
        s.id = 'ops-v';
        ['ROCK', 'PSYCHEDELIC_CLOUD_RAP', 'BLUES', 'POP', 'POST_PUNK'].forEach((g) => {
            const o = document.createElement('option');
            o.textContent = g;
            s.appendChild(o);
        });
        els.opsFields.appendChild(s);
    } else if (kind === 'decrement') {
        els.opsTitle.textContent = 'Уменьшить участников';
        const gid = document.createElement('input');
        gid.type = 'number';
        gid.placeholder = 'bandId';
        gid.id = 'ops-id';
        const by = document.createElement('input');
        by.type = 'number';
        by.placeholder = 'by';
        by.id = 'ops-v';
        els.opsFields.appendChild(gid);
        els.opsFields.appendChild(by);
    }

    els.opsModal.showModal();
}

els.opsRun.onclick = async () => {
    els.opsError.textContent = '';
    els.opsResult.textContent = '';
    try {
        const kind = els.opsRun.dataset.kind;
        if (kind === 'deleteByAlbums') {
            const v = Number(document.getElementById('ops-v').value);
            state.ws.muteNext = true;
            const r = await api.deleteByAlbumsCount(v);
            els.opsResult.textContent = j(r);
            await load();
        } else if (kind === 'descStarts') {
            const v = document.getElementById('ops-v').value;
            const r = await api.descStarts(v);
            els.opsResult.textContent = j(r);
        } else if (kind === 'byGenre') {
            const v = document.getElementById('ops-v').value;
            const r = await api.byGenre(v);
            els.opsResult.textContent = j(r);
        } else if (kind === 'decrement') {
            const id = Number(document.getElementById('ops-id').value);
            const by = Number(document.getElementById('ops-v').value);
            state.ws.muteNext = true;
            const r = await api.decrement(id, by);
            els.opsResult.textContent = j(r);
            await load();
        }
    } catch (e) {
        els.opsError.textContent = prettyError(e);
    }
};

function connectWs() {
    try {
        const sock = new SockJS('/ws');
        const client = Stomp.over(sock);
        client.debug = null;
        state.ws.client = client;
        state.ws.skipFirst = true;
        client.connect(
            {},
            () => {
                client.subscribe('/topic/bands', () => {
                    if (state.ws.skipFirst) {
                        state.ws.skipFirst = false;
                        return;
                    }
                    if (state.ws.muteNext) {
                        state.ws.muteNext = false;
                        return;
                    }
                    load();
                });
                client.subscribe('/topic/imports', () => {
                    if (els.importHistoryModal && els.importHistoryModal.open) {
                        refreshImportHistory();
                    }
                });
            },
            () => {
                setTimeout(connectWs, 1500);
            }
        );
    } catch (e) {
        setTimeout(connectWs, 1500);
    }
}

function openImportModal() {
    els.importForm.reset();
    els.importResult.textContent = '';
    els.importError.textContent = '';
    els.importModal.showModal();
}

els.importForm.onsubmit = async (e) => {
    e.preventDefault();
    if (!els.importFile.files || els.importFile.files.length === 0) {
        els.importError.textContent = 'Выберите файл';
        return;
    }
    els.importSubmit.disabled = true;
    els.importError.textContent = '';
    els.importResult.textContent = 'Загрузка...';
    try {
        const result = await api.importFile(els.importFile.files[0]);
        els.importResult.textContent = JSON.stringify(result, null, 2);
        if (result.status === 'SUCCESS') {
            setTimeout(() => {
                els.importModal.close();
                load();
            }, 1500);
        }
    } catch (err) {
        els.importError.textContent = prettyError(err);
        els.importResult.textContent = '';
    } finally {
        els.importSubmit.disabled = false;
    }
};

els.importCancel.onclick = () => els.importModal.close();

async function openImportHistoryModal() {
    await refreshImportHistory();
    els.importHistoryModal.showModal();
}

async function refreshImportHistory() {
    const all = els.importHistoryAll.checked;
    try {
        const history = await api.importHistory(all);
        const c = els.importHistoryList;
        c.innerHTML = '';
        if (history.length === 0) {
            c.innerHTML = '<div class="list-item">История пуста</div>';
            return;
        }
        for (const op of history) {
            const row = document.createElement('div');
            row.className = 'list-item';
            const statusColor = op.status === 'SUCCESS' ? '#4ade80' : op.status === 'FAILED' ? '#f87171' : '#fbbf24';
            const addedCount = op.status === 'SUCCESS' && op.addedCount != null ? ` (добавлено: ${op.addedCount})` : '';
            const error = op.errorMessage ? `\nОшибка: ${op.errorMessage}` : '';
            row.innerHTML = `
                <div>
                    <strong>ID: ${op.id}</strong> | ${op.userName} | 
                    <span style="color: ${statusColor}">${op.status}</span>${addedCount}
                    <br>
                    <small>Создано: ${op.createdAt ? op.createdAt.replace('T', ' ').substring(0, 19) : ''}</small>
                    ${error ? `<br><small style="color: #f87171">${error}</small>` : ''}
                </div>
            `;
            c.appendChild(row);
        }
    } catch (err) {
        els.importHistoryError.textContent = prettyError(err);
    }
}

els.importHistoryAll.onchange = () => refreshImportHistory();
els.importHistoryClose.onclick = () => els.importHistoryModal.close();

load();
connectWs();
