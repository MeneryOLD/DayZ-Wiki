# Builds dataset.json from: types.json (official CE, all 4 maps), cargo.json + pages/ (dayz.wiki.gg), manual.json (1.30 gaps)
import sys, json, re, os, collections
sys.path.insert(0, os.path.dirname(__file__))
from wikiparse import find_infobox, clean

B = sys.argv[1]
types = json.load(open(f'{B}/types.json'))
tlow = {k.lower(): k for k in types}
cargo = json.load(open(f'{B}/cargo.json', encoding='utf-8'))
idx = json.load(open(f'{B}/pages/index.json', encoding='utf-8'))
manual = json.load(open(f'{B}/manual.json', encoding='utf-8'))
MAPS = [('chernarus', 'Chernarus'), ('livonia', 'Livonia'), ('sakhal', 'Sakhal'), ('nasdara', 'Nasdara')]
report = collections.defaultdict(list)

def page(title):
    v = idx.get(title)
    if not v: return '', {}
    txt = open(f'{B}/pages/' + v['file'], encoding='utf-8').read()
    return txt, find_infobox(txt)[1]

def classnames(*vals):
    out = []
    for v in vals:
        if not v: continue
        v = re.sub(r'<[^>]+>', ' ', v)
        out += [x for x in re.split(r'[\s,;/]+', v) if re.fullmatch(r'[A-Za-z][A-Za-z0-9_]+', x)]
    return list(dict.fromkeys(out))

def ce_info(cns):
    hits = [types[tlow[c.lower()]] for c in cns if c.lower() in tlow]
    if not hits: return None
    maps, tiers = set(), set()
    for h in hits:
        for m, d in h['maps'].items():
            maps.add(m); tiers |= set(d['tiers'])
    tnums = sorted(int(t[-1]) for t in tiers if re.fullmatch(r'Tier\d', t))
    special = sorted(t for t in tiers if not re.fullmatch(r'Tier\d', t))
    tier = ''
    if tnums: tier = f'Tier {tnums[0]}' if tnums[0] == tnums[-1] else f'Tier {tnums[0]}-{tnums[-1]}'
    if special: tier = (tier + ' / ' if tier else '') + ', '.join(special)
    return {'maps': ', '.join(n for k, n in MAPS if k in maps), 'tier': tier}

ALIAS = {
 'KA-74':['AK74'],'KA-M':['AKM'],'CR-75':['CZ75'],'FX-45':['FNX45'],'Mlock-91':['Glock19','Glock_Tan'],'BK-18':['Izh18'],'CR-527':['CZ527'],
 'M70 Tundra':['Winchester70'],'Pioneer':['Scout','Scout_Chernarus','Scout_Livonia'],'Repeater Carbine':['Repeater'],'VS-89':['SV98'],
 'BK-12':['Izh18Shotgun'],'BK-133':['Mp133Shotgun'],'BK-43':['Izh43Shotgun'],'RAK-37':['PM73Rak'],'SG5-K':['MP5K'],'Fange':['FangeKnife'],
 'Kukri':['KukriKnife'],'Brass Knuckles':['BrassKnuckles_Dull','BrassKnuckles_Shiny'],'Birch Broom':['Broom'],'Signal Pistol':['FireworksLauncher'],
 '40mm Smoke Grenade':['Ammo_40mm_Smoke_White'],'Bolt':['Ammo_HuntingBolt'],'.357 Rounds':['Ammo_357'],'12ga Rubber Slugs':['Ammo_12gaRubberSlug'],
 'Claymore':['ClaymoreMine'],'Plastic Explosive':['Plastic_Explosive'],'6-M7 Frag Grenade':['M67Grenade'],'EGD-5 Frag Grenade':['RGD5Grenade'],
 'Land Mine':['LandMineTrap'],'PO-X Vial':['Grenade_ChemGas'],'8-M4 Flashbang':['FlashGrenade'],'1-M8 Smoke Grenade':['M18SmokeGrenade_White'],
 'EGD-2 Smoke Grenade':['RDG2SmokeGrenade_White'],'Alcoholic Tincture':['DisinfectantAlcohol'],'Chlorine Tablets':['PurificationTablets'],
 'Blood Bag Kit':['BloodBagEmpty'],'IV Start Kit':['StartKitIV'],'Medical Thermometer':['Thermometer'],'Multivitamin Pills':['VitaminBottle'],
 'Tetracycline Pills':['TetracyclineAntibiotics'],'Codeine Pills':['PainkillerTablets'],'Morphine Auto-Injector':['Morphine'],
 'Epinephrine Auto-Injector':['Epinephrine'], 'Canned Bacon':['TacticalBaconCan'],'Canned Baked Beans':['BakedBeansCan'],'Canned Crab':['CrabCan'],'Canned Peaches':['PeachesCan'],'Canned Pork':['PorkCan'],'Canned Sardines':['SardinesCan'],'Canned Spaghetti':['SpaghettiCan'],'Canned Tuna':['TunaCan'],'Liver Pate':['Pate'],'Strawberry Jam':['Marmalade'],'Cereals':['BoxCerealCrunchin'],'Plastic Bottle':['WaterBottle'],'Soda Can':['SodaCan_Cola','SodaCan_Pipsi','SodaCan_Spite','SodaCan_Kvass','SodaCan_Fronta','SodaCan_Takola','SodaCan_ZumZam'],'IV Saline Bag':['SalineBag'],'Burlap Sack':['BurlapSack'],
}
NOT_SPAWNING = {'Banana','Kiwi','Orange','Candy Cane','Zluta Klasik Soda','Zluta Kolaloka Soda','Zluta Malinovka Soda'}
GONE = re.compile(r'not implemented|not spawning|removed|planned|unreleased|not in game|unused|cut content|no longer|legacy|deprecated', re.I)
OBTAIN = re.compile(r'craft|obtain|harvest|skin|gut|fish|catch|pick|from |dismantl|quarter|tree|bush|cook|spawn|inventory|found|start|forag|plant|grow|animal|dig|loot|crash|convoy|zone|container|trader|sea|water|wreck', re.I)

