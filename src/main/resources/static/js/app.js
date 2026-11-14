/*
 * DayZ Wiki — application shell shared by every page.
 * Exposes window.DZ with section config, helpers and the item drawer.
 */
(function () {
    'use strict';

    const I18N = window.DZ_I18N || { lang: 'en', t: s => s, plural: (n, a, b, c) => (n === 1 ? a : (c || b)) };
    const t = I18N.t;

    // ---------- Section configuration ----------
    const kg = v => (v >= 1 ? (+v.toFixed(2)) + ' ' + t('kg') : Math.round(v * 1000) + ' ' + t('g'));
    const SECTIONS = {
        weapons: {
            title: t('Weapons'), icon: 'fa-gun', path: '/weapons', mode: 'type',
            blurb: t('Firearms, bows, launchers and melee weapons, with damage, calibers and magazines.'),
            types: ['Assault Rifles', 'Marksman Rifles', 'Rifles', 'Submachine Guns', 'Shotguns', 'Handguns', 'Launchers and Bows', 'Melee'],
            stats: [[t('Damage'), 'damage'], [t('Caliber'), i => i.caliber ? i.caliber.englishName.replace(/ Rounds?$/, '') : (i.damageType || null)], [t('Weight'), 'weight', kg]],
            sorts: [['damage', t('Damage')], ['weight', t('Weight')]]
        },
        ammunition: {
            title: t('Ammunition'), icon: 'fa-circle-dot', path: '/ammunition', mode: 'type',
            blurb: t('Every cartridge, shell, grenade and bolt, with health, blood and shock damage.'),
            types: ['Pistol', 'Intermediate', 'Full-power', 'Shotgun Shells', '40mm Grenades', 'Arrows and Bolts'],
            stats: [[t('Health'), 'healthDamage'], [t('Blood'), 'bloodDamage'], [t('Shock'), 'shockDamage']],
            sorts: [['healthDamage', t('Health damage')], ['shockDamage', t('Shock damage')]]
        },
        modules: {
            title: t('Attachments'), icon: 'fa-crosshairs', path: '/modules', mode: 'type',
            blurb: t('Optics, magazines, suppressors, stocks, handguards and the weapons they fit.'),
            types: ['Optics', 'Magazines', 'Muzzle Devices', 'Handguards', 'Buttstocks', 'Illumination', 'Bayonets', 'Wraps'],
            stats: [[t('Zoom'), 'magnification', v => v + 'x'], [t('Rounds'), 'capacity'], [t('Weight'), 'weight', kg]],
            sorts: [['capacity', t('Capacity')], ['magnification', t('Zoom')], ['weight', t('Weight')]]
        },
        explosives: {
            title: t('Explosives'), icon: 'fa-bomb', path: '/explosives', mode: 'source', key: 'Explosives',
            blurb: t('Grenades, mines and charges — how they are deployed and where to find them.'),
            stats: [[t('Kind'), 'type'], [t('Delivery'), 'delivery'], [t('Weight'), 'weight', kg]],
            sorts: [['weight', t('Weight')]]
        },
        foods: {
            title: t('Food & Drink'), icon: 'fa-utensils', path: '/foods', mode: 'type',
            blurb: t('Energy and water values for canned goods, game meat, produce, drinks and Nasdara specialities.'),
            types: ['Canned Goods', 'Dry Goods and Snacks', 'Drinks', 'Fruits and Vegetables', 'Meat and Fish', 'Mushrooms', 'Other'],
            stats: [[t('Energy'), 'baseCalories'], [t('Water'), 'baseThirst'], [t('Weight'), 'weight', kg]],
            sorts: [['baseCalories', t('Energy')], ['baseThirst', t('Water')], ['weight', t('Weight')]]
        },
        medicaments: {
            title: t('Medical'), icon: 'fa-kit-medical', path: '/medication', mode: 'type',
            blurb: t('Bandages, pills, injectors and transfusion kits — what each one treats.'),
            types: ['Treatment', 'Medicines', 'Pain Relief', 'Disinfectants', 'Medical Equipment'],
            stats: [[t('Uses'), 'numberOfUses', v => String(v).split(' ')[0]], [t('Size'), 'size'], [t('Weight'), 'weight', kg]],
            sorts: [['weight', t('Weight')]]
        },
        diseases: {
            title: t('Diseases'), icon: 'fa-virus', path: '/diseases', mode: 'source', key: 'Diseases',
            blurb: t('Every illness and condition, including the new Badlands heat and sandstorm ailments.'),
            stats: [],
            sorts: []
        },
        armors: {
            title: t('Clothing & Gear'), icon: 'fa-shirt', path: '/armors', mode: 'type',
            blurb: t('Clothing, armour, backpacks and belts: storage, insulation and damage reduction.'),
            types: ['Helmets', 'Hats', 'Masks', 'Eyewear', 'Shirts and Jackets', 'Vests', 'Gloves', 'Pants', 'Shoes', 'Belts', 'Backpacks', 'Ghillie and Camo', 'Accessories'],
            stats: [[t('Slots'), 'capacity'], [t('Insul.'), 'insulation'], [t('Weight'), 'weight', kg]],
            sorts: [['capacity', t('Storage')], ['weight', t('Weight')], ['durability', t('Durability')]]
        },
        cars: {
            title: t('Vehicles'), icon: 'fa-car-side', path: '/cars', mode: 'source', key: 'Cars',
            blurb: t('Cars, trucks, the boat and the new 1.30 motorbikes, with spawn caps for each map.'),
            stats: [[t('km/h'), 'topSpeed', v => '~' + v], [t('Seats'), 'seats'], [t('Fuel'), 'fuelTank', v => v + ' ' + t('L')]],
            sorts: [['topSpeed', t('Top speed')], ['fuelTank', t('Fuel tank')]]
        }
    };
    const BY_PATH = {};
    Object.entries(SECTIONS).forEach(([k, s]) => { s.id = k; BY_PATH[s.path.slice(1)] = s; });

    // ---------- Helpers ----------
    const $ = (s, r = document) => r.querySelector(s);
    const esc = v => String(v ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
    const slug = name => String(name).replace(/ /g, '_').toLowerCase();
    const present = v => v !== null && v !== undefined && v !== '' && !(typeof v === 'number' && (v === 0 || isNaN(v)));

    function tierInfo(tier) {
        if (!tier) return null;
        const m = /Tier (\d)(?:-(\d))?/.exec(tier);
        if (m) {
            const hi = +(m[2] || m[1]);
            return { label: 'T' + m[1] + (m[2] ? '–' + m[2] : ''), cls: 't' + hi };
        }
        if (/Unique/i.test(tier)) return { label: t('Unique'), cls: 'tu' };
        return null;
    }

    const MAPS = [['C', 'Chernarus'], ['L', 'Livonia'], ['S', 'Sakhal'], ['N', 'Nasdara']];
    function mapDots(maps) {
        if (!maps) return '';
        return '<span class="maps" aria-label="' + esc(t('Spawns on: {0}', maps)) + '">' + MAPS.map(([k, n]) =>
            '<span class="map-dot' + (maps.indexOf(n) > -1 ? ' on' : '') + '" title="' + esc(t(n)) + '">' + k + '</span>').join('') + '</span>';
    }

    /**
     * Item picture with the section icon underneath as a fallback (missing image or failed load).
     * size: 'sm' (search rows, table), 'md' (cards), 'lg' (drawer)
     */
    function thumb(image, table, size) {
        const icon = (SECTIONS[table] || {}).icon || 'fa-box';
        return '<span class="thumb thumb-' + size + '"><i class="fa-solid ' + icon + '"></i>' +
            (image ? '<img src="' + esc(image) + '" alt="" loading="lazy" decoding="async" onerror="this.remove()">' : '') + '</span>';
    }

    /** "/weapons/ka-m" or a full URL -> "/weapons#ka-m" */
    function itemHref(url) {
        try {
            const p = new URL(url, location.origin).pathname.split('/').filter(Boolean);
            return '/' + p[0] + (p[1] ? '#' + p[1] : '');
        } catch (e) { return '/'; }
    }

    function sectionOf(item) {
        return SECTIONS[item.source] || null;
    }

    // ---------- Toasts ----------
    function toast(message, ok = true) {
        const box = $('#toasts');
        if (!box) return;
        const el = document.createElement('div');
        el.className = 'toast ' + (ok ? 'ok' : 'err');
        el.innerHTML = '<i class="fa-solid ' + (ok ? 'fa-circle-check' : 'fa-circle-exclamation') + '"></i><span>' + esc(message) + '</span>';
        box.appendChild(el);
        setTimeout(() => { el.style.opacity = '0'; el.style.transition = 'opacity .3s'; setTimeout(() => el.remove(), 300); }, 3500);
    }

    // ---------- Scrim / overlays ----------
    const openLayers = new Set();
    function syncScrim() { const s = $('#scrim'); if (s) s.hidden = openLayers.size === 0; }
    function openLayer(id) { const el = document.getElementById(id); if (!el) return; el.classList.add('open'); el.setAttribute('aria-hidden', 'false'); openLayers.add(id); syncScrim(); }
    function closeLayer(id) {
        const el = document.getElementById(id); if (!el) return;
        el.classList.remove('open'); el.setAttribute('aria-hidden', 'true'); openLayers.delete(id); syncScrim();
        if (id === 'drawer' && typeof window.DZ.onDrawerClose === 'function') window.DZ.onDrawerClose();
    }
    function closeAll() { [...openLayers].forEach(closeLayer); }

    // ---------- Theme ----------
    function initTheme() {
        $('#themeToggle')?.addEventListener('click', () => {
            const cur = document.documentElement.getAttribute('data-theme') ||
                (matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark');
            const next = cur === 'light' ? 'dark' : 'light';
            document.documentElement.setAttribute('data-theme', next);
            try { localStorage.setItem('dz-theme', next); } catch (e) { /* ignore */ }
        });
    }

    // ---------- Sidebar ----------
    function initSidebar() {
        $('#menuToggle')?.addEventListener('click', () => openLayer('sidebar'));
        loadStats().then(stats => {
            document.querySelectorAll('[data-count]').forEach(el => {
                const n = stats[el.getAttribute('data-count')];
                if (n) el.textContent = n;
            });
        });
    }

    let statsPromise = null;
    function loadStats() {
        if (!statsPromise) {
            statsPromise = fetch('/api/v1/wiki/stats').then(r => r.ok ? r.json() : {}).catch(() => ({}));
        }
        return statsPromise;
    }

    // ---------- Auth ----------
    function currentUser() {
        const row = document.cookie.split('; ').find(r => r.startsWith('dayzwiki_user_token='));
        const token = row && row.split('=')[1];
        if (!token) return null;
        try {
            const b64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
            const json = decodeURIComponent(atob(b64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
            return JSON.parse(json).sub || null;
        } catch (e) { return null; }
    }

    function openAuth(tab = 'signin') {
        const d = $('#authDialog'); if (!d) return;
        d.hidden = false;
        switchAuthTab(tab);
    }
    function closeAuth() { const d = $('#authDialog'); if (d) d.hidden = true; }
    function switchAuthTab(tab) {
        const tabs = document.querySelectorAll('[data-auth-tab]');
        tabs.forEach(b => { b.classList.toggle('active', b.dataset.authTab === tab); b.setAttribute('aria-selected', b.dataset.authTab === tab); });
        document.querySelectorAll('[data-auth-form]').forEach(f => { f.hidden = f.dataset.authForm !== tab; });
        moveTabInk();
        requestAnimationFrame(moveTabInk);
        const f = document.querySelector('[data-auth-form="' + tab + '"] input');
        if (f) setTimeout(() => f.focus(), 30);
    }
    // Slide the orange underline under the active tab (hidden on the "done" panel).
    function moveTabInk() {
        const ink = $('.auth-tabs-ink'), active = $('.auth-tab.active');
        if (!ink) return;
        ink.style.opacity = active ? '1' : '0';
        if (!active || !active.offsetWidth) return;
        const first = !ink.style.width;   // first placement: jump into place instead of sliding in from the left
        if (first) ink.style.transition = 'none';
        ink.style.width = active.offsetWidth + 'px';
        ink.style.transform = 'translateX(' + active.offsetLeft + 'px)';
        if (first) { void ink.offsetWidth; ink.style.transition = ''; }
    }
    function showAuthDone(title, text) {
        const done = $('[data-auth-form="done"]');
        done.querySelector('[data-done-title]').textContent = title;
        done.querySelector('[data-done-text]').textContent = text;
        switchAuthTab('done');
    }

    function requireFields(form) {
        let ok = true;
        form.querySelectorAll('input[required]').forEach(i => {
            const bad = !i.value.trim() || (i.type === 'email' && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(i.value.trim()));
            i.classList.toggle('invalid', bad);
            if (bad) ok = false;
        });
        if (!ok) toast(t('Please fill in the highlighted fields.'), false);
        return ok;
    }
    function markInvalid(input, message) {
        input.classList.add('invalid');
        input.focus();
        toast(message, false);
    }
    // Disable the submit button and show a spinner while a request is running.
    async function busy(form, fn) {
        const b = form.querySelector('button[type="submit"]');
        if (b.classList.contains('loading')) return;
        b.classList.add('loading'); b.disabled = true;
        try { await fn(); } finally { b.classList.remove('loading'); b.disabled = false; }
    }

    // 0-4 score shown as four bars under the password field.
    function passwordScore(pw) {
        if (!pw) return 0;
        if (pw.length < 6) return 1;
        let s = 1;
        if (pw.length >= 10) s++;
        if (/[a-zа-яё]/.test(pw) && /[A-ZА-ЯЁ]/.test(pw)) s++;
        if (/\d/.test(pw) && /[^\p{L}\d]/u.test(pw)) s++;
        else if (/\d/.test(pw) || /[^\p{L}\d]/u.test(pw)) s += pw.length >= 8 ? 1 : 0;
        return Math.min(s, 4);
    }
    function bindStrength(input, meter, label) {
        if (!input || !meter) return;
        const words = [label ? label.textContent : '', t('Too short'), t('Weak'), t('Good'), t('Strong')];
        input.addEventListener('input', () => {
            const s = input.value ? passwordScore(input.value) : 0;
            meter.dataset.meter = s;
            if (label) label.textContent = words[s];
        });
    }

    function initAuth() {
        const user = currentUser();
        const btn = $('#accountBtn'), menu = $('#accountMenu');
        if (!btn) return;
        if (user) {
            $('#accountLabel').textContent = user;
            $('#profileLink').href = '/@' + encodeURIComponent(user);
            btn.addEventListener('click', e => { e.stopPropagation(); menu.hidden = !menu.hidden; });
            document.addEventListener('click', e => { if (!menu.contains(e.target)) menu.hidden = true; });
            $('#logoutBtn').addEventListener('click', async () => {
                try { await fetch('/api/v1/auth/logout', { method: 'POST' }); } catch (e) { /* ignore */ }
                document.cookie = 'dayzwiki_user_token=; Max-Age=0; path=/';
                location.reload();
            });
        } else {
            btn.addEventListener('click', () => openAuth('signin'));
        }

        const dialog = $('#authDialog');
        if (!dialog) return;
        dialog.addEventListener('click', e => { if (e.target === dialog || e.target.closest('[data-close-dialog]')) closeAuth(); });
        document.addEventListener('keydown', e => { if (e.key === 'Escape' && !dialog.hidden) closeAuth(); });
        document.querySelectorAll('[data-auth-tab]').forEach(b => b.addEventListener('click', () => switchAuthTab(b.dataset.authTab)));
        dialog.querySelectorAll('[data-goto]').forEach(b => b.addEventListener('click', () => switchAuthTab(b.dataset.goto)));
        window.addEventListener('resize', moveTabInk);
        if (document.fonts) document.fonts.ready.then(moveTabInk);
        initPasswordToggles(dialog);
        dialog.querySelectorAll('input').forEach(i => i.addEventListener('input', () => i.classList.remove('invalid')));

        const signin = document.querySelector('[data-auth-form="signin"]');
        signin.addEventListener('submit', e => {
            e.preventDefault();
            if (!requireFields(signin)) return;
            const fd = new FormData(signin);
            busy(signin, async () => {
                try {
                    const r = await fetch('/api/v1/auth/signin', {
                        method: 'POST', headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ nameOrEmail: fd.get('nameOrEmail').trim(), password: fd.get('password'), remember: !!fd.get('remember') })
                    });
                    if (r.ok) { toast(t('Signed in.')); setTimeout(() => location.reload(), 400); return; }
                    if (r.status === 403) { toast(t(await r.text()), false); return; }
                    signin.querySelector('[name="password"]').classList.add('invalid');
                    toast(t('Wrong username/email or password.'), false);
                } catch (err) { toast(t('Could not reach the server. Try again.'), false); }
            });
        });

        const signup = document.querySelector('[data-auth-form="signup"]');
        bindStrength(signup.querySelector('[data-strength]'), signup.querySelector('.pw-meter'), signup.querySelector('[data-meter-label]'));
        signup.addEventListener('submit', e => {
            e.preventDefault();
            if (!requireFields(signup)) return;
            const fd = new FormData(signup);
            const name = fd.get('name').trim(), email = fd.get('email').trim();
            if (!/^[\p{L}\p{N}_.\-]{3,32}$/u.test(name)) { markInvalid(signup.querySelector('[name="name"]'), t('Username must be 3-32 characters: letters, numbers, _ . -')); return; }
            if (fd.get('password').length < 6) { markInvalid(signup.querySelector('[name="password"]'), t('Password must be at least 6 characters')); return; }
            if (fd.get('password') !== fd.get('password2')) { markInvalid(signup.querySelector('[name="password2"]'), t('Passwords do not match.')); return; }
            busy(signup, async () => {
                try {
                    const r = await fetch('/api/v1/auth/signup', {
                        method: 'POST', headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ name, email, password: fd.get('password') })
                    });
                    const text = await r.text();
                    if (r.ok) {
                        signup.reset();
                        signup.querySelector('.pw-meter').dataset.meter = 0;
                        showAuthDone(t('Check your inbox'), t('We sent a confirmation link to {0}. Open it to activate your account, then sign in.', email));
                        return;
                    }
                    const msg = text && text.length < 200 && text.charAt(0) !== '{' ? t(text) : t('Registration failed. Try again in a minute.');
                    if (/username/i.test(text)) signup.querySelector('[name="name"]').classList.add('invalid');
                    if (/email/i.test(text) && !/send/i.test(text)) signup.querySelector('[name="email"]').classList.add('invalid');
                    toast(msg, false);
                } catch (err) { toast(t('Could not reach the server. Try again.'), false); }
            });
        });

        const reset = document.querySelector('[data-auth-form="reset"]');
        reset.addEventListener('submit', e => {
            e.preventDefault();
            if (!requireFields(reset)) return;
            const email = new FormData(reset).get('email').trim();
            busy(reset, async () => {
                try {
                    const r = await fetch('/api/v1/auth/change/password?email=' + encodeURIComponent(email));
                    if (r.ok) { reset.reset(); showAuthDone(t('Check your inbox'), t('We sent a password reset link to {0}.', email)); }
                    else markInvalid(reset.querySelector('[name="email"]'), t('Could not send the reset email. Check the address and try again.'));
                } catch (err) { toast(t('Could not reach the server. Try again.'), false); }
            });
        });

        // Direct links to the dialog: ?auth=signin | signup | reset
        const params = new URLSearchParams(location.search);
        const wanted = params.get('auth');
        if (!user && ['signin', 'signup', 'reset'].indexOf(wanted) > -1) openAuth(wanted);

        // Coming back from the confirmation email: /?confirmed=1 (ok) or /?confirmed=0 (bad link).
        const confirmed = params.get('confirmed');
        if (confirmed !== null) {
            history.replaceState(null, '', location.pathname + location.hash);
            if (confirmed === '1') { toast(t('Email confirmed. You can sign in now.')); if (!user) openAuth('signin'); }
            else toast(t('This confirmation link is invalid or has already been used.'), false);
        }
        const emailChanged = params.get('emailChanged');
        if (emailChanged !== null) {
            history.replaceState(null, '', location.pathname + location.hash);
            if (emailChanged === '1') toast(t('Your email address has been changed.'));
            else toast(t('This confirmation link is invalid or has already been used.'), false);
        }
    }

    function initPasswordToggles(root) {
        root.querySelectorAll('.pw-toggle').forEach(b => b.addEventListener('click', () => {
            const i = b.previousElementSibling; const show = i.type === 'password';
            i.type = show ? 'text' : 'password';
            b.innerHTML = '<i class="fa-solid ' + (show ? 'fa-eye-slash' : 'fa-eye') + '"></i>';
            i.focus();
        }));
    }

    // ---------- Bookmarks ----------
    async function saveBookmark(item) {
        if (!currentUser()) { openAuth('signin'); toast(t('Sign in to save bookmarks.'), false); return false; }
        const sec = sectionOf(item);
        const desc = item.description || item.causes || '';
        try {
            const r = await fetch('/api/v1/bookmarks', {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    id: null, user: null, englishName: item.englishName,
                    description: desc.length > 240 ? desc.slice(0, 237) + '…' : desc,
                    section: item.type || (sec ? sec.title : item.source), source: item.source, url: item.urlName
                })
            });
            if (r.ok) { toast(t('{0} saved to bookmarks.', item.englishName)); return true; }
            toast(t('Already bookmarked, or could not save.'), false);
        } catch (e) { toast(t('Could not save the bookmark.'), false); }
        return false;
    }

    async function showBookmarks() {
        const list = $('#bookmarksList');
        openLayer('bookmarksPanel');
        if (!currentUser()) {
            list.innerHTML = '<div class="empty"><i class="fa-regular fa-bookmark"></i>' + esc(t('Sign in to keep a list of items you care about.')) + '<br><br><button class="btn btn-primary" id="bmSignin">' + esc(t('Sign in')) + '</button></div>';
            $('#bmSignin').addEventListener('click', () => { closeLayer('bookmarksPanel'); openAuth('signin'); });
            return;
        }
        list.innerHTML = '<div class="skeleton" style="height:60px"></div>';
        try {
            const r = await fetch('/api/v1/bookmarks');
            if (!r.ok) throw new Error();
            const items = await r.json();
            if (!items.length) { list.innerHTML = '<div class="empty"><i class="fa-regular fa-bookmark"></i>' + esc(t('No bookmarks yet. Open any item and press the bookmark icon.')) + '</div>'; return; }
            list.innerHTML = items.map(b =>
                '<div class="bm"><div class="bm-main" data-url="' + esc(b.url) + '"><div class="bm-name">' + esc(b.englishName) + '</div>' +
                '<div class="bm-desc">' + esc(b.section) + (b.description ? ' · ' + esc(b.description) : '') + '</div></div>' +
                '<button class="icon-btn" data-del="' + esc(b.id) + '" aria-label="' + esc(t('Remove bookmark')) + '"><i class="fa-solid fa-trash-can"></i></button></div>').join('');
            list.querySelectorAll('.bm-main').forEach(el => el.addEventListener('click', () => { location.href = itemHref(el.dataset.url); }));
            list.querySelectorAll('[data-del]').forEach(el => el.addEventListener('click', async () => {
                const ok = (await fetch('/api/v1/bookmarks/' + el.dataset.del, { method: 'DELETE' })).ok;
                toast(ok ? t('Bookmark removed.') : t('Could not remove bookmark.'), ok);
                showBookmarks();
            }));
        } catch (e) { list.innerHTML = '<div class="empty">' + esc(t('Could not load bookmarks.')) + '</div>'; }
    }

    // ---------- Item drawer ----------
    let drawerItem = null;
    function openItem(item) {
        drawerItem = item;
        const sec = sectionOf(item);
        $('#drawerKicker').textContent = sec ? sec.title : '';
        $('#drawerTitle').textContent = item.englishName;
        $('#drawerBody').innerHTML = '<div class="drawer-media">' + thumb(item.image, item.source, 'lg') + '</div>' + window.renderItemDetails(item);
        $('#drawerBookmark').classList.remove('saved');
        $('#drawerBookmark').innerHTML = '<i class="fa-regular fa-bookmark"></i>';
        openLayer('drawer');
        $('#drawerBody').scrollTop = 0;
    }
    async function openItemUrl(url) {
        try {
            const r = await fetch(url, { headers: { Accept: 'application/json' } });
            if (!r.ok) throw new Error();
            openItem(await r.json());
        } catch (e) { toast(t('Could not load that entry.'), false); }
    }
    function initDrawer() {
        $('#drawerClose')?.addEventListener('click', () => closeLayer('drawer'));
        $('#drawerBookmark')?.addEventListener('click', async () => {
            if (drawerItem && await saveBookmark(drawerItem)) {
                $('#drawerBookmark').classList.add('saved');
                $('#drawerBookmark').innerHTML = '<i class="fa-solid fa-bookmark"></i>';
            }
        });
        $('#drawerLink')?.addEventListener('click', async () => {
            if (!drawerItem) return;
            const link = location.origin + itemHref(drawerItem.urlName);
            try { await navigator.clipboard.writeText(link); toast(t('Link copied.')); } catch (e) { toast(link); }
        });
        $('#bookmarksBtn')?.addEventListener('click', showBookmarks);
        document.querySelectorAll('[data-close]').forEach(b => b.addEventListener('click', () => closeLayer(b.dataset.close)));
        $('#scrim')?.addEventListener('click', closeAll);
        document.addEventListener('keydown', e => {
            if (e.key === 'Escape') { closeAll(); closeAuth(); }
        });
    }

    // ---------- Global search ----------
    function iconFor(table) { return (SECTIONS[table] || {}).icon || 'fa-file'; }
    function titleFor(table) { return (SECTIONS[table] || {}).title || table; }

    async function searchApi(q, size) {
        const r = await fetch('/api/v1/search', {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ search: { value: q, exact: false }, page: 0, size: size, orders: [], type: '' })
        });
        if (!r.ok) throw new Error('search failed');
        return r.json();
    }

    function initGlobalSearch() {
        const input = $('#globalSearchInput'), pop = $('#searchPop');
        if (!input) return;
        let timer = null, seq = 0, rows = [], focus = -1;

        function close() { pop.hidden = true; focus = -1; }
        function setFocus(i) {
            rows.forEach((r, k) => r.classList.toggle('focus', k === i));
            focus = i;
            if (rows[i]) rows[i].scrollIntoView({ block: 'nearest' });
        }
        async function run() {
            const q = input.value.trim();
            if (q.length < 2) { close(); return; }
            const my = ++seq;
            try {
                const data = await searchApi(q, 8);
                if (my !== seq) return;
                if (!data.items.length) {
                    pop.innerHTML = '<div class="search-empty">' + esc(t('Nothing matches “{0}”.', q)) + '</div>';
                } else {
                    pop.innerHTML = data.items.map(it =>
                        '<button class="search-row" data-href="' + esc(itemHref(it.url)) + '">' + thumb(it.image, it.source.sourceTable, 'sm') +
                        '<span><span class="sr-name">' + esc(it.englishName) + '</span><br><span class="sr-meta">' + esc(titleFor(it.source.sourceTable)) + ' · ' + esc(t(it.section)) + '</span></span></button>').join('') +
                        '<button class="search-row all" data-href="/search?q=' + encodeURIComponent(q) + '">' + esc(t('See all {0} results →', data.total)) + '</button>';
                }
                rows = [...pop.querySelectorAll('.search-row')];
                rows.forEach(r => r.addEventListener('click', () => { location.href = r.dataset.href; }));
                focus = -1;
                pop.hidden = false;
            } catch (e) { /* ignore transient errors */ }
        }

        input.addEventListener('input', () => { clearTimeout(timer); timer = setTimeout(run, 160); });
        input.addEventListener('focus', () => { if (input.value.trim().length >= 2) run(); });
        input.addEventListener('keydown', e => {
            if (e.key === 'ArrowDown') { e.preventDefault(); setFocus(Math.min(focus + 1, rows.length - 1)); }
            else if (e.key === 'ArrowUp') { e.preventDefault(); setFocus(Math.max(focus - 1, 0)); }
            else if (e.key === 'Enter') {
                e.preventDefault();
                if (rows[focus]) location.href = rows[focus].dataset.href;
                else if (input.value.trim()) location.href = '/search?q=' + encodeURIComponent(input.value.trim());
            } else if (e.key === 'Escape') { close(); input.blur(); }
        });
        document.addEventListener('click', e => { if (!$('#globalSearch').contains(e.target)) close(); });
        document.addEventListener('keydown', e => {
            if (e.key === '/' && !/input|textarea|select/i.test(document.activeElement.tagName)) { e.preventDefault(); input.focus(); }
        });
    }

    // ---------- Public API ----------
    window.DZ = {
        SECTIONS, BY_PATH, esc, slug, t, lang: I18N.lang, plural: I18N.plural, present, kg, tierInfo, mapDots, itemHref, toast, thumb,
        openItem, openItemUrl, openLayer, closeLayer, openAuth, searchApi, loadStats, iconFor, titleFor,
        bindStrength, initPasswordToggles, busy, markInvalid, currentUser,
        onDrawerClose: null
    };

    document.addEventListener('DOMContentLoaded', () => {
        initTheme();
        initSidebar();
        initAuth();
        initDrawer();
        initGlobalSearch();
    });
})();
