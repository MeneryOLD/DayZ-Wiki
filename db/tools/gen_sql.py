import sys, json, re
d = json.load(open(sys.argv[1], encoding='utf-8')); out = sys.argv[2]
import os
IMG = json.load(open(os.path.join(os.path.dirname(sys.argv[1]), 'image_paths.json'), encoding='utf-8')) if os.path.exists(os.path.join(os.path.dirname(sys.argv[1]), 'image_paths.json')) else {}
def snake(k): return re.sub(r'([A-Z])', lambda m: '_' + m.group(1).lower(), k)
def lit(v):
    if v is None: return 'NULL'
    if isinstance(v, bool): return '1' if v else '0'
    if isinstance(v, (int, float)): return repr(v)
    v = str(v).replace('&#39;', "'").replace('&quot;', '"')
    return "'" + v.replace(chr(92), chr(92) * 2).replace("'", "''") + "'"
COMMON = ['name', 'englishName', 'altName', 'source', 'description', 'location', 'rarity', 'maps', 'classname', 'image']
COLS = {
 'ammunition': ['type', 'size', 'weight', 'tier', 'healthDamage', 'bloodDamage', 'shockDamage', 'usedBy'],
 'armors': ['type', 'size', 'weight', 'durability', 'insulation', 'capacity', 'additionalSlots', 'protection', 'tier'],
 'cars': ['type', 'capacity', 'fuelTank', 'topSpeed', 'tier', 'seats'],
 'diseases': ['causes', 'symptoms', 'treatment', 'prevention'],
 'explosives': ['type', 'delivery', 'size', 'weight', 'durability', 'tier'],
 'foods': ['type', 'baseCalories', 'baseThirst', 'stomach', 'states', 'tier', 'durability', 'size', 'weight'],
 'medicaments': ['type', 'tier', 'size', 'durability', 'weight', 'medicalUse', 'numberOfUses'],
 'modules': ['type', 'weight', 'size', 'tier', 'durability', 'magnification', 'capacity', 'usedBy'],
 'weapons': ['type', 'size', 'weight', 'tier', 'damage', 'heavyDamage', 'damageType', 'durability', 'ammunition', 'fireModes', 'magazines', 'caliber'],
}
RENAME = {'use': 'medicalUse', 'uses': 'numberOfUses', 'bleedChance': 'damageType'}
L = ['-- DayZ Wiki seed data, current for DayZ 1.30 (Badlands).',
     '-- Sources: BohemiaInteractive/DayZ-Central-Economy (spawn maps, loot tiers, class names),',
     '-- dayz.wiki.gg (stats, descriptions; CC BY-SA), official 1.30 patch notes.',
     '-- Load after the app has created its tables:  mysql -u<user> -p <db> < db/seed.sql', '',
     'SET NAMES utf8mb4;', 'SET FOREIGN_KEY_CHECKS = 0;']
for t in COLS: L.append(f'DELETE FROM {t};')
L.append('SET FOREIGN_KEY_CHECKS = 1;')
ammo_id = {}
for i, a in enumerate(d['ammunition'], 1): ammo_id[a['name']] = i
order = ['ammunition'] + [t for t in COLS if t != 'ammunition']
for t in order:
    cols = COMMON + COLS[t]
    rows = []
    for n, r in enumerate(d[t], 1):
        r = {RENAME.get(k, k): v for k, v in r.items()}
        r['source'] = t
        r['image'] = IMG.get(t + '|' + r['englishName'])
        if t == 'weapons': r['caliber'] = ammo_id.get(r.get('caliberName'))
        vals = [lit(r.get(c)) for c in cols]
        if t == 'ammunition': vals = [str(n)] + vals
        rows.append('(' + ', '.join(vals) + ')')
    colsql = (['id'] if t == 'ammunition' else []) + [snake(c) for c in cols]
    L.append(f'\nINSERT INTO {t} ({", ".join(colsql)}) VALUES\n' + ',\n'.join(rows) + ';')
# full-text search view used by SearchRepository; section = the tab the item appears under on its page
SECT = {'weapons': 'type', 'ammunition': 'type', 'armors': 'type', 'foods': 'type', 'medicaments': 'type', 'modules': 'type',
        'cars': "'Cars'", 'diseases': "'Diseases'", 'explosives': "'Explosives'"}
URL = {'medicaments': 'medication'}
sel = []
for t, sec in SECT.items():
    sel.append(f"SELECT name, english_name, alt_name, {sec} AS section, CONCAT('/{URL.get(t, t)}/', LOWER(REPLACE(english_name, ' ', '_'))) AS url, description, '{t}' AS source_table, image, maps FROM {t}")
L.append('\nCREATE OR REPLACE VIEW full_text_search AS\n' + '\nUNION ALL\n'.join(sel) + ';\n')
open(out, 'w', encoding='utf-8', newline='\n').write('\n'.join(L))
print({t: len(d[t]) for t in order})