def is_current(name, loc, cat, txt, cns):
    if name in NOT_SPAWNING: report['dropped_gone'].append(name); return None
    cns = list(dict.fromkeys(list(cns) + ALIAS.get(name, [])))
    guess = re.sub(r'[^A-Za-z0-9]', '', name)
    ce = ce_info(cns) or ce_info([guess])
    if ce:  # official CE spawn data is authoritative
        ce['cns'] = [c for c in cns if c.lower() in tlow] or ([tlow[guess.lower()]] if guess.lower() in tlow else cns)
        return ce
    if 'mushroom' in (name + ' ' + (cat or '')).lower() or 'Fruits' in (cat or ''): cns = []  # spawn from nature, not CE
    if GONE.search(loc or '') or GONE.search(cat or ''):
        report['dropped_gone'].append(name); return None
    if txt and re.search(r'\{\{\s*(Removed|Cut|Unreleased|Legacy)|\[\[Category:\s*(Removed|Cut|Unreleased|Planned)', txt, re.I):
        report['dropped_gone'].append(name); return None
    body = re.sub(r'\{\{.*?\}\}', '', (txt or '')[:4000], flags=re.S)
    if cns and not OBTAIN.search(loc or '') and not re.search(r'craft|obtain|made by|created', body, re.I):
        report['dropped_not_in_ce'].append(f'{name} {cns}'); return None
    return {'maps': '', 'tier': ''}

def num(v, f=float):
    m = re.search(r'-?\d+(?:\.\d+)?', (v or '').replace(',', ''))
    return f(float(m.group())) if m else None

def grams_to_kg(v):
    g = num(v)
    return round(g / 1000, 3) if g else None

def size(v):
    m = re.search(r'(\d+)\s*x\s*(\d+)', v or '')
    return f'{m.group(1)}x{m.group(2)}' if m else (clean(v) or None)

def slots(v):
    m = re.search(r'(\d+)\s*x\s*(\d+)', v or '')
    if m: return int(m.group(1)) * int(m.group(2))
    return num(v, int)

RARITY_LEVELS = ['Very Common', 'Common', 'Uncommon', 'Rare', 'Very Rare', 'Extremely Rare', 'Unique', 'Crafted']
def rar(v):
    v = clean(v).replace("'", '').strip()
    if not v or v.upper() in ('N/A', 'NONE', 'UNKNOWN') or v.isdigit(): return None
    found = re.findall(r'Very Common|Extremely Rare|Very Rare|Uncommon|Common|Rare|Unique|Crafted', v)
    if len(found) >= 2:
        found = sorted(set(found), key=RARITY_LEVELS.index)
        return found[0] + ' to ' + found[-1]
    return v
