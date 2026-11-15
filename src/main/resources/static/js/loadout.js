/*
 * Weapon loadouts: which attachments fit a gun, and what a build adds up to.
 * Used by the item drawer (weapon builder) and the shooting range.
 *
 * Fit comes from each attachment's "used by" list in the wiki data. Weight, magazine capacity and optic zoom
 * are the game's values. The aim spread is an estimate (see SPREAD below): DayZ does not publish sway numbers.
 */
(function () {
    'use strict';
    const DZ = window.DZ;
    if (!DZ) return;
    const t = DZ.t;

    // Slot order and labels; keys are the attachment "type" values in the data.
    const SLOTS = [
        ['Magazines', t('Magazine'), 'fa-layer-group'],
        ['Optics', t('Optic'), 'fa-binoculars'],
        ['Muzzle Devices', t('Muzzle'), 'fa-wind'],
        ['Handguards', t('Handguard'), 'fa-grip-lines'],
        ['Buttstocks', t('Buttstock'), 'fa-gun'],
        ['Illumination', t('Light'), 'fa-lightbulb'],
        ['Bayonets', t('Bayonet'), 'fa-khanda'],
        ['Wraps', t('Wrap'), 'fa-scroll']
    ];

    // Aim spread estimate, in milliradians (1 mrad = 10 cm at 100 m), for an aimed shot while standing.
    const SPREAD = {
        base: 1.2,          // iron sights: ~12 cm (1 sigma) at 100 m
        perZoom: 0.3,       // each extra x of zoom steadies the aim a little (diminishing)
        noStock: 1.5,       // gun has a removable stock but none is fitted
        noHandguard: 1.12   // gun takes a handguard but none is fitted
    };

    let cache = null;
    function load() {
        if (cache) return cache;
        cache = (async () => {
            try {
                const saved = sessionStorage.getItem('dz-modules-v1');
                if (saved) return JSON.parse(saved);
            } catch (e) { /* ignore */ }
            const lists = await Promise.all(SLOTS.map(([type]) =>
                fetch('/modules/type/' + encodeURIComponent(type)).then(r => r.ok ? r.json() : []).catch(() => [])));
            const mods = lists.flat().map(m => ({
                name: m.englishName, type: m.type, image: m.image, weight: m.weight || 0,
                capacity: m.capacity || null, zoom: m.magnification || null,
                fits: (m.usedBy || '').split(',').map(s => s.trim()).filter(Boolean)
            }));
            try { sessionStorage.setItem('dz-modules-v1', JSON.stringify(mods)); } catch (e) { /* ignore */ }
            return mods;
        })();
        return cache;
    }

    /** { slotType: [attachments that fit] } for one weapon, only slots that have options. */
    function slotsFor(weaponName, mods) {
        const out = {};
        SLOTS.forEach(([type]) => {
            const list = mods.filter(m => m.type === type && m.fits.indexOf(weaponName) > -1)
                .sort((a, b) => (a.capacity || 0) - (b.capacity || 0) || a.name.localeCompare(b.name));
            if (list.length) out[type] = list;
        });
        return out;
    }

    /** Totals for a build. picked: { slotType: attachment } */
    function summary(weapon, slots, picked) {
        const parts = Object.values(picked).filter(Boolean);
        const mag = picked['Magazines'];
        const optic = picked['Optics'];
        const muzzle = picked['Muzzle Devices'];
        const zoom = optic && optic.zoom ? optic.zoom : 1;
        let spread = SPREAD.base / (1 + SPREAD.perZoom * Math.sqrt(Math.max(0, zoom - 1)) * 2);
        const noStock = slots['Buttstocks'] && !picked['Buttstocks'];
        const noGuard = slots['Handguards'] && !picked['Handguards'];
        if (noStock) spread *= SPREAD.noStock;
        if (noGuard) spread *= SPREAD.noHandguard;
        return {
            weight: (weapon.weight || 0) + parts.reduce((n, m) => n + (m.weight || 0), 0),
            capacity: mag ? mag.capacity : null,
            zoom,
            suppressed: !!(muzzle && /suppress/i.test(muzzle.name)),
            noStock: !!noStock,
            spread
        };
    }

    // Remember the last build per weapon (this browser only).
    function saved(weaponName) {
        try { return JSON.parse(localStorage.getItem('dz-build:' + weaponName) || '{}'); } catch (e) { return {}; }
    }
    function save(weaponName, picked) {
        const names = {};
        Object.keys(picked).forEach(k => { if (picked[k]) names[k] = picked[k].name; });
        try { localStorage.setItem('dz-build:' + weaponName, JSON.stringify(names)); } catch (e) { /* ignore */ }
    }
    function restore(slots, names) {
        const picked = {};
        Object.keys(slots).forEach(k => { picked[k] = slots[k].find(m => m.name === (names || {})[k]) || null; });
        return picked;
    }

    /** Slot pickers + summary, rendered into `root`. onChange(picked, summary) runs after every change. */
    function renderBuilder(root, weapon, slots, picked, onChange, opts) {
        const esc = DZ.esc;
        const types = SLOTS.filter(([k]) => slots[k]);
        if (!types.length) { root.innerHTML = '<p class="muted small">' + esc(t('No attachments fit this weapon.')) + '</p>'; return; }
        root.innerHTML = '<div class="build-slots">' + types.map(([k, label, icon]) =>
            '<label class="build-slot"><span class="build-ico"><i class="fa-solid ' + icon + '"></i></span><span class="build-main"><small>' + esc(label) + '</small>' +
            '<select class="field" data-build="' + esc(k) + '"><option value="">' + esc(t('— none —')) + '</option>' +
            slots[k].map(m => '<option' + (picked[k] && picked[k].name === m.name ? ' selected' : '') + '>' + esc(m.name) + '</option>').join('') +
            '</select></span></label>').join('') + '</div><div class="build-sum"></div>';
        const draw = () => {
            const s = summary(weapon, slots, picked);
            const chip = (icon, txt, cls) => '<span class="build-chip' + (cls ? ' ' + cls : '') + '"><i class="fa-solid ' + icon + '"></i>' + esc(txt) + '</span>';
            root.querySelector('.build-sum').innerHTML =
                chip('fa-weight-hanging', t('{0} kg total', (Math.round(s.weight * 100) / 100).toLocaleString(DZ.lang === 'ru' ? 'ru-RU' : 'en-US'))) +
                (s.capacity ? chip('fa-layer-group', t('{0} rounds per magazine', s.capacity)) : '') +
                (s.zoom > 1 ? chip('fa-binoculars', s.zoom + 'x') : '') +
                (s.suppressed ? chip('fa-volume-xmark', t('Suppressed')) : '') +
                (s.noStock ? chip('fa-triangle-exclamation', t('No stock: more sway'), 'warn') : '') +
                (opts && opts.rangeLink ? '<a class="build-chip link" href="' + rangeUrl(weapon.englishName || weapon.name, picked) + '"><i class="fa-solid fa-bullseye"></i>' + esc(t('Try in the shooting range')) + '</a>' : '');
            if (onChange) onChange(picked, s);
        };
        root.querySelectorAll('[data-build]').forEach(sel => sel.addEventListener('change', () => {
            const k = sel.dataset.build;
            picked[k] = slots[k].find(m => m.name === sel.value) || null;
            save(weapon.englishName || weapon.name, picked);
            draw();
        }));
        draw();
    }

    function rangeUrl(name, picked) {
        const names = Object.values(picked).filter(Boolean).map(m => m.name);
        return '/range?w=' + encodeURIComponent(name) + (names.length ? '&att=' + encodeURIComponent(names.join('|')) : '');
    }

    DZ.Loadout = { SLOTS, SPREAD, load, slotsFor, summary, saved, save, restore, renderBuilder, rangeUrl };

    // Weapon builder in the item drawer.
    (DZ.itemHooks = DZ.itemHooks || []).push(async (item, body) => {
        if (item.source !== 'weapons') return;
        const box = document.createElement('div');
        box.className = 'build';
        box.innerHTML = '<div class="dz-subhead">' + DZ.esc(t('Loadout')) + '</div><div class="build-body"><div class="skeleton" style="height:90px"></div></div>';
        // Right under the description, above the stats table.
        const details = body.querySelector('.dz-details');
        const where = details && details.querySelector('.dz-stats');
        if (where) details.insertBefore(box, where); else (details || body).appendChild(box);
        const mods = await load();
        if (!box.isConnected) return;   // another item was opened meanwhile
        const slots = slotsFor(item.englishName, mods);
        if (!Object.keys(slots).length) { box.remove(); return; }
        renderBuilder(box.querySelector('.build-body'), item, slots, restore(slots, saved(item.englishName)), null, { rangeLink: true });
    });
})();
