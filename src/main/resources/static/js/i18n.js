/*
 * Client-side translations. The page language comes from <html lang> (set by the server from the dz-lang cookie).
 * Strings are keyed by their English text: DZ_I18N.t('Weapons') returns the Russian text on Russian pages,
 * and the English key itself everywhere else. "{0}", "{1}" are replaced by the extra arguments.
 */
(function () {
    'use strict';
    const lang = (document.documentElement.lang || 'en').slice(0, 2).toLowerCase() === 'ru' ? 'ru' : 'en';

    const RU = {
        // ----- Sections -----
        'Weapons': 'Оружие', 'Ammunition': 'Боеприпасы', 'Attachments': 'Обвесы', 'Explosives': 'Взрывчатка',
        'Food & Drink': 'Еда и напитки', 'Medical': 'Медицина', 'Diseases': 'Болезни',
        'Clothing & Gear': 'Одежда и снаряжение', 'Vehicles': 'Транспорт',
        'Firearms, bows, launchers and melee weapons, with damage, calibers and magazines.': 'Огнестрельное оружие, луки, гранатомёты и холодное оружие: урон, калибры и магазины.',
        'Every cartridge, shell, grenade and bolt, with health, blood and shock damage.': 'Все патроны, гильзы, гранаты и болты: урон здоровью, крови и шоку.',
        'Optics, magazines, suppressors, stocks, handguards and the weapons they fit.': 'Прицелы, магазины, глушители, приклады, цевья и оружие, к которому они подходят.',
        'Grenades, mines and charges — how they are deployed and where to find them.': 'Гранаты, мины и заряды: как их применять и где найти.',
        'Energy and water values for canned goods, game meat, produce, drinks and Nasdara specialities.': 'Калорийность и вода консервов, мяса дичи, овощей и фруктов, напитков и блюд Насдары.',
        'Bandages, pills, injectors and transfusion kits — what each one treats.': 'Бинты, таблетки, автоинъекторы и наборы для переливания: что чем лечится.',
        'Every illness and condition, including the new Badlands heat and sandstorm ailments.': 'Все болезни и состояния, включая новые недуги Badlands от жары и песчаных бурь.',
        'Clothing, armour, backpacks and belts: storage, insulation and damage reduction.': 'Одежда, броня, рюкзаки и ремни: вместимость, утепление и защита от урона.',
        'Cars, trucks, the boat and the new 1.30 motorbikes, with spawn caps for each map.': 'Машины, грузовики, лодка и новые мотоциклы 1.30 с лимитами появления на каждой карте.',

        // ----- Item types (category chips) -----
        'All': 'Все',
        'Assault Rifles': 'Штурмовые винтовки', 'Marksman Rifles': 'Марксманские винтовки', 'Rifles': 'Винтовки',
        'Submachine Guns': 'Пистолеты-пулемёты', 'Shotguns': 'Дробовики', 'Handguns': 'Пистолеты',
        'Launchers and Bows': 'Гранатомёты и луки', 'Melee': 'Холодное оружие',
        'Pistol': 'Пистолетные', 'Intermediate': 'Промежуточные', 'Full-power': 'Винтовочные',
        'Shotgun Shells': 'Патроны для дробовика', '40mm Grenades': 'Гранаты 40 мм', 'Arrows and Bolts': 'Стрелы и болты',
        'Optics': 'Прицелы', 'Magazines': 'Магазины', 'Muzzle Devices': 'Дульные устройства', 'Handguards': 'Цевья',
        'Buttstocks': 'Приклады', 'Illumination': 'Фонари', 'Bayonets': 'Штыки', 'Wraps': 'Камуфляжные обмотки',
        'Canned Goods': 'Консервы', 'Dry Goods and Snacks': 'Сухие продукты и снеки', 'Drinks': 'Напитки',
        'Fruits and Vegetables': 'Фрукты и овощи', 'Meat and Fish': 'Мясо и рыба', 'Mushrooms': 'Грибы', 'Other': 'Другое',
        'Treatment': 'Лечение', 'Medicines': 'Лекарства', 'Pain Relief': 'Обезболивающие', 'Disinfectants': 'Дезинфекция',
        'Medical Equipment': 'Медоборудование',
        'Helmets': 'Шлемы', 'Hats': 'Головные уборы', 'Masks': 'Маски', 'Eyewear': 'Очки', 'Shirts and Jackets': 'Рубашки и куртки',
        'Vests': 'Жилеты', 'Gloves': 'Перчатки', 'Pants': 'Штаны', 'Shoes': 'Обувь', 'Belts': 'Ремни', 'Backpacks': 'Рюкзаки',
        'Ghillie and Camo': 'Гилли и камуфляж', 'Accessories': 'Аксессуары',
        // weapon types in range.json
        'Archery': 'Лук', 'Assault Rifle': 'Штурмовая винтовка', 'Assault rifle': 'Штурмовая винтовка',
        'Automatic Rifle': 'Автоматическая винтовка', 'Automatic rifle': 'Автоматическая винтовка', 'Battle Rifle': 'Боевая винтовка',
        'Bolt-action Rifle': 'Винтовка с продольно-скользящим затвором', 'Break-action Rifle': 'Переломная винтовка',
        'Breech-action Rifle': 'Однозарядная винтовка', 'Handgun': 'Пистолет', 'Lever-action Rifle': 'Винтовка с рычажным затвором',
        'Marksman Rifle': 'Марксманская винтовка', 'Semi-automatic Rifle': 'Самозарядная винтовка', 'Shotgun': 'Дробовик',
        'Submachine Gun': 'Пистолет-пулемёт',

        // ----- Card / table stats -----
        'Damage': 'Урон', 'Caliber': 'Калибр', 'Weight': 'Вес', 'Health': 'Здоровье', 'Blood': 'Кровь', 'Shock': 'Шок',
        'Health damage': 'Урон здоровью', 'Shock damage': 'Урон шоку', 'Zoom': 'Кратность', 'Rounds': 'Патронов',
        'Capacity': 'Вместимость', 'Kind': 'Тип', 'Delivery': 'Применение', 'Energy': 'Энергия', 'Water': 'Вода',
        'Uses': 'Применений', 'Size': 'Размер', 'Slots': 'Слоты', 'Insul.': 'Утепл.', 'Storage': 'Вместимость',
        'Durability': 'Прочность', 'km/h': 'км/ч', 'Seats': 'Мест', 'Fuel': 'Топливо', 'Top speed': 'Макс. скорость',
        'Fuel tank': 'Топливный бак', 'Name': 'Название', 'Type': 'Тип', 'Tier': 'Уровень', 'Maps': 'Карты',
        'kg': 'кг', 'g': 'г', 'L': 'л', 'Unique': 'Уник.',
        'Symptoms: {0}': 'Симптомы: {0}',
        'Spawns on: {0}': 'Появляется на: {0}',
        'Could not load this section. Refresh to try again.': 'Не удалось загрузить раздел. Обновите страницу.',
        'No entries match these filters.': 'Нет записей под эти фильтры.',

        // ----- Item details drawer -----
        'Damage (health)': 'Урон (здоровье)', 'Heavy attack damage': 'Урон сильного удара', 'Damage type': 'Тип урона',
        'Fire modes': 'Режимы огня', 'Used by': 'Используется в', 'Weight per round': 'Вес патрона',
        'Cargo capacity': 'Вместимость', 'Damage reduction': 'Снижение урона', 'Heat insulation': 'Теплоизоляция',
        'Energy (full item)': 'Энергия (целый предмет)', 'Water (full item)': 'Вода (целый предмет)',
        'Stomach volume': 'Объём в желудке', 'States': 'Состояния', 'Use': 'Применение', 'Magnification': 'Увеличение',
        'Fits': 'Подходит к', 'Cargo': 'Грузовой отсек', 'Causes': 'Причины', 'Symptoms': 'Симптомы',
        'Prevention': 'Профилактика', 'Found in': 'Где искать', 'Rarity': 'Редкость', 'Where to find': 'Где найти',
        'Class name:': 'Имя класса:', '{0} slots': '{0} слотов', '{0} rounds': '{0} патронов', 'hp': 'ед.',
        'kcal': 'ккал', 'ml': 'мл',

        // ----- Shell: auth, bookmarks, search -----
        'Please fill in the highlighted fields.': 'Заполните выделенные поля.',
        'Signed in.': 'Вы вошли.',
        'Wrong username/email or password.': 'Неверное имя пользователя/email или пароль.',
        'Could not reach the server. Try again.': 'Сервер недоступен. Попробуйте ещё раз.',
        'Passwords do not match.': 'Пароли не совпадают.',
        'Account created.': 'Аккаунт создан.',
        'Registration failed.': 'Не удалось зарегистрироваться.',
        'Check your inbox for the reset link.': 'Проверьте почту: мы отправили ссылку для сброса.',
        'Check your inbox': 'Проверьте почту',
        'We sent a confirmation link to {0}. Open it to activate your account, then sign in.': 'Мы отправили ссылку для подтверждения на {0}. Откройте её, чтобы активировать аккаунт, а затем войдите.',
        'We sent a password reset link to {0}.': 'Мы отправили ссылку для сброса пароля на {0}.',
        'Could not send the reset email. Check the address and try again.': 'Не удалось отправить письмо для сброса. Проверьте адрес и попробуйте снова.',
        'Too short': 'Слишком короткий', 'Weak': 'Слабый', 'Good': 'Хороший', 'Strong': 'Надёжный',
        'Registration failed. Try again in a minute.': 'Не удалось зарегистрироваться. Попробуйте через минуту.',
        'Email confirmed. You can sign in now.': 'Email подтверждён. Теперь можно войти.',
        'This confirmation link is invalid or has already been used.': 'Ссылка для подтверждения недействительна или уже использована.',
        'Password changed. Sign in with your new password.': 'Пароль изменён. Войдите с новым паролем.',
        'Could not change the password. The link may have expired.': 'Не удалось сменить пароль. Возможно, ссылка устарела.',
        // Server messages (auth API)
        'Username already taken!': 'Это имя пользователя уже занято.',
        'Email already taken!': 'Этот email уже зарегистрирован.',
        'Incorrect email format': 'Неверный формат email.',
        'Password must be at least 6 characters': 'Пароль должен быть не короче 6 символов.',
        'Username must be 3-32 characters: letters, numbers, _ . -': 'Имя пользователя: 3–32 символа, буквы, цифры, _ . -',
        'Could not send the confirmation email. Try again later.': 'Не удалось отправить письмо с подтверждением. Попробуйте позже.',
        'Confirm your email first: open the link we sent you.': 'Сначала подтвердите email: откройте ссылку из письма.',
        'Registration successful. Verify your email.': 'Регистрация прошла успешно. Подтвердите email.',
        'Could not send the reset email.': 'Не удалось отправить письмо для сброса.',
        'Sign in to save bookmarks.': 'Войдите, чтобы сохранять закладки.',
        '{0} saved to bookmarks.': '{0} добавлено в закладки.',
        'Already bookmarked, or could not save.': 'Уже в закладках или не удалось сохранить.',
        'Could not save the bookmark.': 'Не удалось сохранить закладку.',
        'Sign in to keep a list of items you care about.': 'Войдите, чтобы вести список нужных предметов.',
        'Sign in': 'Войти',
        'No bookmarks yet. Open any item and press the bookmark icon.': 'Закладок пока нет. Откройте любой предмет и нажмите значок закладки.',
        'Remove bookmark': 'Удалить закладку',
        'Bookmark removed.': 'Закладка удалена.',
        'Could not remove bookmark.': 'Не удалось удалить закладку.',
        'Could not load bookmarks.': 'Не удалось загрузить закладки.',
        'Could not load that entry.': 'Не удалось загрузить запись.',
        'Link copied.': 'Ссылка скопирована.',
        'Nothing matches “{0}”.': 'Ничего не найдено по запросу «{0}».',
        'See all {0} results →': 'Все результаты ({0}) →',
        'No matches. Try a shorter or different word.': 'Ничего не найдено. Попробуйте более короткое или другое слово.',
        'Type at least two characters to search.': 'Введите хотя бы два символа.',
        'Search is unavailable right now.': 'Поиск сейчас недоступен.',

        // ----- Home: Badlands highlights -----
        'Bolt-action rifle': 'Винтовка с продольно-скользящим затвором', 'Automatic rifle': 'Автоматическая винтовка',
        'Submachine gun': 'Пистолет-пулемёт', 'Enduro motorbike': 'Эндуро-мотоцикл', 'Moped': 'Мопед',
        'New condition': 'Новое состояние', 'Sandstorm illness': 'Болезнь от песчаной бури',
        'Sandstorm condition': 'Состояние от песчаной бури', 'Melee weapon': 'Холодное оружие', 'Face covering': 'Платок на лицо',
        'Eye protection': 'Защита глаз', 'Vest': 'Жилет', 'Nasdara fruit': 'Фрукт Насдары', 'Yogurt drink': 'Йогуртовый напиток',
        'Canned sweet': 'Сладкие консервы',

        // ----- Maps -----
        'Chernarus': 'Чернарусь', 'Livonia': 'Ливония', 'Sakhal': 'Сахал', 'Nasdara': 'Насдара',
        'NEW · 1.30': 'НОВАЯ · 1.30', 'NEW': 'НОВАЯ', 'area': 'площадь', 'items spawn': 'предметов', 'exclusive': 'уникальных',
        'base game': 'базовая игра', 'expansion': 'дополнение', 'Explore': 'Открыть',
        'Area': 'Площадь', 'Released': 'Выход', 'Access': 'Доступ', 'Climate': 'Климат',
        'Items that spawn': 'Предметов появляется', 'Exclusive items': 'Уникальных предметов', 'Max vehicles at once': 'Макс. транспорта одновременно',
        'Total': 'Всего', 'Previous': 'Назад', 'Next': 'Далее', 'Only on {0}': 'Только на карте {0}',
        'Loot data unavailable.': 'Данные о луте недоступны.',
        'Everything that spawns here also spawns on at least one other map.': 'Всё, что появляется здесь, есть ещё хотя бы на одной карте.',

        // ----- Shooting range -----
        'Head': 'Голова', 'Torso': 'Торс', 'Right arm': 'Правая рука', 'Left arm': 'Левая рука',
        'Right hand': 'Правая кисть', 'Left hand': 'Левая кисть', 'Right leg': 'Правая нога', 'Left leg': 'Левая нога',
        'Right foot': 'Правая стопа', 'Left foot': 'Левая стопа',
        'Arms': 'Руки', 'Hands': 'Кисти', 'Legs': 'Ноги', 'Feet': 'Стопы',
        'Headgear': 'Головной убор', 'Mask': 'Маска', 'Top': 'Верх', 'Boots': 'Обувь',
        'Pristine': 'Идеальное', 'Worn': 'Изношенное', 'Damaged': 'Повреждённое', 'Badly damaged': 'Сильно повреждённое', 'Ruined': 'Испорченное',
        'Already dead: only the gear takes damage.': 'Уже мёртв: урон получает только снаряжение.',
        'destroyed': 'уничтожено',
        'New bleeding wound ({0} ml/min)': 'Новое кровотечение ({0} мл/мин)',
        'Armor stopped the bleeding': 'Броня остановила кровотечение',
        'Wound already bleeding here': 'Здесь уже кровоточит рана',
        'Legs broken': 'Ноги сломаны',
        '☠ Survivor is dead': '☠ Выживший мёртв',
        'Survivor is unconscious': 'Выживший без сознания',
        '{0} pellets': 'дробин: {0}',
        'm': 'м', 'm/s': 'м/с', 'ml/min': 'мл/мин',
        'No clothing over this part.': 'На этой части тела нет одежды.',
        '{0} (ruined, no protection)': '{0} (испорчено, не защищает)',
        'Hit through {0} (no ballistic protection).': 'Попадание сквозь {0} (без баллистической защиты).',
        'Armor: {0} blocked {1} health, {2} shock, {3} of bleeding.': 'Броня: {0} поглотила {1} урона здоровью, {2} шока и {3} кровотечения.',
        '☠ Bled out at {0}': '☠ Истёк кровью на {0}',
        'Woke up at {0}': 'Пришёл в себя на {0}',
        'Passed out from blood loss at {0}': 'Потерял сознание от кровопотери на {0}',
        'Pause': 'Пауза', 'Let time pass': 'Запустить время',
        'Dead': 'Мёртв', 'Seriously injured': 'Тяжело ранен', 'Moderately injured': 'Ранен', 'Slightly injured': 'Легко ранен', 'Healthy': 'Здоров',
        'Fatal': 'Смертельно', 'Critical, unconscious': 'Критично, без сознания', 'Very low': 'Очень мало', 'Low': 'Мало',
        'Slightly low': 'Чуть ниже нормы', 'Normal': 'Норма',
        'Knocked out': 'Без сознания', 'Groggy': 'Оглушён', 'Shaken': 'Потрясён', 'Steady': 'В норме',
        'losing {0} ml/min': 'теряет {0} мл/мин',
        'Unconscious': 'Без сознания', 'Alive · broken legs': 'Жив · сломаны ноги', 'Alive': 'Жив',
        'shot': 'выстрел', 'shots': 'выстрелов',
        '{0} ml/min · clots in {1}': '{0} мл/мин · свернётся через {1}',
        'No bleeding.': 'Кровотечения нет.',
        'Broken': 'Сломана', 'Wrecked': 'Разбита',
        'Nothing worn': 'Ничего не надето',
        'Projectile protection': 'Защита от пуль',
        'no bleeding': 'без кровотечения', 'bleeding': 'кровотечения', 'health': 'здоровья', 'shock': 'шока',
        'No ballistic protection': 'Нет баллистической защиты',
        '— none —': '— ничего —', 'Armored': 'Бронированное', 'Regular': 'Обычное',
        'Naked': 'Без одежды', 'Fresh spawn': 'Свежий спавн', 'Geared': 'Экипирован', 'Press': 'Пресса', 'Knight': 'Рыцарь',
        '× {0} of {1} pellets': '× {0} из {1} дробин', 'per hit': 'за попадание', 'Speed at {0} m': 'Скорость на {0} м',
        '{0} of muzzle speed': '{0} от начальной скорости', 'Single': 'Одиночный',
        'Bleed chance': 'Шанс кровотечения',
        'Gear repaired to pristine': 'Снаряжение починено до идеального',
        'Bandaged: {0}': 'Перевязано: {0}',
        'Epinephrine: shock restored': 'Адреналин: шок восстановлен',
        ', still out from blood loss': ', но без сознания от кровопотери',
        'Blood bag: +{0} ml': 'Пакет крови: +{0} мл',
        'Could not load range data.': 'Не удалось загрузить данные тира.',
        'Body part': 'Часть тела', 'Health ×': 'Здоровье ×', 'Shock ×': 'Шок ×', 'Bleed ml/min': 'Кровь мл/мин', 'Covered by': 'Чем закрыта',
        'HOW_TEXT': '<p><b>Каждое попадание</b> берёт урон патрона по здоровью, шоку и крови из конфига игры, умножает его на скорость пули, оставшуюся на этой дистанции ' +
            '(сопротивление воздуха её снижает), затем на защиту брони над частью тела и на множитель самой части тела.</p>' +
            '<p><b>Броня</b> использует коэффициенты защиты от пуль каждого предмета. Плитник пропускает 30% урона здоровью и 60% шока и полностью останавливает кровотечение. ' +
            'Слои перемножаются. Каждое попадание изнашивает снаряжение: внешний слой получает полный урон здоровью от патрона, слой под ним только то, что прошло. <b>Испорченный</b> предмет больше не защищает.</p>' +
            '<p><b>Кровотечение:</b> пуля всегда ранит, если её не остановила броня. Раны кровоточат по 36 мл/мин (кисти, стопы), 216 (руки, ноги) или 360 (голова, торс) и сворачиваются через 5 минут. ' +
            'Ниже 3000 мл крови вы теряете сознание, ниже 2500 умираете. При шоке ниже 25 вы без сознания, пока он не поднимется выше 50. ' +
            'Если нога или стопа опускается до 0, <b>ноги сломаны</b>.</p>' +
            '<p><b>Дробовик:</b> картечь выпускает 8 дробин; дальше примерно 15 м попадает меньше.</p>'
    };

    function t(key) {
        let s = lang === 'ru' && Object.prototype.hasOwnProperty.call(RU, key) ? RU[key] : key;
        const args = arguments;
        if (args.length > 1) s = s.replace(/\{(\d)\}/g, (m, i) => (args[+i + 1] !== undefined ? args[+i + 1] : m));
        return s;
    }

    /** Russian plural form: plural(5, 'выстрел', 'выстрела', 'выстрелов'). English uses one/many. */
    function plural(n, one, few, many) {
        if (lang !== 'ru') return n === 1 ? one : (many || few);
        const a = Math.abs(n) % 100, b = a % 10;
        if (a > 10 && a < 20) return many;
        if (b === 1) return one;
        if (b >= 2 && b <= 4) return few;
        return many;
    }

    // Loot tier, rarity and map lists come from the database in a fixed vocabulary ("Tier 2-4 · Very Rare", "Chernarus, Livonia").
    const VOCAB = [
        [/\bExtremely rare\b/gi, 'крайне редко'], [/\bVery rare\b/gi, 'очень редко'], [/\bVery common\b/gi, 'очень часто'],
        [/\bUncommon\b/gi, 'нечасто'], [/\bCommon\b/gi, 'часто'], [/\bRare\b/gi, 'редко'], [/\bUnique\b/gi, 'уникальный'],
        [/\bTier\b/g, 'Уровень'], [/ to /g, ' – '],
        [/\bChernarus\b/g, 'Чернарусь'], [/\bLivonia\b/g, 'Ливония'], [/\bSakhal\b/g, 'Сахал'], [/\bNasdara\b/g, 'Насдара']
    ];
    function vocab(s) {
        if (lang !== 'ru' || s === null || s === undefined) return s;
        let out = String(s);
        VOCAB.forEach(([re, ru]) => { out = out.replace(re, ru); });
        // Capitalise a value that now starts with a lower-case Russian word ("редко" -> "Редко").
        return out.charAt(0).toUpperCase() + out.slice(1);
    }

    function setLanguage(code) {
        try { document.cookie = 'dz-lang=' + code + '; path=/; max-age=31536000; SameSite=Lax'; } catch (e) { /* ignore */ }
        const url = new URL(location.href);
        url.searchParams.delete('lang');
        // Same address (or one that differs only by #hash) would not reload, so force it.
        if (url.toString() === location.href) location.reload();
        else location.replace(url.toString());
    }

    document.addEventListener('DOMContentLoaded', () => {
        const btn = document.getElementById('langToggle');
        if (btn) btn.addEventListener('click', () => setLanguage(btn.dataset.lang || 'en'));
    });

    window.DZ_I18N = { lang, t, plural, vocab, setLanguage };
})();