def tier_text(ce, rarity):
    parts = [p for p in [ce.get('tier'), rarity] if p and p.upper() != 'N/A']
    return ' · '.join(parts) or None

def base(name, cns, desc, loc, rarity, ce):
    cns = ce.get('cns') or cns
    return {'name': name, 'englishName': name, 'altName': ', '.join(cns) or None, 'description': desc or None,
            'location': loc or None, 'rarity': rarity, 'maps': ce['maps'] or None, 'classname': (cns[0] if cns else None)}

data = collections.defaultdict(list)

# ---------- Ammunition ----------
AMMO_TYPE = {'Pistol': 'Pistol', 'Intermediate': 'Intermediate', 'Full-power': 'Full-power', 'Shell': 'Shotgun Shells',
             'Grenade': '40mm Grenades', 'Archery': 'Arrows and Bolts'}
for r in cargo['Ammunition']:
    t = AMMO_TYPE.get(r['Category']) or ('Intermediate' if '9x39' in r['Name'] else None)
    txt, p = page(r['Page'])
    cns = classnames(r['Classname'], p.get('classname'))
    if not t: report['ammo_skipped_cat'].append(f"{r['Name']} [{r['Category']}]"); continue
    loc = clean(r['Location'] or p.get('location'))
    ce = is_current(r['Name'], loc, r['Category'], txt, cns)
    if ce is None: continue
    rarity = rar(r['Rarity'] or p.get('rarity'))
    d = base(r['Name'], cns, clean(p.get('description')), loc, rarity, ce)
    d.update(type=t, size=clean(r['Size']) or None, weight=grams_to_kg(r['Weight']), tier=tier_text(ce, rarity),
             healthDamage=num(r['healthDmg']), bloodDamage=num(r['bloodDmg']), shockDamage=num(r['shockDmg']),
             usedBy=clean(r['Weapon']) or None, source='ammunition')
    data['ammunition'].append(d)
ammo_by_name = {a['name'].lower(): a for a in data['ammunition']}

# ---------- Weapons: ranged ----------
def wtype(cat):
    c = cat.lower()
    if 'handgun' in c: return 'Handguns'
    if 'submachine' in c: return 'Submachine Guns'
    if 'shotgun' in c: return 'Shotguns'
    if 'machine gun' in c: return 'Assault Rifles'
    if 'assault' in c or 'automatic rifle' in c or 'battle' in c: return 'Assault Rifles'
    if 'marksman' in c or 'semi-automatic' in c: return 'Marksman Rifles'
    if 'bolt' in c or 'lever' in c or 'breech' in c or 'break' in c or c == 'rifle': return 'Rifles'
    if 'launcher' in c or 'archery' in c or 'non-lethal' in c or c == 'other': return 'Launchers and Bows'
    return None
mag_rows = []
for r in cargo['Ranged']:
    txt, p = page(r['Page'])
    t = wtype(r['Category'])
    cns = classnames(r['Classname'], p.get('classname'))
    loc = clean(r['Location'] or p.get('location'))
    if not t: report['weapon_skipped_cat'].append(r['Name']); continue
    ce = is_current(r['Name'], loc, r['Category'], txt, cns)
    if ce is None: continue
    rarity = rar(r['Rarity'] or p.get('rarity'))
    ammo_names = [a.strip() for a in clean(r['Ammunition'] or p.get('ammunition')).split(',') if a.strip()]
    cal = next((ammo_by_name[a.lower()] for a in ammo_names if a.lower() in ammo_by_name), None)
    if ammo_names and not cal: report['weapon_no_caliber'].append(f"{r['Name']} {ammo_names}")
    d = base(r['Name'], cns, clean(p.get('description')), loc, rarity, ce)
    d.update(type=t, size=size(r['Size'] or p.get('size')), weight=grams_to_kg(r['Weight'] or p.get('weight')),
             tier=tier_text(ce, rarity), damage=int(cal['healthDamage']) if cal and cal['healthDamage'] else None,
             durability=num(r['Hitpoints'] or p.get('hitpoints'), int), caliberName=cal['name'] if cal else None,
             ammunition=', '.join(ammo_names) or None, fireModes=clean(r['Mode'] or p.get('mode')) or None,
             magazines=clean(r['Magazines'] or p.get('magazines')) or None, source='weapons')
    data['weapons'].append(d)
    for m in (d['magazines'] or '').split(','):
        if m.strip(): mag_rows.append((m.strip(), d['name'], ammo_names[0] if ammo_names else None))

