/*
 * Shooting range (/range): dress a survivor, pick a gun, click the body to fire.
 * Item data: /data/range.json (built by db/tools/gen_range.py from the DayZ wiki's config dumps).
 *
 * The model, per pellet/bullet:
 *   speed factor  f = e^(airFriction * distance)                (velocity left at the target, 0..1)
 *   armor         p = product of the projectile coefficients of every worn, non-ruined item over the zone
 *   global health -= ammo.health * f * p.health * zone.health
 *   shock         -= ammo.shock  * f * p.shock  * zone.shock
 *   blood         -= ammo.blood  * f * p.blood                  (instant loss)
 *   bleeding wound with probability p.blood (bullets always cut unless armor stops it)
 *   worn items over the zone lose hit points, outer layer first: each layer takes what got through the one above it
 *   the zone itself loses ammo.health * f * p.health (legs/feet at 0 = broken legs)
 */
(function () {
    'use strict';
    const DZ = window.DZ;
    const esc = DZ.esc, t = DZ.t;
    const $ = id => document.getElementById(id);
    const SVGNS = 'http://www.w3.org/2000/svg';

    // ---------- Game rules ----------
    // Zone multipliers are estimates: Bohemia does not publish them. Head damage was doubled in 1.22.
    const ZONES = {
        head:  { label: t('Head'),       health: 2,    shock: 2,    slots: ['head', 'face'], bleed: 360 },
        torso: { label: t('Torso'),      health: 1,    shock: 0.75, slots: ['vest', 'top'],  bleed: 360 },
        rarm:  { group: 'Arms',  label: t('Right arm'),  health: 0.35, shock: 0.3,  slots: ['top'],          bleed: 216 },
        larm:  { label: t('Left arm'),   health: 0.35, shock: 0.3,  slots: ['top'],          bleed: 216 },
        rhand: { group: 'Hands',  label: t('Right hand'), health: 0.2,  shock: 0.15, slots: ['gloves'],       bleed: 36 },
        lhand: { label: t('Left hand'),  health: 0.2,  shock: 0.15, slots: ['gloves'],       bleed: 36 },
        rleg:  { group: 'Legs',  label: t('Right leg'),  health: 0.35, shock: 0.3,  slots: ['legs'],         bleed: 216, leg: true },
        lleg:  { label: t('Left leg'),   health: 0.35, shock: 0.3,  slots: ['legs'],         bleed: 216, leg: true },
        rfoot: { group: 'Feet',  label: t('Right foot'), health: 0.2,  shock: 0.15, slots: ['feet'],         bleed: 36,  leg: true },
        lfoot: { label: t('Left foot'),  health: 0.2,  shock: 0.15, slots: ['feet'],         bleed: 36,  leg: true }
    };
    const SLOTS = [
        ['head', t('Headgear'), 'fa-helmet-safety'], ['face', t('Mask'), 'fa-masks-theater'], ['vest', t('Vest'), 'fa-vest'],
        ['top', t('Top'), 'fa-shirt'], ['gloves', t('Gloves'), 'fa-mitten'], ['legs', t('Pants'), 'fa-person'], ['feet', t('Boots'), 'fa-shoe-prints']
    ];
    const MAX = { health: 100, blood: 5000, shock: 100 };
    const UNCONSCIOUS_SHOCK = 25, WAKE_SHOCK = 50;      // wiki: out below ~25, wake above 50
    const UNCONSCIOUS_BLOOD = 3000, DEAD_BLOOD = 2500;
    const CLOT_SECONDS = 300;                            // wounds clot on their own after 5 minutes
    const SHOCK_REGEN = 1;                               // per second
    const BLOOD_BAG = 500, MAX_WOUNDS = 10;
    const PELLET_FULL_SPREAD = 15;                       // all buckshot pellets land within ~15 m (estimate)

    const CONDITIONS = [[0.7, t('Pristine'), 'good'], [0.5, t('Worn'), 'good'], [0.3, t('Damaged'), 'warn'], [0.0001, t('Badly damaged'), 'bad'], [-1, t('Ruined'), 'ruined']];
    const condition = (hp, max) => CONDITIONS.find(c => hp / max >= c[0]);

    let D = null;            // range.json
    let S = null;            // survivor state
    const worn = {};         // slot -> {item, hp}
    let shots = 0, playing = null;

    // ---------- Helpers ----------
    const weapon = () => D.weapons.find(w => w.name === $('weaponSel').value);
    const ammo = () => D.ammo[$('ammoSel').value];
    const dist = () => +$('distance').value;
    const speedFactor = (a, d) => a.friction < 0 ? Math.exp(a.friction * d) : 1;
    const fmt = (n, dp = 0) => (Math.round(n * 10 ** dp) / 10 ** dp).toLocaleString(DZ.lang === 'ru' ? 'ru-RU' : 'en-US');
    const pct = x => Math.round(x * 100) + '%';
    const img = (src, cls) => src ? '<img class="' + cls + '" src="' + esc(src) + '" alt="" loading="lazy">' : '<span class="' + cls + ' noimg"><i class="fa-solid fa-image"></i></span>';
    const mmss = t => Math.floor(t / 60) + ':' + String(Math.floor(t % 60)).padStart(2, '0');

    function freshSurvivor() {
        const zones = {};
        Object.keys(ZONES).forEach(z => { zones[z] = 100; });
        return { health: 100, blood: 5000, shock: 100, wounds: [], zones, time: 0, state: 'alive' };
    }

    function protection(zoneKey) {
        const p = { h: 1, b: 1, s: 1, items: [] };
        ZONES[zoneKey].slots.forEach(slot => {
            const w = worn[slot];
            if (!w) return;
            p.items.push(slot);
            if (w.hp <= 0) return; // ruined gear stops protecting
            p.h *= w.item.h; p.b *= w.item.b; p.s *= w.item.s;
        });
        return p;
    }

    function pelletsLanding(a, d) {
        if (a.pellets <= 1) return 1;
        return Math.max(1, Math.round(a.pellets * Math.min(1, PELLET_FULL_SPREAD / d)));
    }

    // Damage one hit would do right now, without randomness or side effects.
    function preview(zoneKey) {
        const a = ammo(), z = ZONES[zoneKey], d = dist();
        const f = speedFactor(a, d), n = pelletsLanding(a, d), p = protection(zoneKey);
        return {
            f, n, p,
            health: a.health * f * p.h * z.health * n,
            shock: a.shock * f * p.s * z.shock * n,
            blood: a.blood * f * p.b * n,
            bleedChance: a.blood > 0 ? 1 - Math.pow(1 - p.b, n) : 0
        };
    }

    // ---------- Firing ----------
    function fire(zoneKey, pt) {
        const a = ammo(), w = weapon(), z = ZONES[zoneKey], d = dist();
        const f = speedFactor(a, d), n = pelletsLanding(a, d);
        const before = { health: S.health, blood: S.blood, shock: S.shock };
        const dead = S.state === 'dead';
        const gearBefore = {};
        z.slots.forEach(s => { if (worn[s]) gearBefore[s] = worn[s].hp; });
        let protTxt = '', newWound = false, bleedStopped = false;

        for (let i = 0; i < n; i++) {
            const p = protection(zoneKey); // recomputed per pellet: gear can break mid-volley
            if (i === 0) protTxt = describeProtection(p);
            if (!dead) {
                S.health -= a.health * f * p.h * z.health;
                S.shock -= a.shock * f * p.s * z.shock;
                S.blood -= a.blood * f * p.b;
                S.zones[zoneKey] = Math.max(0, S.zones[zoneKey] - a.health * f * p.h);
                if (a.blood > 0) {
                    if (Math.random() < p.b) newWound = addWound(zoneKey) || newWound;
                    else bleedStopped = true;
                }
            }
            // gear wear: outer layer first, each inner layer only takes what got through
            let through = a.health * f;
            p.items.forEach(s => {
                const g = worn[s];
                const intact = g.hp > 0;
                g.hp = Math.max(0, g.hp - through);
                if (intact) through *= g.item.h;
            });
        }
        clamp();
        updateState();
        shots++;

        // log entry
        const lines = [];
        if (dead) lines.push('<span class="muted">' + esc(t('Already dead: only the gear takes damage.')) + '</span>');
        else lines.push(stat(t('Health'), S.health - before.health) + stat(t('Shock'), S.shock - before.shock) + stat(t('Blood'), S.blood - before.blood, ' ' + t('ml')));
        if (protTxt) lines.push('<span class="muted">' + protTxt + '</span>');
        Object.keys(gearBefore).forEach(s => {
            const g = worn[s], c0 = condition(gearBefore[s], g.item.hp), c1 = condition(g.hp, g.item.hp);
            lines.push('<span class="gear-line">' + esc(g.item.name) + ': ' + fmt(gearBefore[s]) + ' → <b>' + fmt(g.hp) + '</b>/' + fmt(g.item.hp) +
                ' <span class="cond ' + c1[2] + '">' + c1[1] + '</span>' + (c0 !== c1 && c1[2] === 'ruined' ? ' <b class="bad">' + esc(t('destroyed')) + '</b>' : '') + '</span>');
        });
        if (a.blood > 0 && !dead) lines.push(newWound ? '<span class="bad"><i class="fa-solid fa-droplet"></i> ' + esc(t('New bleeding wound ({0} ml/min)', z.bleed)) + '</span>'
            : bleedStopped ? '<span class="good"><i class="fa-solid fa-shield"></i> ' + esc(t('Armor stopped the bleeding')) + '</span>'
                : '<span class="muted">' + esc(t('Wound already bleeding here')) + '</span>');
        if (z.leg && S.zones[zoneKey] <= 0 && !S.brokenLogged) { lines.push('<span class="bad"><i class="fa-solid fa-bone"></i> ' + esc(t('Legs broken')) + '</span>'); S.brokenLogged = true; }
        if (S.state !== 'alive' && !dead) lines.push('<span class="bad state-line">' + esc(S.state === 'dead' ? t('☠ Survivor is dead') : t('Survivor is unconscious')) + '</span>');

        const li = document.createElement('li');
        li.innerHTML = '<div class="log-head"><b>#' + shots + ' ' + esc(z.label) + '</b><span class="muted small">' + esc(w.name) + ' · ' + esc(a.name) +
            (n > 1 ? ' · ' + esc(t('{0} pellets', n)) : '') + ' · ' + d + ' ' + t('m') + '</span></div>' + lines.map(l => '<div>' + l + '</div>').join('');
        $('log').prepend(li);

        addHitMarker(pt, shots);
        if (!playing && S.state !== 'dead' && (S.wounds.length || S.shock < MAX.shock)) setPlaying(true);
        render();
    }

    const stat = (k, delta, unit = '') => '<span class="delta' + (delta < 0 ? ' neg' : '') + '">' + k + ' <b>' + (delta < 0 ? '−' : '') + fmt(Math.abs(delta), Math.abs(delta) < 10 && delta ? 1 : 0) + unit + '</b></span>';

    function describeProtection(p) {
        if (!p.items.length) return esc(t('No clothing over this part.'));
        const parts = p.items.map(s => worn[s].hp <= 0 ? esc(t('{0} (ruined, no protection)', worn[s].item.name)) : esc(worn[s].item.name));
        if (p.h === 1 && p.b === 1 && p.s === 1) return t('Hit through {0} (no ballistic protection).', parts.join(' + '));
        return t('Armor: {0} blocked {1} health, {2} shock, {3} of bleeding.', parts.join(' + '), pct(1 - p.h), pct(1 - p.s), pct(1 - p.b));
    }

    function addWound(zoneKey) {
        const existing = S.wounds.find(w => w.zone === zoneKey);
        if (existing) { existing.left = CLOT_SECONDS; return false; }
        if (S.wounds.length >= MAX_WOUNDS) return false;
        S.wounds.push({ zone: zoneKey, rate: ZONES[zoneKey].bleed / 60, left: CLOT_SECONDS });
        return true;
    }

    function clamp() {
        S.health = Math.max(0, Math.min(MAX.health, S.health));
        S.blood = Math.max(0, Math.min(MAX.blood, S.blood));
        S.shock = Math.max(0, Math.min(MAX.shock, S.shock));
    }

    function updateState() {
        if (S.state === 'dead') return;
        if (S.health <= 0 || S.blood < DEAD_BLOOD) { S.state = 'dead'; S.wounds = []; setPlaying(false); return; }
        if (S.shock < UNCONSCIOUS_SHOCK || S.blood < UNCONSCIOUS_BLOOD) S.state = 'unconscious';
        else if (S.state === 'unconscious' && S.shock >= WAKE_SHOCK && S.blood >= UNCONSCIOUS_BLOOD) S.state = 'alive';
    }

    const brokenLegs = () => Object.keys(ZONES).some(z => ZONES[z].leg && S.zones[z] <= 0);

    // ---------- Time ----------
    function tick(dt) {
        if (S.state === 'dead') return setPlaying(false);
        S.time += dt;
        S.wounds.forEach(w => { const t = Math.min(dt, w.left); S.blood -= w.rate * t; w.left -= dt; });
        S.wounds = S.wounds.filter(w => w.left > 0);
        S.shock += SHOCK_REGEN * dt;
        clamp();
        const was = S.state;
        updateState();
        if (was !== S.state) logEvent(S.state === 'dead' ? t('☠ Bled out at {0}', mmss(S.time)) : S.state === 'alive' ? t('Woke up at {0}', mmss(S.time)) : t('Passed out from blood loss at {0}', mmss(S.time)), S.state === 'alive' ? 'good' : 'bad');
        if (!S.wounds.length && S.shock >= MAX.shock && S.state === 'alive') setPlaying(false);
        render();
    }

    function setPlaying(on) {
        if (on && !playing) {
            playing = setInterval(() => tick(0.25 * +$('speedSel').value), 250);
        } else if (!on && playing) {
            clearInterval(playing); playing = null;
        }
        $('playBtn').innerHTML = playing ? '<i class="fa-solid fa-pause"></i> ' + esc(t('Pause')) : '<i class="fa-solid fa-play"></i> ' + esc(t('Let time pass'));
    }

    function logEvent(text, cls) {
        const li = document.createElement('li');
        li.className = 'log-event ' + (cls || '');
        li.textContent = text;
        $('log').prepend(li);
    }

    // ---------- Rendering ----------
    function healthStage(h) { return t(h <= 0 ? 'Dead' : h < 40 ? 'Seriously injured' : h < 60 ? 'Moderately injured' : h < 80 ? 'Slightly injured' : 'Healthy'); }
    function bloodStage(b) { return t(b < DEAD_BLOOD ? 'Fatal' : b < UNCONSCIOUS_BLOOD ? 'Critical, unconscious' : b < 3500 ? 'Very low' : b < 4000 ? 'Low' : b < 4500 ? 'Slightly low' : 'Normal'); }
    function shockStage(s) { return t(s < UNCONSCIOUS_SHOCK ? 'Knocked out' : s < WAKE_SHOCK ? 'Groggy' : s < 100 ? 'Shaken' : 'Steady'); }

    function meter(label, icon, val, max, stage, cls) {
        return '<div class="meter ' + cls + '"><div class="meter-top"><span><i class="fa-solid ' + icon + '"></i> ' + label + '</span><b>' + fmt(val) + '<small>/' + fmt(max) + '</small></b></div>' +
            '<div class="meter-bar"><i style="width:' + (val / max * 100) + '%"></i></div><span class="meter-stage">' + stage + '</span></div>';
    }

    function render() {
        const bleedRate = S.wounds.reduce((n, w) => n + w.rate * 60, 0);
        $('meters').innerHTML =
            meter(t('Health'), 'fa-heart-pulse', S.health, MAX.health, healthStage(S.health), 'm-health') +
            meter(t('Blood'), 'fa-droplet', S.blood, MAX.blood, bloodStage(S.blood) + (bleedRate ? ' · ' + t('losing {0} ml/min', fmt(bleedRate)) : ''), 'm-blood') +
            meter(t('Shock'), 'fa-bolt', S.shock, MAX.shock, shockStage(S.shock), 'm-shock');
        const life = $('lifeState');
        life.textContent = t(S.state === 'dead' ? 'Dead' : S.state === 'unconscious' ? 'Unconscious' : brokenLegs() ? 'Alive · broken legs' : 'Alive');
        life.className = 'life ' + S.state;
        $('clock').textContent = mmss(S.time);
        $('shotCount').textContent = shots + ' ' + DZ.plural(shots, t('shot'), 'выстрела', t('shots'));

        const banner = $('stateBanner');
        banner.hidden = S.state === 'alive';
        banner.className = 'state-banner ' + S.state;
        banner.innerHTML = S.state === 'dead' ? '<i class="fa-solid fa-skull"></i> ' + esc(t('Dead')) : '<i class="fa-solid fa-bed"></i> ' + esc(t('Unconscious'));

        // wounds and limbs
        const wounds = S.wounds.length ? S.wounds.map(w => '<div class="wound"><i class="fa-solid fa-droplet"></i><span>' + esc(ZONES[w.zone].label) + '</span>' +
            '<span class="muted small">' + esc(t('{0} ml/min · clots in {1}', fmt(w.rate * 60), mmss(w.left))) + '</span></div>').join('')
            : '<p class="muted small" style="margin:0 0 10px">' + esc(t('No bleeding.')) + '</p>';
        const limbs = Object.keys(ZONES).filter(z => z !== 'head' && z !== 'torso').map(z => {
            const v = S.zones[z];
            return '<div class="limb' + (v <= 0 ? ' broken' : '') + '"><span>' + esc(ZONES[z].label) + '</span><i><b style="width:' + v + '%"></b></i>' +
                '<em>' + (v <= 0 ? t(ZONES[z].leg ? 'Broken' : 'Wrecked') : fmt(v)) + '</em></div>';
        }).join('');
        $('wounds').innerHTML = wounds + '<div class="limbs">' + limbs + '</div>';

        // zone colouring on the figure
        document.querySelectorAll('#zones path').forEach(p => {
            const z = p.dataset.zone;
            p.classList.toggle('hurt', S.zones[z] < 100 && S.zones[z] > 0);
            p.classList.toggle('wrecked', S.zones[z] <= 0);
            p.classList.toggle('bleeding', S.wounds.some(w => w.zone === z));
        });
        $('figure').classList.toggle('is-dead', S.state === 'dead');
        renderSlots(false);
        drawGear();
    }

    function renderSlots(rebuild) {
        SLOTS.forEach(([slot]) => {
            const row = document.querySelector('.slot[data-slot="' + slot + '"]');
            if (!row) return;
            const w = worn[slot];
            row.querySelector('.slot-pic').innerHTML = w ? img(w.item.image, 'slot-img') : '<i class="fa-solid ' + SLOTS.find(s => s[0] === slot)[2] + '"></i>';
            const info = row.querySelector('.slot-info');
            if (!w) { info.innerHTML = '<span class="muted small">' + esc(t('Nothing worn')) + '</span>'; return; }
            const c = condition(w.hp, w.item.hp);
            const armored = w.item.h < 1 || w.item.b < 1 || w.item.s < 1;
            info.innerHTML = '<div class="cond-bar ' + c[2] + '"><i style="width:' + (w.hp / w.item.hp * 100) + '%"></i></div>' +
                '<span class="small"><span class="cond ' + c[2] + '">' + c[1] + '</span> ' + fmt(w.hp) + '/' + fmt(w.item.hp) + '</span>' +
                (armored ? '<span class="small prot" title="' + esc(t('Projectile protection')) + '">−' + pct(1 - w.item.h) + ' ' + t('health') + ' · −' + pct(1 - w.item.s) + ' ' + t('shock') + ' · ' + (w.item.b === 0 ? t('no bleeding') : '−' + pct(1 - w.item.b) + ' ' + t('bleeding')) + '</span>'
                    : '<span class="small muted">' + esc(t('No ballistic protection')) + '</span>');
        });
    }

    // ---------- Figure drawing ----------
    const zonePath = z => document.querySelector('#zones path[data-zone="' + z + '"]').getAttribute('d');
    const PALETTE = ['#4f5d3f', '#3e4a5c', '#6b5640', '#5a3d3d', '#3f5552', '#5d5a3a', '#474747', '#2f3b4f'];
    const colorFor = name => PALETTE[[...name].reduce((n, ch) => n + ch.charCodeAt(0), 0) % PALETTE.length];

    function shape(d, fill, w, extra) {
        const el = document.createElementNS(SVGNS, 'path');
        el.setAttribute('d', d);
        el.setAttribute('fill', fill);
        el.setAttribute('class', 'gear-shape' + (w && w.hp <= 0 ? ' ruined' : '') + (extra ? ' ' + extra : ''));
        return el;
    }

    function drawGear() {
        const g = $('gearLayer');
        g.innerHTML = '';
        const add = (slot, paths, extra) => {
            const w = worn[slot];
            if (!w) return;
            const chain = /chain/i.test(w.item.name);
            paths.forEach(d => {
                g.appendChild(shape(d, colorFor(w.item.name), w, extra));
                if (chain) g.appendChild(shape(d, 'url(#chain)', w, 'texture'));
            });
        };
        add('legs', [zonePath('rleg'), zonePath('lleg'), 'M100 292h100v24H100z']);
        add('top', [zonePath('torso'), zonePath('rarm'), zonePath('larm')]);
        add('feet', [zonePath('rfoot'), zonePath('lfoot'), 'M114 500h24v26h-24z', 'M162 500h24v26h-24z']);
        add('gloves', [zonePath('rhand'), zonePath('lhand')]);
        add('vest', ['M112 122h76l10 12-2 66 4 66H100l4-66-2-66z'], 'vest');
        add('face', ['M124 66h52v20c0 14-12 22-26 22s-26-8-26-22z']);
        add('head', ['M112 62c0-28 16-46 38-46s38 18 38 46c-8-8-22-12-38-12s-30 4-38 12z'], 'helmet');
    }

    function addHitMarker(pt, n) {
        const g = $('hits');
        const c = document.createElementNS(SVGNS, 'g');
        c.setAttribute('class', 'hit');
        c.innerHTML = '<circle cx="' + pt.x + '" cy="' + pt.y + '" r="5"/><text x="' + (pt.x + 7) + '" y="' + (pt.y - 6) + '">' + n + '</text>';
        g.appendChild(c);
    }

    function svgPoint(evt) {
        const svg = $('figure');
        const p = svg.createSVGPoint();
        p.x = evt.clientX; p.y = evt.clientY;
        const r = p.matrixTransform(svg.getScreenCTM().inverse());
        return { x: Math.round(r.x), y: Math.round(r.y) };
    }

    // ---------- Controls ----------
    function buildSlots() {
        $('slots').innerHTML = SLOTS.map(([slot, label]) => {
            const items = D.gear[slot];
            const armored = items.filter(i => i.h < 1 || i.b < 1 || i.s < 1), plain = items.filter(i => !armored.includes(i));
            const opts = list => list.map(i => '<option value="' + esc(i.name) + '">' + esc(i.name) + '</option>').join('');
            return '<div class="slot" data-slot="' + slot + '"><div class="slot-pic"></div><div class="slot-main">' +
                '<label class="slot-label">' + label + '<select class="field field-sm" data-slot-sel="' + slot + '"><option value="">' + esc(t('— none —')) + '</option>' +
                (armored.length ? '<optgroup label="' + esc(t('Armored')) + '">' + opts(armored) + '</optgroup><optgroup label="' + esc(t('Regular')) + '">' + opts(plain) + '</optgroup>' : opts(plain)) +
                '</select></label><div class="slot-info"></div></div></div>';
        }).join('');
        $('slots').addEventListener('change', e => {
            const slot = e.target.dataset.slotSel;
            if (slot) { equip(slot, e.target.value); saveLoadout(); render(); }
        });
    }

    function equip(slot, name) {
        const item = D.gear[slot].find(i => i.name === name);
        if (item) worn[slot] = { item, hp: item.hp }; else delete worn[slot];
        const sel = document.querySelector('[data-slot-sel="' + slot + '"]');
        if (sel) sel.value = item ? item.name : '';
    }

    const PRESETS = {
        'Naked': {},
        'Fresh spawn': { top: ['T-Shirt', 'Shirt', 'Hoodie'], legs: ['Jeans', 'Canvas Pants', 'Cargo Pants'], feet: ['Athletic Shoes', 'Sneakers', 'Hiking Boots'] },
        'Geared': { head: ['Ballistic Helmet'], face: ['Balaclava'], vest: ['Plate Carrier'], top: ['BDU Jacket', 'Combat Jacket'], gloves: ['Tactical Gloves'], legs: ['BDU Pants', 'Combat Pants'], feet: ['Combat Boots'] },
        'Press': { head: ['Combat Helmet'], vest: ['Ballistic Vest'], top: ['Bomber Jacket', 'BDU Jacket'], legs: ['Cargo Pants'], feet: ['Hiking Boots'] },
        'Knight': { head: ['Chainmail Coif', 'Great Helm'], vest: ['Chestplate'], top: ['Chainmail'], legs: ['Chainmail Leggings'], feet: ['Combat Boots'] }
    };

    function applyPreset(name) {
        const p = PRESETS[name];
        SLOTS.forEach(([slot]) => {
            const pick = (p[slot] || []).find(n => D.gear[slot].some(i => i.name === n));
            equip(slot, pick || '');
        });
        saveLoadout();
        render();
    }

    function buildWeapons() {
        const groups = {};
        D.weapons.forEach(w => { (groups[w.type] = groups[w.type] || []).push(w); });
        $('weaponSel').innerHTML = Object.keys(groups).sort().map(ty => '<optgroup label="' + esc(t(ty)) + '">' +
            groups[ty].map(w => '<option value="' + esc(w.name) + '">' + esc(w.name) + '</option>').join('') + '</optgroup>').join('');
        $('weaponSel').addEventListener('change', () => { fillAmmo(); savePrefs(); });
        $('ammoSel').addEventListener('change', () => { showGun(); savePrefs(); });
        $('distance').addEventListener('input', () => { showGun(); savePrefs(); });
    }

    function fillAmmo() {
        const w = weapon();
        $('ammoSel').innerHTML = w.ammo.map(n => '<option>' + esc(n) + '</option>').join('');
        showGun();
    }

    function showGun() {
        const w = weapon(), a = ammo(), d = dist(), f = speedFactor(a, d), n = pelletsLanding(a, d);
        $('distOut').textContent = d + ' ' + t('m');
        $('gunPic').innerHTML = img(w.image, 'gun-img');
        const cell = (k, v, sub) => '<div><span>' + k + '</span><b>' + v + '</b>' + (sub ? '<small>' + sub + '</small>' : '') + '</div>';
        $('ammoStats').innerHTML =
            cell(t('Health'), fmt(a.health * f, 1), a.pellets > 1 ? t('× {0} of {1} pellets', n, a.pellets) : t('per hit')) +
            cell(t('Shock'), fmt(a.shock * f, 1)) +
            cell(t('Blood'), fmt(a.blood * f) + ' ' + t('ml')) +
            cell(t('Speed at {0} m', d), fmt(a.speed * f) + '<small> ' + t('m/s') + '</small>', t('{0} of muzzle speed', pct(f)));
        $('gunModes').textContent = (w.type ? t(w.type) + ' · ' : '') + (w.modes || t('Single'));
    }

    // ---------- Persistence (per viewer convenience only) ----------
    function saveLoadout() { try { const o = {}; Object.keys(worn).forEach(s => { o[s] = worn[s].item.name; }); localStorage.setItem('dz-range-loadout', JSON.stringify(o)); } catch (e) { /* ignore */ } }
    function savePrefs() { try { localStorage.setItem('dz-range-gun', JSON.stringify({ w: $('weaponSel').value, a: $('ammoSel').value, d: dist() })); } catch (e) { /* ignore */ } }
    function load(key) { try { return JSON.parse(localStorage.getItem(key) || 'null'); } catch (e) { return null; } }

    // ---------- Figure events ----------
    function bindFigure() {
        const zones = $('zones'), tip = $('zoneTip'), wrap = tip.parentElement;
        zones.addEventListener('click', e => {
            const z = e.target.closest('path');
            if (z) fire(z.dataset.zone, svgPoint(e));
        });
        zones.addEventListener('mousemove', e => {
            const z = e.target.closest('path');
            if (!z) { tip.hidden = true; return; }
            const k = z.dataset.zone, p = preview(k);
            const r = wrap.getBoundingClientRect();
            tip.hidden = false;
            tip.style.left = Math.min(e.clientX - r.left + 14, r.width - 210) + 'px';
            tip.style.top = (e.clientY - r.top + 14) + 'px';
            tip.innerHTML = '<b>' + esc(ZONES[k].label) + '</b>' +
                '<div>' + t('Health') + ' <em>−' + fmt(p.health, 1) + '</em></div><div>' + t('Shock') + ' <em>−' + fmt(p.shock, 1) + '</em></div>' +
                '<div>' + t('Blood') + ' <em>−' + fmt(p.blood) + ' ' + t('ml') + '</em></div><div>' + t('Bleed chance') + ' <em>' + pct(p.bleedChance) + '</em></div>' +
                (p.p.items.length ? '<div class="muted">' + p.p.items.map(s => esc(worn[s].item.name)).join(' + ') + '</div>' : '');
        });
        zones.addEventListener('mouseleave', () => { tip.hidden = true; });
    }

    function bindButtons() {
        $('presets').innerHTML = Object.keys(PRESETS).map(n => '<button class="chip" data-preset="' + esc(n) + '">' + esc(t(n)) + '</button>').join('');
        $('presets').addEventListener('click', e => { const b = e.target.closest('[data-preset]'); if (b) applyPreset(b.dataset.preset); });
        $('repairAll').addEventListener('click', () => { Object.values(worn).forEach(w => { w.hp = w.item.hp; }); render(); DZ.toast && DZ.toast(t('Gear repaired to pristine')); });
        $('resetBtn').addEventListener('click', () => {
            setPlaying(false); S = freshSurvivor(); shots = 0;
            $('log').innerHTML = ''; $('hits').innerHTML = '';
            render();
        });
        $('playBtn').addEventListener('click', () => { if (S.state !== 'dead') setPlaying(!playing); });
        $('bandageBtn').addEventListener('click', () => {
            if (S.state === 'dead' || !S.wounds.length) return;
            S.wounds.sort((a, b) => b.rate - a.rate);
            const w = S.wounds.shift();
            logEvent(t('Bandaged: {0}', ZONES[w.zone].label.toLowerCase()), 'good');
            render();
        });
        $('epiBtn').addEventListener('click', () => {
            if (S.state === 'dead') return;
            S.shock = MAX.shock; updateState();
            logEvent(t('Epinephrine: shock restored') + (S.state === 'unconscious' ? t(', still out from blood loss') : ''), 'good');
            render();
        });
        $('bloodBtn').addEventListener('click', () => {
            if (S.state === 'dead') return;
            S.blood = Math.min(MAX.blood, S.blood + BLOOD_BAG); updateState();
            logEvent(t('Blood bag: +{0} ml', BLOOD_BAG), 'good');
            render();
        });
    }

    function buildHow() {
        const rows = Object.values(ZONES).filter((z, i, all) => all.findIndex(o => o.health === z.health && o.bleed === z.bleed && o.slots.join() === z.slots.join()) === i);
        const groupName = z => z.group ? t(z.group) : z.label;
        $('howGrid').innerHTML =
            (DZ.lang === 'ru' ? '<div class="prose">' + t('HOW_TEXT') + '</div>' : '<div class="prose"><p><b>Each hit</b> takes the round\'s health, shock and blood damage from the game\'s config, scales it by the bullet speed left at that distance ' +
            '(air friction slows it down), then by the armor over the body part and by the body part itself.</p>' +
            '<p><b>Armor</b> uses each item\'s projectile coefficients. A plate carrier lets through 30% of health damage and 60% of shock, and stops bleeding entirely. ' +
            'Stacked layers multiply. Every hit also wears down the gear: the outer layer takes the round\'s full health damage, the layer under it only what got through. A <b>ruined</b> item stops protecting.</p>' +
            '<p><b>Bleeding:</b> a bullet always cuts unless armor stops it. Wounds bleed ' + '36 ml/min (hands, feet), 216 (arms, legs) or 360 (head, torso) and clot after 5 minutes. ' +
            'Below 3,000 ml blood you pass out; below 2,500 you die. Below 25 shock you are knocked out until it climbs back above 50. ' +
            'A leg or foot that drops to 0 means <b>broken legs</b>.</p>' +
            '<p><b>Shotgun</b> buckshot fires 8 pellets; past about 15 m fewer of them land.</p></div>') +
            '<table class="dz-stats"><tr><th>' + t('Body part') + '</th><td><b>' + t('Health ×') + '</b></td><td><b>' + t('Shock ×') + '</b></td><td><b>' + t('Bleed ml/min') + '</b></td><td><b>' + t('Covered by') + '</b></td></tr>' +
            rows.map(z => '<tr><th>' + esc(groupName(z)) + '</th><td>' + z.health + '</td><td>' + z.shock + '</td><td>' + z.bleed + '</td><td>' +
                z.slots.map(s => SLOTS.find(x => x[0] === s)[1]).join(', ') + '</td></tr>').join('') +
            '</table>';
    }

    // ---------- Boot ----------
    fetch('/data/range.json').then(r => r.json()).then(data => {
        D = data;
        S = freshSurvivor();
        buildSlots();
        buildWeapons();
        bindFigure();
        bindButtons();
        buildHow();

        const prefs = load('dz-range-gun');
        $('weaponSel').value = prefs && D.weapons.some(w => w.name === prefs.w) ? prefs.w : 'KA-M';
        if (!$('weaponSel').value) $('weaponSel').selectedIndex = 0;
        fillAmmo();
        if (prefs) {
            if ([...$('ammoSel').options].some(o => o.value === prefs.a)) $('ammoSel').value = prefs.a;
            if (prefs.d) $('distance').value = prefs.d;
            showGun();
        }
        const saved = load('dz-range-loadout');
        if (saved) { Object.keys(saved).forEach(s => { if (D.gear[s]) equip(s, saved[s]); }); render(); }
        else applyPreset('Fresh spawn');
    }).catch(() => {
        $('rangePage').insertAdjacentHTML('beforeend', '<p class="muted">' + esc(t('Could not load range data.')) + '</p>');
    });
})();
