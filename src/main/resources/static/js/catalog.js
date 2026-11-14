/*
 * Category page: loads a whole section once, then filters / sorts / switches views client-side.
 * Expects <main data-section="weapons"> and the shell from app.js (window.DZ).
 */
(function () {
    'use strict';
    const DZ = window.DZ;
    const root = document.querySelector('main[data-section]');
    if (!root || !DZ) return;
    const sec = DZ.SECTIONS[root.dataset.section];
    const { esc, present, tierInfo, mapDots, slug, t } = DZ;

    const els = {
        chips: document.getElementById('typeChips'),
        q: document.getElementById('filterInput'),
        map: document.getElementById('mapFilter'),
        sort: document.getElementById('sortSelect'),
        count: document.getElementById('resultCount'),
        out: document.getElementById('results'),
        views: document.querySelectorAll('[data-view]')
    };

    const params = new URLSearchParams(location.search);
    const state = {
        items: [],
        type: params.get('type') || 'All',
        q: params.get('q') || '',
        map: params.get('map') || '',
        sort: params.get('sort') || 'name',
        view: (function () { try { return localStorage.getItem('dz-view') || 'grid'; } catch (e) { return 'grid'; } })()
    };

    // ---------- Setup controls ----------
    sec.sorts.forEach(([k, label]) => els.sort.insertAdjacentHTML('beforeend', '<option value="' + k + '">' + esc(label) + '</option>'));
    els.sort.value = [...els.sort.options].some(o => o.value === state.sort) ? state.sort : 'name';
    els.q.value = state.q;
    els.map.value = state.map;

    function syncUrl() {
        const p = new URLSearchParams();
        if (state.type !== 'All') p.set('type', state.type);
        if (state.q) p.set('q', state.q);
        if (state.map) p.set('map', state.map);
        if (state.sort !== 'name') p.set('sort', state.sort);
        const qs = p.toString();
        history.replaceState(null, '', location.pathname + (qs ? '?' + qs : '') + location.hash);
    }

    // ---------- Data ----------
    async function load() {
        els.out.innerHTML = '<div class="grid">' + '<div class="skeleton"></div>'.repeat(8) + '</div>';
        try {
            let lists;
            if (sec.mode === 'type') {
                lists = await Promise.all(sec.types.map(t => fetch(sec.path + '/type/' + encodeURIComponent(t)).then(r => r.json())));
            } else {
                lists = [await fetch(sec.path + '/source/' + encodeURIComponent(sec.key)).then(r => r.json())];
            }
            state.items = lists.flat();
        } catch (e) {
            els.out.innerHTML = '<div class="empty"><i class="fa-solid fa-triangle-exclamation"></i>' + esc(t('Could not load this section. Refresh to try again.')) + '</div>';
            return;
        }
        buildChips();
        render();
        openFromHash();
    }

    function buildChips() {
        if (!els.chips) return;
        if (sec.mode !== 'type') { els.chips.hidden = true; return; }
        const counts = {};
        state.items.forEach(i => { counts[i.type] = (counts[i.type] || 0) + 1; });
        const types = ['All'].concat(sec.types.filter(t => counts[t]));
        if (types.indexOf(state.type) < 0) state.type = 'All';
        els.chips.innerHTML = types.map(ty =>
            '<button class="chip' + (ty === state.type ? ' active' : '') + '" data-type="' + esc(ty) + '">' + esc(t(ty)) +
            ' <span class="n">' + (ty === 'All' ? state.items.length : counts[ty]) + '</span></button>').join('');
        els.chips.querySelectorAll('.chip').forEach(c => c.addEventListener('click', () => {
            state.type = c.dataset.type;
            els.chips.querySelectorAll('.chip').forEach(x => x.classList.toggle('active', x === c));
            render(); syncUrl();
        }));
    }

    // ---------- Filtering / sorting ----------
    function filtered() {
        const q = state.q.toLowerCase();
        let list = state.items.filter(i =>
            (state.type === 'All' || i.type === state.type) &&
            (!state.map || (i.maps && i.maps.indexOf(state.map) > -1)) &&
            (!q || [i.englishName, i.altName, i.description, i.type, t(i.type || ''), i.causes, i.symptoms]
                .some(v => v && String(v).toLowerCase().indexOf(q) > -1)));
        const k = state.sort;
        if (k === 'name') list.sort((a, b) => a.englishName.localeCompare(b.englishName));
        else list.sort((a, b) => (num(b[k]) - num(a[k])) || a.englishName.localeCompare(b.englishName));
        return list;
    }
    const num = v => (typeof v === 'number' && !isNaN(v) ? v : (parseFloat(v) || -Infinity));

    function statValue(item, [, field, fmt]) {
        const v = typeof field === 'function' ? field(item) : item[field];
        return present(v) ? (fmt ? fmt(v) : v) : null;
    }

    // ---------- Rendering ----------
    function cardHtml(item, idx) {
        const tier = tierInfo(item.tier);
        const desc = sec.id === 'diseases' ? (item.symptoms ? t('Symptoms: {0}', item.symptoms) : '') : (item.description || '');
        const stats = sec.stats.map(s => [s[0], statValue(item, s)]).filter(s => s[1] !== null);
        return '<button class="card" data-idx="' + idx + '" style="animation-delay:' + Math.min(idx, 20) * 15 + 'ms">' +
            '<div class="card-media">' + DZ.thumb(item.image, sec.id, 'md') + '</div>' +
            '<div class="card-top"><div><div class="card-type">' + esc(item.type ? t(item.type) : sec.title) + '</div>' +
            '<div class="card-name">' + esc(item.englishName) + '</div></div>' +
            (tier ? '<span class="badge tier ' + tier.cls + '" title="' + esc(DZ_I18N.vocab(item.tier)) + '">' + tier.label + '</span>' : '') + '</div>' +
            (desc ? '<p class="card-desc">' + esc(desc) + '</p>' : '') +
            (stats.length ? '<div class="card-stats c' + stats.length + '" style="--cols:' + stats.length + '">' + stats.map(([k, v]) =>
                '<div class="stat"><div class="stat-k">' + esc(k) + '</div><div class="stat-v" title="' + esc(v) + '">' + esc(v) + '</div></div>').join('') + '</div>' : '') +
            ((item.maps || item.rarity) ? '<div class="card-foot">' + mapDots(item.maps) + '<span class="muted small">' + esc(DZ_I18N.vocab(item.rarity || '')) + '</span></div>' : '') +
            '</button>';
    }

    function tableHtml(list) {
        const cols = sec.stats;
        return '<div class="table-wrap"><table class="table"><thead><tr><th>' + esc(t('Name')) + '</th>' + (sec.mode === 'type' ? '<th>' + esc(t('Type')) + '</th>' : '') +
            cols.map(c => '<th>' + esc(c[0]) + '</th>').join('') + '<th>' + esc(t('Tier')) + '</th><th>' + esc(t('Maps')) + '</th></tr></thead><tbody>' +
            list.map((item, idx) => {
                const tier = tierInfo(item.tier);
                return '<tr data-idx="' + idx + '"><td class="name"><span class="name-cell">' + DZ.thumb(item.image, sec.id, 'sm') + esc(item.englishName) + '</span></td>' +
                    (sec.mode === 'type' ? '<td>' + esc(t(item.type)) + '</td>' : '') +
                    cols.map(c => { const v = statValue(item, c); return '<td class="num">' + (v === null ? '<span class="muted">—</span>' : esc(v)) + '</td>'; }).join('') +
                    '<td>' + (tier ? '<span class="badge tier ' + tier.cls + '">' + tier.label + '</span>' : '<span class="muted">—</span>') + '</td>' +
                    '<td>' + (mapDots(item.maps) || '<span class="muted">—</span>') + '</td></tr>';
            }).join('') + '</tbody></table></div>';
    }

    let current = [];
    function render() {
        current = filtered();
        els.count.textContent = current.length + ' / ' + state.items.length;
        els.views.forEach(b => b.classList.toggle('active', b.dataset.view === state.view));
        if (!current.length) {
            els.out.innerHTML = '<div class="empty"><i class="fa-solid fa-filter-circle-xmark"></i>' + esc(t('No entries match these filters.')) + '</div>';
            return;
        }
        els.out.innerHTML = state.view === 'table' ? tableHtml(current)
            : '<div class="grid">' + current.map(cardHtml).join('') + '</div>';
        els.out.querySelectorAll('[data-idx]').forEach(el => el.addEventListener('click', () => open(current[+el.dataset.idx])));
    }

    // ---------- Drawer + deep links ----------
    function open(item) {
        DZ.openItem(item);
        history.replaceState(null, '', location.pathname + location.search + '#' + slug(item.englishName));
    }
    DZ.onDrawerClose = () => history.replaceState(null, '', location.pathname + location.search);
    function openFromHash() {
        const h = decodeURIComponent(location.hash.slice(1));
        if (!h) return;
        const item = state.items.find(i => slug(i.englishName) === h);
        if (item) DZ.openItem(item);
    }
    window.addEventListener('hashchange', openFromHash);

    // ---------- Events ----------
    let timer = null;
    els.q.addEventListener('input', () => { clearTimeout(timer); timer = setTimeout(() => { state.q = els.q.value.trim(); render(); syncUrl(); }, 120); });
    els.map.addEventListener('change', () => { state.map = els.map.value; render(); syncUrl(); });
    els.sort.addEventListener('change', () => { state.sort = els.sort.value; render(); syncUrl(); });
    els.views.forEach(b => b.addEventListener('click', () => {
        state.view = b.dataset.view;
        try { localStorage.setItem('dz-view', state.view); } catch (e) { /* ignore */ }
        render();
    }));

    load();
})();