# ---------- Weapons: melee ----------
for r in cargo['Melee']:
    txt, p = page(r['Page'])
    cns = classnames(r['Classname'], p.get('classname'))
    loc = clean(r['Location'] or p.get('location'))
    ce = is_current(r['Name'], loc, '', txt, cns)
    if ce is None: continue
    rarity = rar(r['Rarity'] or p.get('rarity'))
    d = base(r['Name'], cns, clean(p.get('description')), loc, rarity, ce)
    d.update(type='Melee', size=size(r['Size'] or p.get('size')), weight=grams_to_kg(r['Weight'] or p.get('weight')),
             tier=tier_text(ce, rarity), damage=num(r['nhealth'] or p.get('nhealth'), int),
             heavyDamage=num(r['hhealth'] or p.get('hhealth'), int), bleedChance=clean(r['Type'] or p.get('bleed')) or None,
             durability=num(p.get('durability'), int), source='weapons')
    data['weapons'].append(d)

# ---------- Magazines (modules) from weapon pages + CE ----------
seen = {}
for m, w, ammo in mag_rows:
    key = re.sub(r'\s+', ' ', m.lower().replace('rnd', 'rd').replace('rds', 'rd'))
    cap = num(m, int)
    if key in seen: seen[key]['usedBy'].add(w); continue
    seen[key] = {'name': m, 'usedBy': {w}, 'capacity': cap, 'ammo': ammo}
MAG_FIX = {'32 rd mp18 drum mag': None, 'luger mag': None, '12rd ij-70 mag': '8rd IJ-70 Mag'}  # duplicates / wrong capacity vs CE
for k in list(seen):
    if k in MAG_FIX:
        if MAG_FIX[k] is None: del seen[k]
        else: seen[k]['name'] = MAG_FIX[k]; seen[k]['capacity'] = num(MAG_FIX[k], int)
for m in seen.values():
    data['modules'].append({'name': m['name'], 'englishName': m['name'], 'altName': None, 'type': 'Magazines',
        'capacity': m['capacity'], 'description': f"Magazine for {', '.join(sorted(m['usedBy']))}" + (f" ({m['ammo']})." if m['ammo'] else '.'),
        'usedBy': ', '.join(sorted(m['usedBy'])), 'source': 'modules'})

# ---------- Attachments ----------
ranged_names = {r['Page'] for r in cargo['Ranged']} | {r['Page'] for r in cargo['Melee']}
wa = open(f'{B}/wiki/Weapon_Attachments.txt', encoding='utf-8').read()
sections = re.split(r'\n==\s*([^=\n]+?)\s*==\n', wa)
SEC = {'Buttstocks': 'Buttstocks', 'Handguards': 'Handguards', 'Optics': 'Optics', 'Illumination': 'Illumination',
       'Muzzle Attachments': 'Muzzle Devices', 'Bayonets': 'Bayonets', 'Wraps': 'Wraps'}
for i in range(1, len(sections) - 1, 2):
    t = SEC.get(sections[i].strip())
    if not t: continue
    names = {l.strip() for l in re.findall(r'\[\[([^\]\[|#:]+)(?:\|[^\]\[]*)?\]\]', sections[i + 1])}
    for n in sorted(names):
        txt, p = page(n)
        if not p or 'category' not in p and 'usedby' not in p: continue
        if re.search(r'\{\{\s*InfoboxWeapon', txt) or n in ranged_names: continue
        cns = classnames(p.get('classname'))
        loc = clean(p.get('location'))
        ce = is_current(n, loc, clean(p.get('category')), txt, cns)
        if ce is None: continue
        rarity = rar(p.get('rarity'))
        d = base(n, cns, clean(p.get('description')), loc, rarity, ce)
        d.update(type=t, size=size(p.get('size')), weight=grams_to_kg(p.get('weight')), tier=tier_text(ce, rarity),
                 magnification=num(p.get('zoom')), durability=num(p.get('durability'), int),
                 usedBy=clean(p.get('usedby')) or None, source='modules')
        data['modules'].append(d)

