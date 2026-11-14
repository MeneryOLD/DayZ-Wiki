/*
 * Community pages: post list, single post, profile, and the two moderation pages.
 * Uses the shell from app.js (window.DZ). Each page block runs only if its root element exists.
 */
(function () {
    'use strict';
    const DZ = window.DZ;
    if (!DZ) return;
    const { t, toast, esc } = DZ;
    const $ = (sel, root) => (root || document).querySelector(sel);
    const $$ = (sel, root) => Array.from((root || document).querySelectorAll(sel));

    // ---------- Dialog helpers ----------
    function openDialog(d) {
        d.hidden = false;
        const f = d.querySelector('input:not([type=hidden]):not([readonly]):not([hidden]), textarea');
        if (f) setTimeout(() => f.focus(), 40);
    }
    function closeDialog(d) { d.hidden = true; if (d.onclose) d.onclose(); }
    $$('.dialog').forEach(d => {
        if (d.id === 'authDialog') return;   // handled by app.js
        d.addEventListener('mousedown', e => { if (e.target === d) closeDialog(d); });
        $$('[data-close-dialog]', d).forEach(b => b.addEventListener('click', () => closeDialog(d)));
    });
    document.addEventListener('keydown', e => {
        if (e.key !== 'Escape') return;
        $$('.dialog').forEach(d => { if (d.id !== 'authDialog' && !d.hidden) closeDialog(d); });
    });

    /** Ask a yes/no question in the confirm dialog. Resolves true on "yes". */
    function confirmBox(title, text, yesLabel) {
        const d = $('#confirmDialog');
        if (!d) return Promise.resolve(window.confirm(title));
        $('#confirmTitle', d).textContent = title;
        $('#confirmText', d).textContent = text || '';
        $('#confirmYes', d).textContent = yesLabel || t('Delete');
        d.hidden = false;
        setTimeout(() => $('#confirmYes', d).focus(), 40);
        return new Promise(resolve => {
            const done = ok => { d.hidden = true; d.onclose = null; d.removeEventListener('click', onClick); resolve(ok); };
            const onClick = e => {
                const b = e.target.closest('[data-confirm]');
                if (b) done(b.dataset.confirm === 'yes');
                else if (e.target === d) done(false);
            };
            d.onclose = () => done(false);
            d.addEventListener('click', onClick);
        });
    }

    async function copyLink(url) {
        try { await navigator.clipboard.writeText(url || location.href); toast(t('Link copied.')); }
        catch (e) { toast(t('Could not copy the link.'), false); }
    }

    /** Read {message} from a JSON error response, or fall back to plain text. */
    async function errorText(r, fallback) {
        try {
            const text = await r.text();
            if (!text) return fallback;
            if (text.charAt(0) === '{') { const j = JSON.parse(text); return j.message ? t(j.message) : fallback; }
            return text.length < 200 ? t(text) : fallback;
        } catch (e) { return fallback; }
    }

    // ---------- Composer (write / edit a post) ----------
    const composer = $('#composer');
    const MAX_IMAGES = 3, MAX_BYTES = 8 * 1024 * 1024;
    let compose = null;   // { id, keep: [{id, src}], files: [File] }

    function renderThumbs() {
        const box = $('#composerThumbs');
        const urls = [];
        box.innerHTML = compose.keep.map((img, i) =>
            '<div class="thumb-item"><img src="' + esc(img.src) + '" alt=""><button type="button" data-drop-keep="' + i + '" aria-label="' + esc(t('Remove')) + '"><i class="fa-solid fa-xmark"></i></button></div>').join('') +
            compose.files.map((f, i) => {
                const u = URL.createObjectURL(f); urls.push(u);
                return '<div class="thumb-item"><img src="' + u + '" alt=""><span class="thumb-new">' + esc(t('new')) + '</span><button type="button" data-drop-file="' + i + '" aria-label="' + esc(t('Remove')) + '"><i class="fa-solid fa-xmark"></i></button></div>';
            }).join('');
        $('#composerAdd').hidden = compose.keep.length + compose.files.length >= MAX_IMAGES;
    }
    function addFiles(list) {
        for (const f of Array.from(list)) {
            if (!/^image\/(jpeg|png|webp|gif)$/.test(f.type)) { toast(t('Only JPG, PNG, WebP and GIF images are supported.'), false); continue; }
            if (f.size > MAX_BYTES) { toast(t('Each image must be 8 MB or smaller.'), false); continue; }
            if (compose.keep.length + compose.files.length >= MAX_IMAGES) { toast(t('You can attach up to 3 images.'), false); break; }
            compose.files.push(f);
        }
        renderThumbs();
    }
    function updateCount() {
        const ta = $('[name="content"]', composer), c = $('#composerCount');
        c.textContent = ta.value.length + ' / 5000';
        c.classList.toggle('near', ta.value.length > 4500);
    }
    function openComposer(post) {
        if (!composer) return;
        if (!DZ.currentUser()) { DZ.openAuth('signin'); toast(t('Sign in to write a post.'), false); return; }
        const form = $('#composerForm');
        form.reset();
        $$('.invalid', form).forEach(x => x.classList.remove('invalid'));
        compose = { id: post ? post.id : null, keep: post ? post.images.slice() : [], files: [] };
        const h = $('#composerTitle');
        h.textContent = post ? h.dataset.edit : h.dataset.new;
        if (post) { form.elements.title.value = post.title; form.elements.content.value = post.content; }
        $('#composerNote').hidden = !!(post && DZ_MOD);
        renderThumbs(); updateCount();
        openDialog(composer);
    }
    // Moderators' edits keep a post published, so the "needs approval" note is hidden for them.
    const DZ_MOD = !!document.querySelector('.nav-link[href="/moder/posts"]');

    if (composer) {
        const form = $('#composerForm'), drop = $('#composerDrop'), input = $('#composerFiles');
        $('#composerAdd').addEventListener('click', () => input.click());
        input.addEventListener('change', () => { addFiles(input.files); input.value = ''; });
        ['dragenter', 'dragover'].forEach(ev => drop.addEventListener(ev, e => { e.preventDefault(); drop.classList.add('drag'); }));
        ['dragleave', 'drop'].forEach(ev => drop.addEventListener(ev, e => { e.preventDefault(); drop.classList.remove('drag'); }));
        drop.addEventListener('drop', e => addFiles(e.dataTransfer.files));
        composer.addEventListener('paste', e => { if (e.clipboardData && e.clipboardData.files.length) addFiles(e.clipboardData.files); });
        $('#composerThumbs').addEventListener('click', e => {
            const k = e.target.closest('[data-drop-keep]'), f = e.target.closest('[data-drop-file]');
            if (k) compose.keep.splice(+k.dataset.dropKeep, 1);
            else if (f) compose.files.splice(+f.dataset.dropFile, 1);
            else return;
            renderThumbs();
        });
        form.elements.content.addEventListener('input', updateCount);
        $$('input, textarea', form).forEach(i => i.addEventListener('input', () => i.classList.remove('invalid')));

        form.addEventListener('submit', e => {
            e.preventDefault();
            const title = form.elements.title.value.trim(), content = form.elements.content.value.trim();
            if (title.length < 3) { DZ.markInvalid(form.elements.title, t('The title must be 3-120 characters.')); return; }
            if (content.length < 10) { DZ.markInvalid(form.elements.content, t('The text must be 10-5000 characters.')); return; }
            const fd = new FormData();
            fd.append('title', title);
            fd.append('content', content);
            compose.files.forEach(f => fd.append('images', f));
            compose.keep.forEach(img => fd.append('keepImageIds', img.id));
            DZ.busy(form, async () => {
                try {
                    const r = await fetch(compose.id ? '/posts/edit/' + compose.id : '/posts/create', { method: 'POST', body: fd });
                    if (r.status === 401 || r.status === 403 && !compose.id) { DZ.openAuth('signin'); toast(t('Sign in to write a post.'), false); return; }
                    if (!r.ok) { toast(await errorText(r, t('Could not save the post. Try again.')), false); return; }
                    const j = await r.json();
                    closeDialog(composer);
                    if (compose.id) {
                        toast(j.approved ? t('Post updated.') : t('Post updated. It is back in the review queue.'));
                        setTimeout(() => location.reload(), 600);
                    } else {
                        toast(t('Sent for review. A moderator will publish it soon.'));
                        setTimeout(() => { location.href = '/posts/' + j.id; }, 700);
                    }
                } catch (err) { toast(t('Could not reach the server. Try again.'), false); }
            });
        });
        $$('[data-compose]').forEach(b => b.addEventListener('click', () => openComposer(null)));
        if (new URLSearchParams(location.search).get('compose') === '1') {
            history.replaceState(null, '', location.pathname);
            openComposer(null);
        }
    }

    // ---------- Post list ----------
    const grid = $('#postGrid');
    if (grid) {
        const cards = $$('.post-card', grid);
        const filter = $('#postFilter'), sort = $('#postSort'), count = $('#postCount'), none = $('#postNoMatch');
        const apply = () => {
            const q = filter.value.trim().toLowerCase();
            let shown = 0;
            cards.forEach(c => {
                const hit = !q || [c.dataset.title, c.dataset.text, c.dataset.author].some(v => v && v.indexOf(q) > -1);
                c.hidden = !hit; if (hit) shown++;
            });
            const dir = sort.value === 'old' ? 1 : -1;
            cards.slice().sort((a, b) => dir * (a.dataset.time < b.dataset.time ? -1 : a.dataset.time > b.dataset.time ? 1 : 0))
                .forEach(c => grid.appendChild(c));
            count.textContent = shown === cards.length ? String(cards.length) : shown + ' / ' + cards.length;
            none.hidden = shown > 0;
        };
        let timer;
        filter.addEventListener('input', () => { clearTimeout(timer); timer = setTimeout(apply, 100); });
        sort.addEventListener('change', apply);
    }

    // ---------- Lightbox ----------
    function initLightbox(buttons) {
        const box = $('#lightbox');
        if (!box || !buttons.length) return;
        const img = $('#lightboxImg'), cap = $('#lightboxCaption');
        let i = 0;
        const show = n => {
            i = (n + buttons.length) % buttons.length;
            img.src = buttons[i].dataset.full;
            cap.textContent = buttons.length > 1 ? (i + 1) + ' / ' + buttons.length : '';
            $$('.lightbox-nav', box).forEach(b => { b.hidden = buttons.length < 2; });
            box.hidden = false;
        };
        buttons.forEach((b, n) => b.addEventListener('click', () => show(n)));
        box.addEventListener('click', e => {
            const nav = e.target.closest('[data-step]');
            if (nav) { show(i + +nav.dataset.step); return; }
            if (e.target === box || e.target.closest('.lightbox-close')) box.hidden = true;
        });
        document.addEventListener('keydown', e => {
            if (box.hidden) return;
            if (e.key === 'Escape') box.hidden = true;
            else if (e.key === 'ArrowRight') show(i + 1);
            else if (e.key === 'ArrowLeft') show(i - 1);
        });
    }

    // ---------- Moderation actions (post page + queue) ----------
    async function moderate(id, action) {
        const r = await fetch('/moder/posts/' + action + '/' + id, { method: 'POST' });
        if (!r.ok) throw new Error(String(r.status));
    }

    // ---------- Single post ----------
    const postPage = $('#postPage');
    if (postPage) {
        const id = postPage.dataset.postId;
        initLightbox($$('.post-shot'));
        $('#shareBtn').addEventListener('click', () => copyLink(location.origin + '/posts/' + id));
        const edit = $('#editBtn');
        if (edit) edit.addEventListener('click', () => openComposer({
            id,
            title: $('#postSourceTitle').value,
            content: $('#postSource').value,
            images: $$('.post-shot').map(b => ({ id: b.dataset.imageId, src: b.dataset.full }))
        }));
        const del = $('#deleteBtn');
        if (del) del.addEventListener('click', async () => {
            if (!await confirmBox(t('Delete this post?'), t('The post and its images are removed for good.'), t('Delete'))) return;
            try {
                const r = await fetch('/posts/' + id, { method: 'DELETE' });
                if (!r.ok) throw new Error();
                toast(t('Post deleted.'));
                setTimeout(() => { location.href = '/posts'; }, 500);
            } catch (e) { toast(t('Could not delete the post.'), false); }
        });
        $$('[data-moderate]', postPage).forEach(b => b.addEventListener('click', async () => {
            const action = b.dataset.moderate;
            if (action === 'reject' && !await confirmBox(t('Reject this post?'), t('The post and its images are removed for good.'), t('Reject'))) return;
            try {
                await moderate(id, action);
                toast(action === 'approve' ? t('Post published.') : t('Post rejected.'));
                setTimeout(() => { location.href = action === 'approve' ? location.pathname : '/moder/posts'; }, 500);
            } catch (e) { toast(t('Something went wrong. Try again.'), false); }
        }));
    }

    // ---------- Moderation queue ----------
    const queue = $('#queue');
    if (queue) {
        const lead = $('#queueLead');
        const refresh = () => {
            const left = $$('.queue-item', queue).length;
            if (!left) { $('#queueEmpty').hidden = false; lead.textContent = lead.dataset.zero; }
            else lead.textContent = t('Waiting for review: {0}. Oldest first.', left);
        };
        $$('.queue-item', queue).forEach(item => {
            const text = $('.queue-text', item), more = $('.queue-more', item);
            text.classList.add('clamped');
            if (text.scrollHeight > text.clientHeight + 4) {
                more.hidden = false;
                more.addEventListener('click', () => {
                    const open = text.classList.toggle('clamped');
                    more.textContent = open ? t('Show more') : t('Show less');
                });
            } else text.classList.remove('clamped');
        });
        queue.addEventListener('click', async e => {
            const b = e.target.closest('[data-moderate]');
            if (!b) return;
            const item = b.closest('.queue-item'), action = b.dataset.moderate;
            if (action === 'reject' && !await confirmBox(t('Reject this post?'), t('The post and its images are removed for good.'), t('Reject'))) return;
            $$('button', item).forEach(x => { x.disabled = true; });
            try {
                await moderate(item.dataset.postId, action);
                toast(action === 'approve' ? t('Post published.') : t('Post rejected.'));
                item.classList.add('leaving');
                setTimeout(() => { item.remove(); refresh(); }, 300);
            } catch (err) {
                $$('button', item).forEach(x => { x.disabled = false; });
                toast(t('Something went wrong. Try again.'), false);
            }
        });
    }

    // ---------- Profile ----------
    const profile = $('#profilePage');
    if (profile) {
        $('#shareProfile').addEventListener('click', () => copyLink(location.origin + '/@' + encodeURIComponent(profile.dataset.userName)));

        if (profile.dataset.owner === 'true') {
            const upload = async (input, field, url, onDone) => {
                const f = input.files[0];
                input.value = '';
                if (!f) return;
                if (!/^image\/(jpeg|png)$/.test(f.type) || f.size > MAX_BYTES) { toast(t('Choose a JPG or PNG image up to 8 MB.'), false); return; }
                const fd = new FormData();
                fd.append(field, f);
                onDone(URL.createObjectURL(f), true);
                try {
                    const r = await fetch(url, { method: 'POST', body: fd });
                    if (!r.ok) throw new Error(await errorText(r, t('Upload failed. Try another image.')));
                    toast(field === 'avatar' ? t('Profile picture updated.') : t('Cover updated.'));
                    onDone(null, false);
                } catch (err) { toast(err.message || t('Upload failed. Try another image.'), false); location.reload(); }
            };
            const avatarInput = $('#avatarInput'), bannerInput = $('#bannerInput');
            $('#avatarBtn').addEventListener('click', () => avatarInput.click());
            $('#bannerBtn').addEventListener('click', () => bannerInput.click());
            avatarInput.addEventListener('change', () => upload(avatarInput, 'avatar', '/profile/upload-avatar', (src, busy) => {
                const box = $('.profile-avatar');
                box.classList.toggle('busy', busy);
                if (src) $('#avatarImg').src = src;
                if (!busy) $$('img[src*="get-avatar?user_id=' + profile.dataset.userId + '"]').forEach(i => { i.src = i.src.split('&')[0] + '&v=' + Date.now(); });
            }));
            bannerInput.addEventListener('change', () => upload(bannerInput, 'banner', '/profile/upload-banner', src => {
                if (!src) return;
                let img = $('.profile-banner-img');
                if (!img) { img = document.createElement('img'); img.className = 'profile-banner-img'; img.alt = ''; $('.profile-banner').prepend(img); }
                img.src = src;
            }));

            $('#signOutBtn').addEventListener('click', async () => {
                try { await fetch('/api/v1/auth/logout', { method: 'POST' }); } catch (e) { /* ignore */ }
                document.cookie = 'dayzwiki_user_token=; Max-Age=0; path=/';
                location.href = '/';
            });

            const settings = $('#settingsDialog');
            const tab = name => {
                $$('[data-settings-tab]', settings).forEach(b => b.classList.toggle('active', b.dataset.settingsTab === name));
                $$('[data-settings]', settings).forEach(f => { f.hidden = f.dataset.settings !== name; });
                const first = $('[data-settings="' + name + '"] input:not([readonly])', settings);
                if (first) setTimeout(() => first.focus(), 30);
            };
            $('#settingsBtn').addEventListener('click', () => { openDialog(settings); tab('email'); });
            $$('[data-settings-tab]', settings).forEach(b => b.addEventListener('click', () => tab(b.dataset.settingsTab)));
            DZ.initPasswordToggles(settings);
            const pwForm = $('[data-settings="password"]', settings);
            DZ.bindStrength($('[data-strength]', pwForm), $('.pw-meter', pwForm), $('[data-meter-label]', pwForm));
            $$('input', settings).forEach(i => i.addEventListener('input', () => i.classList.remove('invalid')));

            const emailForm = $('[data-settings="email"]', settings);
            emailForm.addEventListener('submit', e => {
                e.preventDefault();
                const current = emailForm.elements.email.value.trim(), next = emailForm.elements.newEmail.value.trim();
                if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(next)) { DZ.markInvalid(emailForm.elements.newEmail, t('Incorrect email format')); return; }
                if (next.toLowerCase() === current.toLowerCase()) { DZ.markInvalid(emailForm.elements.newEmail, t('That is already your email.')); return; }
                DZ.busy(emailForm, async () => {
                    try {
                        const r = await fetch('/api/v1/auth/change/email?email=' + encodeURIComponent(current) + '&newEmail=' + encodeURIComponent(next));
                        if (r.ok) { emailForm.elements.newEmail.value = ''; closeDialog(settings); toast(t('We sent a confirmation link to {0}.', next)); }
                        else DZ.markInvalid(emailForm.elements.newEmail, t('This email is already used by another account.'));
                    } catch (err) { toast(t('Could not reach the server. Try again.'), false); }
                });
            });

            pwForm.addEventListener('submit', e => {
                e.preventDefault();
                const cur = pwForm.elements.current, pw = pwForm.elements.password, pw2 = pwForm.elements.password2;
                if (!cur.value) { DZ.markInvalid(cur, t('Please fill in the highlighted fields.')); return; }
                if (pw.value.length < 6) { DZ.markInvalid(pw, t('Password must be at least 6 characters')); return; }
                if (pw.value !== pw2.value) { DZ.markInvalid(pw2, t('Passwords do not match.')); return; }
                if (pw.value === cur.value) { DZ.markInvalid(pw, t('The new password must be different.')); return; }
                DZ.busy(pwForm, async () => {
                    try {
                        const r = await fetch('/api/v1/auth/change/password/by-username', {
                            method: 'POST', headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ username: profile.dataset.userName, currentPassword: cur.value, newPassword: pw.value })
                        });
                        if (r.ok) { pwForm.reset(); $('.pw-meter', pwForm).dataset.meter = 0; closeDialog(settings); toast(t('Password changed.')); }
                        else DZ.markInvalid(cur, t('Current password is incorrect.'));
                    } catch (err) { toast(t('Could not reach the server. Try again.'), false); }
                });
            });
        }
    }

    // ---------- Featured videos ----------
    const videosPage = $('#modVideosPage');
    if (videosPage) {
        const dialog = $('#videoDialog'), form = $('#videoForm'), preview = $('#videoPreview');
        let editing = null;
        const parseId = v => {
            v = (v || '').trim();
            const m = v.match(/(?:youtu\.be\/|[?&]v=|\/embed\/|\/shorts\/|\/live\/)([\w-]{11})/);
            if (m) return m[1];
            return /^[\w-]{11}$/.test(v) ? v : null;
        };
        const showPreview = () => {
            const id = parseId(form.elements.link.value);
            preview.hidden = !id;
            if (id) $('img', preview).src = 'https://i.ytimg.com/vi/' + id + '/mqdefault.jpg';
        };
        form.elements.link.addEventListener('input', showPreview);
        $$('input', form).forEach(i => i.addEventListener('input', () => i.classList.remove('invalid')));

        const open = card => {
            form.reset();
            editing = card ? card.dataset.id : null;
            const h = $('#videoDialogTitle');
            h.textContent = card ? h.dataset.edit : h.dataset.new;
            form.elements.link.disabled = !!card;
            if (card) {
                form.elements.link.value = 'https://www.youtube.com/watch?v=' + card.dataset.id;
                form.elements.name.value = card.dataset.name;
                form.elements.order.value = card.dataset.order;
                form.elements.expiry.value = card.dataset.expiry;
                form.elements.active.checked = card.dataset.active === 'true';
            } else {
                const d = new Date(); d.setMonth(d.getMonth() + 1);
                form.elements.expiry.value = d.toISOString().slice(0, 10);
            }
            showPreview();
            openDialog(dialog);
        };
        const payload = (id, name, order, expiry, active) =>
            ({ id, name, order: +order || 0, active, expiryDate: expiry ? expiry + 'T23:59:00' : null });
        const save = async (method, url, body) => {
            const r = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
            if (!r.ok) throw new Error(String(r.status));
        };

        $('#addVideoBtn').addEventListener('click', () => open(null));
        form.addEventListener('submit', e => {
            e.preventDefault();
            const id = editing || parseId(form.elements.link.value);
            if (!id) { DZ.markInvalid(form.elements.link, t('Paste a YouTube link or an 11-character video ID.')); return; }
            if (!form.elements.name.value.trim()) { DZ.markInvalid(form.elements.name, t('Please fill in the highlighted fields.')); return; }
            if (!form.elements.expiry.value) { DZ.markInvalid(form.elements.expiry, t('Please fill in the highlighted fields.')); return; }
            const body = payload(id, form.elements.name.value.trim(), form.elements.order.value, form.elements.expiry.value, form.elements.active.checked);
            DZ.busy(form, async () => {
                try {
                    await save(editing ? 'PUT' : 'POST', editing ? '/api/v1/youtube/' + encodeURIComponent(id) : '/api/v1/youtube', body);
                    closeDialog(dialog);
                    toast(editing ? t('Video updated.') : t('Video added.'));
                    setTimeout(() => location.reload(), 500);
                } catch (err) { toast(t('Could not save the video.'), false); }
            });
        });

        videosPage.addEventListener('click', async e => {
            const card = e.target.closest('.video-card');
            if (!card) return;
            if (e.target.closest('[data-video-edit]')) open(card);
            else if (e.target.closest('[data-video-toggle]')) {
                const d = card.dataset;
                try {
                    await save('PUT', '/api/v1/youtube/' + encodeURIComponent(d.id), payload(d.id, d.name, d.order, d.expiry, d.active !== 'true'));
                    toast(d.active === 'true' ? t('Video hidden.') : t('Video is active again.'));
                    setTimeout(() => location.reload(), 400);
                } catch (err) { toast(t('Could not save the video.'), false); }
            } else if (e.target.closest('[data-video-delete]')) {
                if (!await confirmBox(t('Delete this video?'), card.dataset.name, t('Delete'))) return;
                try {
                    const r = await fetch('/api/v1/youtube/' + encodeURIComponent(card.dataset.id), { method: 'DELETE' });
                    if (!r.ok) throw new Error();
                    toast(t('Video deleted.'));
                    card.remove();
                    if (!$('.video-card')) location.reload();
                } catch (err) { toast(t('Could not delete the video.'), false); }
            }
        });
    }
})();
