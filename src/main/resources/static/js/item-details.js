/*
 * Shared renderer for the item details modal.
 * Every page calls window.renderItemDetails(item) and appends the result after the title.
 */
(function () {
    const t = window.DZ_I18N ? window.DZ_I18N.t : s => s;
    const vocab = window.DZ_I18N ? window.DZ_I18N.vocab : s => s;

    function esc(v) {
        return String(v)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    function present(v) {
        return v !== null && v !== undefined && v !== '' && !(typeof v === 'number' && (v === 0 || isNaN(v)));
    }

    const kg = v => (v >= 1 ? v.toFixed(2).replace(/\.?0+$/, '') + ' ' + t('kg') : Math.round(v * 1000) + ' ' + t('g'));
    const slots = v => /^\d+x\d+$/.test(v) ? v + ' (' + t('{0} slots', v.split('x').reduce((a, b) => a * b)) + ')' : v;
    const hp = v => v + ' ' + t('hp');
    const pct = v => Math.round(v) + '%';

    // [label, field, formatter?]
    const FIELDS = {
        weapons: [
            ['Ammunition', 'ammunition'],
            ['Damage (health)', 'damage'],
            ['Heavy attack damage', 'heavyDamage'],
            ['Damage type', 'damageType'],
            ['Fire modes', 'fireModes'],
            ['Magazines', 'magazines'],
            ['Size', 'size', slots],
            ['Weight', 'weight', kg],
            ['Durability', 'durability', hp]
        ],
        ammunition: [
            ['Health damage', 'healthDamage'],
            ['Blood damage', 'bloodDamage'],
            ['Shock damage', 'shockDamage'],
            ['Used by', 'usedBy'],
            ['Size', 'size'],
            ['Weight per round', 'weight', kg]
        ],
        armors: [
            ['Cargo capacity', 'capacity', v => t('{0} slots', v)],
            ['Damage reduction', 'protection'],
            ['Heat insulation', 'insulation'],
            ['Attachments', 'additionalSlots'],
            ['Durability', 'durability', hp],
            ['Size', 'size', slots],
            ['Weight', 'weight', kg]
        ],
        foods: [
            ['Energy (full item)', 'baseCalories', v => v + ' ' + t('kcal')],
            ['Water (full item)', 'baseThirst', v => v + ' ' + t('ml')],
            ['Stomach volume', 'stomach', v => v + ' ' + t('ml')],
            ['States', 'states'],
            ['Size', 'size', slots],
            ['Weight', 'weight', kg]
        ],
        medicaments: [
            ['Use', 'medicalUse'],
            ['Uses', 'numberOfUses'],
            ['Size', 'size', slots],
            ['Weight', 'weight', kg]
        ],
        modules: [
            ['Magnification', 'magnification', v => v + 'x'],
            ['Capacity', 'capacity', v => t('{0} rounds', v)],
            ['Fits', 'usedBy'],
            ['Durability', 'durability', hp],
            ['Size', 'size', slots],
            ['Weight', 'weight', kg]
        ],
        explosives: [
            ['Delivery', 'delivery'],
            ['Size', 'size', slots],
            ['Weight', 'weight', kg]
        ],
        cars: [
            ['Seats', 'seats'],
            ['Cargo', 'capacity'],
            ['Fuel tank', 'fuelTank', v => v + ' ' + t('L')],
            ['Top speed', 'topSpeed', v => '~' + v + ' ' + t('km/h')]
        ],
        diseases: [
            ['Causes', 'causes'],
            ['Symptoms', 'symptoms'],
            ['Treatment', 'treatment'],
            ['Prevention', 'prevention']
        ]
    };

    function value(item, field) {
        return typeof field === 'function' ? field(item) : item[field];
    }

    window.renderItemDetails = function (item) {
        let html = '<div class="dz-details">';

        const chips = [];
        if (present(item.type)) chips.push('<span class="dz-chip">' + esc(t(item.type)) + '</span>');
        if (present(item.tier)) chips.push('<span class="dz-chip dz-chip-tier">' + esc(vocab(item.tier)) + '</span>');
        else if (present(item.rarity)) chips.push('<span class="dz-chip dz-chip-tier">' + esc(vocab(item.rarity)) + '</span>');
        if (chips.length) html += '<div class="dz-chips">' + chips.join('') + '</div>';

        if (present(item.description)) html += '<p class="dz-desc">' + esc(item.description) + '</p>';

        const rows = (FIELDS[item.source] || [])
            .map(([label, field, fmt]) => {
                const v = value(item, field);
                return present(v) ? '<tr><th>' + esc(t(label)) + '</th><td>' + esc(fmt ? fmt(v) : v) + '</td></tr>' : '';
            })
            .join('');
        if (rows) html += '<table class="dz-stats">' + rows + '</table>';

        const where = [];
        if (present(item.location)) where.push('<tr><th>' + esc(t('Found in')) + '</th><td>' + esc(item.location) + '</td></tr>');
        if (present(item.maps)) where.push('<tr><th>' + esc(t('Maps')) + '</th><td>' + esc(vocab(item.maps)) + '</td></tr>');
        if (present(item.rarity) && present(item.tier) && item.tier.indexOf(item.rarity) === -1) {
            where.push('<tr><th>' + esc(t('Rarity')) + '</th><td>' + esc(vocab(item.rarity)) + '</td></tr>');
        }
        if (where.length) html += '<div class="dz-subhead">' + esc(t('Where to find')) + '</div><table class="dz-stats">' + where.join('') + '</table>';

        if (present(item.classname)) html += '<div class="dz-class">' + esc(t('Class name:')) + ' <code>' + esc(item.classname) + '</code></div>';

        return html + '</div>';
    };
})();