# ---------- Clothing / armor ----------
def ctype(cat, slot):
    c = (clean(cat) + ' ' + (slot or '')).lower()
    for k, v in [('backpack', 'Backpacks'), ('helmet', 'Helmets'), ('hat', 'Hats'), ('head', 'Hats'), ('mask', 'Masks'),
                 ('face', 'Masks'), ('eye', 'Eyewear'), ('hand', 'Gloves'), ('glove', 'Gloves'), ('vest', 'Vests'),
                 ('pant', 'Pants'), ('leg', 'Pants'), ('shoe', 'Shoes'), ('feet', 'Shoes'), ('boot', 'Shoes'),
                 ('belt', 'Belts'), ('holster', 'Belts'), ('shirt', 'Shirts and Jackets'), ('jacket', 'Shirts and Jackets'),
                 ('top', 'Shirts and Jackets'), ('body', 'Shirts and Jackets'), ('camouflage', 'Ghillie and Camo'),
                 ('accessor', 'Accessories'), ('arm', 'Accessories')]:
        if k in c: return v
    return None
for r in cargo['Clothing']:
    txt, p = page(r['Page'])
    t = ctype(r['Category'], r['inventorySlot'])
    if not t: report['clothing_skipped_cat'].append(f"{r['Name']} [{r['Category']}]"); continue
    cns = classnames(r['Classname'], p.get('classname'))
    loc = clean(r['Location'] or p.get('location'))
    ce = is_current(r['Name'], loc, r['Category'], txt, cns)
    if ce is None: continue
    rarity = rar(r['Rarity'] or p.get('rarity'))
    prot = []
    for lbl, k in [('Projectile', 'projH'), ('Melee', 'meleeH'), ('Infected', 'infH')]:
        v = num(r.get(k))
        if v is not None and v < 1: prot.append(f'{lbl} {round((1 - v) * 100)}%')
    heat = num(r['heatIsolation'])
    d = base(r['Name'], cns, clean(p.get('description')), loc, rarity, ce)
    d.update(type=t, size=size(r['Size']), weight=grams_to_kg(r['Weight']), durability=num(r['Durability'], int),
             insulation=f'{heat:g}' if heat is not None else None, capacity=slots(r['Capacity']),
             additionalSlots=clean(p.get('attachments')) or None, protection=', '.join(prot) or None,
             tier=tier_text(ce, rarity), source='armors')
    data['armors'].append(d)

# ---------- Food ----------
FOOD = {'Canned Goods': 'Canned Goods', 'Dry Goods': 'Dry Goods and Snacks', 'Meats': 'Meat and Fish', 'Fruits': 'Fruits and Vegetables',
        'Vegetables': 'Fruits and Vegetables', 'Berries': 'Fruits and Vegetables', 'Mushrooms': 'Mushrooms', 'Drinks': 'Drinks', 'Other': 'Other'}
for r in cargo['Food']:
    txt, p = page(r['Page'])
    t = FOOD.get(clean(r['Category']), 'Other')
    cns = classnames(r['Classname'], p.get('classname'))
    loc = clean(r['Location'] or p.get('location'))
    ce = is_current(r['Name'], loc, r['Category'], txt, cns)
    if ce is None: continue
    rarity = rar(r['Rarity'] or p.get('rarity'))
    w = grams_to_kg(r['Weight'])
    if not w and num(r['Quantity']) and num(r['weightPerQuant']): w = round(num(r['Quantity']) * num(r['weightPerQuant']) / 1000, 3)
    d = base(r['Name'], cns, clean(p.get('description')), loc, rarity, ce)
    d.update(type=t, size=size(r['Size']), weight=w, baseCalories=num(r['Energy'], int), baseThirst=num(r['Water'], int),
             stomach=num(r['Stomach'], int), states=clean(r['Variants']) or None, tier=tier_text(ce, rarity), source='foods')
    data['foods'].append(d)

# ---------- Medical ----------
ms = open(f'{B}/wiki/Medical_Supplies.txt', encoding='utf-8').read()
parts = re.split(r'\n==\s*([^=\n]+?)\s*==\n', ms)
for i in range(1, len(parts) - 1, 2):
    sec = parts[i].strip()
    if 'Protective' in sec: continue
    for n in re.findall(r'\|\s*link\s*=\s*([^\n|]+)', parts[i + 1]):
        n = n.strip(); txt, p = page(n)
        if not p: report['medical_missing'].append(n); continue
        cns = classnames(p.get('classname'))
        loc = clean(p.get('location'))
        ce = is_current(n, loc, '', txt, cns)
        if ce is None: continue
        rarity = rar(p.get('rarity'))
        d = base(n, cns, clean(p.get('description')), loc, rarity, ce)
        d.update(type=sec, size=size(p.get('size')), weight=grams_to_kg(p.get('weight')), tier=tier_text(ce, rarity),
                 use=clean(p.get('primary')) or None, uses=clean(p.get('numuse')) or None, source='medicaments')
        data['medicaments'].append(d)

