/*
 * Small UI enhancements for category pages:
 *  - category chips under the header (alongside the existing prev/next arrows)
 *  - live item count under the category title
 * Pages expose their category list as window.dzItemTypes.
 */
(function () {
    const title = document.getElementById('item-title');
    const grid = document.getElementById('items-grid');
    const header = document.querySelector('.container .header');
    if (!title || !grid || !header) return;

    const count = document.createElement('span');
    count.className = 'dz-count';
    title.after(count);

    function updateCount() {
        const n = grid.querySelectorAll('.item-button').length;
        count.textContent = n ? n + (n === 1 ? ' entry' : ' entries') : '';
    }
    new MutationObserver(updateCount).observe(grid, { childList: true });

    const types = window.dzItemTypes || [];
    if (types.length < 2 || typeof window.dzSelectType !== 'function') return;

    const bar = document.createElement('div');
    bar.className = 'dz-tabs';
    const chips = types.map(function (t) {
        const b = document.createElement('button');
        b.type = 'button';
        b.className = 'dz-tab';
        b.textContent = t;
        b.addEventListener('click', function () {
            window.dzSelectType(types.indexOf(t));
        });
        bar.appendChild(b);
        return b;
    });
    header.after(bar);

    function syncActive() {
        chips.forEach(c => c.classList.toggle('active', c.textContent === title.textContent));
    }
    new MutationObserver(syncActive).observe(title, { childList: true, characterData: true, subtree: true });
    syncActive();
})();
