/*
 * Maps overview (/maps) and map detail (/maps/{id}).
 * Content: /data/maps.json. Live loot numbers: /api/v1/wiki/maps/{name}.
 */
(function () {
    'use strict';
    const DZ = window.DZ;
    const esc = DZ.esc, t = DZ.t;
    const $ = id => document.getElementById(id);

    const loadMaps = () => fetch(DZ.lang === 'ru' ? '/data/maps.ru.json' : '/data/maps.json').then(r => r.json());
    const loadLoot = name => fetch('/api/v1/wiki/maps/' + encodeURIComponent(name)).then(r => r.ok ? r.json() : null).catch(() => null);
    // Facts are in the same order in every map (and language): Area, Released, Access, Setting, Inspired by, Climate, Mission folder.
    const FACT = { Area: 0, Released: 1, Access: 2, Climate: 5 };
    const fact = (m, key) => (m.facts[FACT[key]] || [])[1] || '';
    const title = m => m.title || m.name;
    const totalVehicles = m => m.vehicles.reduce((n, v) => n + v[1], 0);

    // ---------- Overview page ----------
    async function overview() {
        const data = await loadMaps();
        const ids = data.order;
        const loot = await Promise.all(ids.map(id => loadLoot(data.maps[id].name)));

        $('mapGrid').innerHTML = ids.map((id, i) => {
            const m = data.maps[id];
            const total = loot[i] ? Object.values(loot[i].counts).reduce((a, b) => a + b, 0) : null;
            return '<a class="map-card" href="/maps/' + id + '" style="--map-accent:' + esc(m.accent) + '">' +
                '<img class="map-card-img" src="' + esc(m.cover) + '" alt="" loading="lazy">' +
                (m.isNew ? '<span class="badge tier t4 map-new">' + esc(t('NEW · 1.30')) + '</span>' : '') +
                '<div class="map-card-body">' +
                '<div class="eyebrow">' + esc(m.biome) + '</div>' +
                '<h2>' + esc(title(m)) + '</h2>' +
                '<p>' + esc(m.tagline) + '</p>' +
                '<div class="map-card-stats">' +
                '<span><b>' + esc(m.areaShort || fact(m, 'Area')) + '</b>' + esc(t('area')) + '</span>' +
                (total !== null ? '<span><b>' + total + '</b>' + esc(t('items spawn')) + '</span>' : '') +
                (loot[i] ? '<span><b>' + loot[i].exclusive.length + '</b>' + esc(t('exclusive')) + '</span>' : '') +
                '<span><b>' + esc((fact(m, 'Released').match(/\d{4}/) || [''])[0]) + '</b>' + esc(m.baseGame ? t('base game') : t('expansion')) + '</span>' +
                '</div><span class="map-card-go">' + esc(t('Explore')) + ' <i class="fa-solid fa-arrow-right"></i></span></div></a>';
        }).join('');

        const rows = [
            [t('Area'), id => fact(data.maps[id], 'Area')],
            [t('Released'), id => fact(data.maps[id], 'Released')],
            [t('Access'), id => fact(data.maps[id], 'Access')],
            [t('Climate'), id => fact(data.maps[id], 'Climate')],
            [t('Items that spawn'), (id, i) => loot[i] ? Object.values(loot[i].counts).reduce((a, b) => a + b, 0) : '—'],
            [t('Exclusive items'), (id, i) => loot[i] ? loot[i].exclusive.length : '—'],
            [t('Max vehicles at once'), id => totalVehicles(data.maps[id])]
        ];
        $('compare').innerHTML = '<thead><tr><th></th>' + ids.map(id => '<th><a href="/maps/' + id + '">' + esc(title(data.maps[id])) + '</a></th>').join('') + '</tr></thead><tbody>' +
            rows.map(([label, fn]) => '<tr class="no-hover"><td class="muted">' + esc(label) + '</td>' + ids.map((id, i) => '<td>' + esc(fn(id, i)) + '</td>').join('') + '</tr>').join('') + '</tbody>';
    }

    // ---------- Detail page ----------
    async function detail(id) {
        const data = await loadMaps();
        const m = data.maps[id];
        if (!m) return;
        document.documentElement.style.setProperty('--map-accent', m.accent);

        $('mapHero').style.backgroundImage = 'linear-gradient(100deg, rgba(10,12,15,.95) 25%, rgba(10,12,15,.55) 65%, rgba(10,12,15,.25)), url("' + m.cover + '")';
        $('mapBiome').innerHTML = '<i class="fa-solid fa-earth-europe"></i> ' + esc(m.biome) + (m.isNew ? ' · <b style="color:var(--accent)">' + esc(t('NEW')) + '</b>' : '');
        $('mapTagline').textContent = m.tagline;
        $('mapChips').innerHTML = ['Area', 'Released', 'Access'].map(k => '<span class="hero-tag">' + esc(t(k)) + ': <b>' + esc(fact(m, k)) + '</b></span>').join('');

        $('mapIntro').textContent = m.intro;
        $('mapHistory').innerHTML = m.history.map(p => '<p>' + esc(p) + '</p>').join('');
        $('mapFeatures').innerHTML = m.features.map(([icon, title, text]) =>
            '<div class="feature"><span class="feature-icon"><i class="fa-solid ' + esc(icon) + '"></i></span><h3>' + esc(title) + '</h3><p>' + esc(text) + '</p></div>').join('');

        $('mapGallery').innerHTML = m.gallery.map(([src, cap], i) =>
            '<button class="shot' + (i === 0 && m.gallery.length % 2 === 1 ? ' wide' : '') + '" data-src="' + esc(src) + '" data-cap="' + esc(cap) + '"><img src="' + esc(src) + '" alt="' + esc(cap) + '" loading="lazy"><span>' + esc(cap) + '</span></button>').join('');

        if (m.map) {
            $('mapImage').src = m.map;
            $('mapImage').alt = m.mapCaption;
            $('mapCaption').textContent = m.mapCaption;
            $('mapImageBtn').dataset.src = m.map;
            $('mapImageBtn').dataset.cap = m.mapCaption;
        } else {
            $('mapImagePanel').hidden = true;
        }

        if (m.locations.length) {
            $('mapLocations').innerHTML = m.locations.map(([n, kind]) => '<div class="loc"><i class="fa-solid fa-location-dot"></i><div><b>' + esc(n) + '</b><span>' + esc(kind) + '</span></div></div>').join('');
        } else {
            $('locationsSection').hidden = true;
        }

        $('mapFacts').innerHTML = m.facts.map(([k, v]) => '<tr><th>' + esc(k) + '</th><td>' + esc(v) + '</td></tr>').join('');
        $('mapVehicles').innerHTML = m.vehicles.map(([k, v]) => '<tr><th>' + esc(k) + '</th><td>' + esc(v) + '</td></tr>').join('') +
            '<tr><th><b>' + esc(t('Total')) + '</b></th><td><b>' + totalVehicles(m) + '</b></td></tr>';
        $('mapWildlife').textContent = m.wildlife;

        // pager
        const ids = data.order, i = ids.indexOf(id);
        const prev = ids[(i - 1 + ids.length) % ids.length], next = ids[(i + 1) % ids.length];
        $('mapPager').innerHTML =
            '<a href="/maps/' + prev + '"><span class="muted small"><i class="fa-solid fa-arrow-left"></i> ' + esc(t('Previous')) + '</span><b>' + esc(title(data.maps[prev])) + '</b></a>' +
            '<a href="/maps/' + next + '" style="text-align:right"><span class="muted small">' + esc(t('Next')) + ' <i class="fa-solid fa-arrow-right"></i></span><b>' + esc(title(data.maps[next])) + '</b></a>';

        // live loot data
        const loot = await loadLoot(m.name);
        $('exclusiveTitle').textContent = t('Only on {0}', title(m));
        if (!loot) {
            $('mapLoot').innerHTML = '<p class="muted">' + esc(t('Loot data unavailable.')) + '</p>';
            $('mapExclusive').innerHTML = '';
            return;
        }
        const order = ['weapons', 'ammunition', 'modules', 'explosives', 'armors', 'foods', 'medicaments', 'cars'];
        $('mapLoot').innerHTML = order.filter(k => loot.counts[k]).map(k => {
            const s = DZ.SECTIONS[k];
            return '<a class="loot-row" href="' + s.path + '?map=' + encodeURIComponent(m.name) + '"><i class="fa-solid ' + s.icon + '"></i><span>' + esc(s.title) + '</span><b>' + loot.counts[k] + '</b></a>';
        }).join('');
        $('mapExclusive').innerHTML = loot.exclusive.length ? loot.exclusive.map(it =>
            '<a class="news-item" href="' + esc(DZ.itemHref(it.url)) + '">' + DZ.thumb(it.image, it.source, 'sm') +
            '<div><b>' + esc(it.englishName) + '</b><span>' + esc(DZ.titleFor(it.source)) + ' · ' + esc(DZ.t(it.section)) + '</span></div></a>').join('')
            : '<p class="muted">' + esc(t('Everything that spawns here also spawns on at least one other map.')) + '</p>';
    }

    // ---------- Lightbox ----------
    function initLightbox() {
        const box = $('lightbox');
        if (!box) return;
        document.addEventListener('click', e => {
            const t = e.target.closest('[data-src]');
            if (t && (t.classList.contains('shot') || t.id === 'mapImageBtn')) {
                $('lightboxImg').src = t.dataset.src;
                $('lightboxCaption').textContent = t.dataset.cap || '';
                box.hidden = false;
            } else if (!box.hidden && (e.target === box || e.target.closest('.lightbox-close'))) {
                box.hidden = true;
            }
        });
        document.addEventListener('keydown', e => { if (e.key === 'Escape') box.hidden = true; });
    }

    const page = document.getElementById('mapPage');
    if (page) { detail(page.dataset.map); initLightbox(); }
    else if (document.getElementById('mapsPage')) overview();
})();
