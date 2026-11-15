/*
 * Item comparison: up to 4 items from one section, side by side.
 * The list lives in this browser (localStorage); /compare renders it.
 */
(function () {
    'use strict';
    const DZ = window.DZ;
    if (!DZ) return;
    const { t, esc } = DZ;
    const $ = (s, r) => (r || document).querySelector(s);
    const MAX = 4, KEY = 'dz-compare';

    // [label, field or fn, direction (1 higher is better, -1 lower is better, 0 no ranking), format?]
    const kg = v => v >= 1 ? (+v.toFixed(2)) + ' ' + t('kg') : Math.round(v * 1000) + ' ' + t('g');
    const caliber = i => i.caliber ? i.caliber.englishName : (i.damageType || null);
    const FIELDS = {
        weapons: [['Damage', 'damage', 1], ['Heavy attack damage', 'heavyDamage', 1], ['Caliber', caliber, 0], ['Fire modes', 'fireModes', 0],
            ['Magazines', 'magazines', 0], ['Weight', 'weight', -1, kg], ['Durability', 'durability', 1], ['Size', 'size', 0]],
        ammunition: [['Health damage', 'healthDamage', 1], ['Blood damage', 'bloodDamage', 1], ['Shock damage', 'shockDamage', 1],
            ['Weight per round', 'weight', -1, kg], ['Used by', 'usedBy', 0]],
        modules: [['Magnification', 'magnification', 0, v => v + 'x'], ['Capacity', 'capacity', 1], ['Weight', 'weight', -1, kg], ['Fits', 'usedBy', 0]],
        explosives: [['Delivery', 'delivery', 0], ['Weight', 'weight', -1, kg]],
        armors: [['Cargo capacity', 'capacity', 1], ['Damage reduction', 'protection', 0], ['Heat insulation', 'insulation', 1],
            ['Durability', 'durability', 1], ['Weight', 'weight', -1, kg], ['Size', 'size', 0]],
        foods: [['Energy (full item)', 'baseCalories', 1, v => v + ' ' + t('kcal')], ['Water (full item)', 'baseThirst', 1, v => v + ' ' + t('ml')],
            ['Stomach volume', 'stomach', -1, v => v + ' ' + t('ml')], ['Weight', 'weight', -1, kg]],
        medicaments: [['Use', 'medicalUse', 0], ['Uses', 'numberOfUses', 1], ['Weight', 'weight', -1, kg]],
        cars: [['Top speed', 'topSpeed', 1, v => '~' + v + ' ' + t('km/h')], ['Seats', 'seats', 1], ['Fuel tank', 'fuelTank', 1, v => v + ' ' + t('L')], ['Cargo', 'capacity', 1]],
        diseases: [['Causes', 'causes', 0], ['Symptoms', 'symptoms', 0], ['Treatment', 'treatment', 0]]
    };
    const COMMON = [['Tier', 'tier', 0], ['Maps', 'maps', 0]];

    function read() {
        try { const v = JSON.parse(localStorage.getItem(KEY) || 'null'); if (v && Array.isArray(v.items)) return v; } catch (e) { /* ignore */ }
        return { section: null, items: [] };
    }
    function write(v) {
        try { localStorage.setItem(KEY, JSON.stringify(v)); } catch (e) { /* ignore */ }
        refresh();
    }
    const has = (list, url) => list.items.some(i => i.url === url);

    function toggle(item) {
        const list = read(), url = item.urlName;
        if (has(list, url)) { list.items = list.items.filter(i => i.url !== url); write(list); DZ.toast(t('Removed from comparison.')); return; }
        if (list.section && list.section !== item.source && list.items.length) {
            list.items = [];
            DZ.toast(t('Started a new comparison: items from different sections can\'t be compared.'));
        }
        if (list.items.length >= MAX) { DZ.toast(t('You can compare up to 4 items.'), false); return; }
        list.section = item.source;
        list.items.push({ url, name: item.englishName });
        write(list);
        DZ.toast(t('Added to comparison ({0}/4).', list.items.length));
    }

    // ---------- Tray, sidebar badge, drawer button ----------
    let drawerItem = null;
    function refresh() {
        const list = read(), n = list.items.length;
        const tray = $('#compareTray');
        if (tray) {
            tray.hidden = n === 0 || !!$('#comparePage');
            const sec = DZ.SECTIONS[list.section];
            $('#compareTrayText').textContent = t('{0} of 4 · {1}', n, sec ? sec.title : '');
        }
        document.querySelectorAll('[data-compare-count]').forEach(el => { el.hidden = !n; el.textContent = n; });
        const btn = $('#drawerCompare');
        if (btn && drawerItem) btn.classList.toggle('on', has(list, drawerItem.urlName));
    }
    (DZ.itemHooks = DZ.itemHooks || []).push(item => { drawerItem = item; refresh(); });
    $('#drawerCompare') && $('#drawerCompare').addEventListener('click', () => { if (drawerItem) toggle(drawerItem); });
    $('#compareClear') && $('#compareClear').addEventListener('click', () => write({ section: null, items: [] }));
    window.addEventListener('storage', e => { if (e.key === KEY) refresh(); });
    refresh();

    // ---------- /compare page ----------
    const page = $('#comparePage');
    if (!page) return;
    const out = $('#compareOut');

    const num = v => typeof v === 'number' ? v : (typeof v === 'string' && /^-?\d+(\.\d+)?$/.test(v.trim()) ? +v : NaN);
    const present = v => v !== null && v !== undefined && v !== '' && !(typeof v === 'number' && isNaN(v));

    async function render() {
        const list = read();
        if (!list.items.length) {
            out.innerHTML = '<div class="empty empty-big"><span class="empty-icon"><i class="fa-solid fa-code-compare"></i></span><h2>' + esc(t('Nothing to compare yet')) +
                '</h2><p>' + esc(t('Open any item and tap the compare icon at the top of its card, or search below.')) + '</p></div>';
            return;
        }
        out.innerHTML = '<div class="skeleton" style="height:240px"></div>';
        const items = (await Promise.all(list.items.map(i => fetch(i.url).then(r => r.ok ? r.json() : null).catch(() => null)))).filter(Boolean);
        const sec = DZ.SECTIONS[list.section] || {};
        const fields = (FIELDS[list.section] || []).concat(COMMON);
        const rows = fields.map(([label, field, dir, fmt]) => {
            const vals = items.map(it => typeof field === 'function' ? field(it) : it[field]);
            if (!vals.some(present)) return '';
            const nums = vals.map(num);
            let best = null;
            if (dir && items.length > 1 && nums.filter(n => !isNaN(n) && n !== 0).length > 1) {
                const valid = nums.filter(n => !isNaN(n) && n !== 0);
                best = dir > 0 ? Math.max(...valid) : Math.min(...valid);
                if (valid.every(n => n === best)) best = null;
            }
            return '<tr><th>' + esc(t(label)) + '</th>' + vals.map((v, i) => {
                const shown = present(v) && v !== 0 ? (fmt ? fmt(v) : (field === 'tier' || field === 'maps' ? DZ_I18N.vocab(String(v)) : v)) : '—';
                return '<td class="' + (best !== null && nums[i] === best ? 'best' : '') + '">' + esc(shown) + '</td>';
            }).join('') + '</tr>';
        }).join('');
        out.innerHTML = '<div class="cmp-wrap"><table class="cmp"><thead><tr><th class="cmp-sec">' + esc(sec.title || '') + '<small>' + esc(t('{0} of 4', items.length)) + '</small></th>' +
            items.map(it => '<th><div class="cmp-item">' + DZ.thumb(it.image, it.source, 'md') +
                '<a href="' + esc(DZ.itemHref(it.urlName)) + '">' + esc(it.englishName) + '</a>' +
                '<button class="icon-btn" data-remove="' + esc(it.urlName) + '" aria-label="' + esc(t('Remove')) + '"><i class="fa-solid fa-xmark"></i></button></div></th>').join('') +
            '</tr></thead><tbody>' + rows + '</tbody></table></div>' +
            '<p class="muted small cmp-note"><span class="best-dot"></span> ' + esc(t('Best value in the row')) + '</p>';
        out.querySelectorAll('[data-remove]').forEach(b => b.addEventListener('click', () => {
            const l = read(); l.items = l.items.filter(i => i.url !== b.dataset.remove); write(l); render();
        }));
    }

    // Add items by search (limited to the current section once there is one).
    const input = $('#compareSearch'), pop = $('#compareHits');
    let timer = null, seq = 0;
    input.addEventListener('input', () => {
        clearTimeout(timer);
        timer = setTimeout(async () => {
            const q = input.value.trim();
            if (q.length < 2) { pop.hidden = true; return; }
            const my = ++seq;
            try {
                const data = await DZ.searchApi(q, 12);
                if (my !== seq) return;
                const sec = read().section;
                const hits = data.items.filter(it => !sec || it.source.sourceTable === sec).slice(0, 8);
                pop.innerHTML = hits.length ? hits.map(it => '<button class="search-row" data-url="' + esc(it.url) + '">' + DZ.thumb(it.image, it.source.sourceTable, 'sm') +
                    '<span><span class="sr-name">' + esc(it.englishName) + '</span><br><span class="sr-meta">' + esc(DZ.titleFor(it.source.sourceTable)) + '</span></span></button>').join('')
                    : '<div class="search-empty">' + esc(t('Nothing matches “{0}”.', q)) + '</div>';
                pop.hidden = false;
                pop.querySelectorAll('[data-url]').forEach(b => b.addEventListener('click', async () => {
                    const r = await fetch(b.dataset.url);
                    if (r.ok) { toggleOn(await r.json()); input.value = ''; pop.hidden = true; render(); }
                }));
            } catch (e) { /* ignore */ }
        }, 160);
    });
    document.addEventListener('click', e => { if (!e.target.closest('.cmp-add')) pop.hidden = true; });
    function toggleOn(item) { if (!has(read(), item.urlName)) toggle(item); }

    render();
})();