# ---------- Explosives ----------
for n in re.findall(r'link=([^\]|]+)\]\]', open(f'{B}/wiki/List_of_explosives.txt', encoding='utf-8').read()):
    n = n.replace('_', ' ').strip(); txt, p = page(n)
    if not p: report['explosive_missing'].append(n); continue
    cns = classnames(p.get('classname'))
    loc = clean(p.get('location'))
    ce = is_current(n, loc, '', txt, cns)
    if ce is None: continue
    rarity = rar(p.get('rarity'))
    d = base(n, cns, clean(p.get('description')), loc, rarity, ce)
    d.update(type=clean(p.get('etype')) or None, delivery=clean(p.get('delivery')) or None, size=size(p.get('size')),
             weight=grams_to_kg(p.get('weight')), tier=tier_text(ce, rarity), source='explosives')
    data['explosives'].append(d)

# ---------- Vehicles ----------
for n in ['Olga 24', 'Gunter 2', 'Ada 4x4', 'Sarka 120', 'M1025', 'M3S', 'Rubber Boat']:
    txt, p = page(n)
    if not p: report['vehicle_missing'].append(n); continue
    cns = classnames(p.get('classname'))
    rarity = rar(p.get('rarity'))
    data['cars'].append({**base(n, cns, None, clean(p.get('location')), rarity, {'maps': '', 'tier': ''}),
        'seats': clean(p.get('seats')) or None, 'capacity': clean(p.get('capacity')) or None,
        'fuelTank': num(p.get('fueltank') or p.get('fuelcapacity'), int), 'topSpeed': num(p.get('topspeed'), int),
        'tier': rarity, 'type': 'Boat' if 'Boat' in n else 'Car', 'source': 'cars'})

# ---------- Diseases ----------
cs = open(f'{B}/wiki/Character_Status.txt', encoding='utf-8').read()
tbl = cs[cs.find('=== Illness & Disease'):cs.find('==== Stuffed Stomach')]
for row in tbl.split('\n|-')[1:]:
    m = re.search(r'^\s*[^\n]*\n!\s*(.+)', row)
    if not m: continue
    name = clean(m.group(1))
    cells = re.split(r'\n\|', row[m.end():])[1:]
    cells = [clean(c.replace('\n*', ';').replace('\n', ' ')).strip('; ') for c in cells]
    while len(cells) < 4: cells.append('')
    data['diseases'].append({'name': name, 'englishName': name, 'altName': None, 'causes': cells[0], 'symptoms': cells[1],
        'treatment': cells[2], 'prevention': cells[3] or None, 'description': None, 'source': 'diseases'})
# Heat Stroke from its page
txt, p = page('Heat Stroke')
if p:
    data['diseases'].append({'name': 'Heat Stroke', 'englishName': 'Heat Stroke', 'altName': 'Heatstroke', 'causes': clean(p['causes']),
        'symptoms': clean(p['symptoms']), 'treatment': clean(p['treatment']), 'prevention': clean(p['prevention']).replace(', ', '; '),
        'description': None, 'source': 'diseases'})

# ---------- Manual (1.30 content not yet on the wiki) ----------
for table, rows in manual.items():
    if table.startswith('_'): continue
    for r in rows:
        if any(x['englishName'].lower() == r['englishName'].lower() for x in data[table]): continue
        cns = r.pop('_cns', None)
        if cns:
            ce = ce_info(cns)
            if not ce: report['manual_not_in_ce'].append(r['englishName']); continue
            r.setdefault('maps', ce['maps']); r.setdefault('tier', ce['tier'] or None)
            r.setdefault('altName', ', '.join(cns)); r.setdefault('classname', cns[0])
        r.setdefault('source', table)
        data[table].append(r)

# vehicle maps/counts from events.xml is merged in manual step (cars) -- apply here
for c in data['cars']:
    ev = manual['_vehicle_maps'].get(c['englishName'])
    if ev: c['maps'] = ev
for k in list(data): data[k].sort(key=lambda d: (d.get('type') or '', d['englishName']))
json.dump(data, open(f'{B}/dataset.json', 'w', encoding='utf-8'), indent=1, ensure_ascii=False)
print({k: len(v) for k, v in data.items()})
for k, v in report.items(): print('##', k, len(v)); print('   ', v[:200])
