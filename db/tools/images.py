# Finds a dayz.wiki.gg image for every dataset entry and resolves thumbnail URLs.
# usage: images.py <workdir>   -> writes <workdir>/images.json  {"source|name": {"file":..,"thumb":..,"how":..}}
import sys, os, re, json, time, urllib.request, urllib.parse
sys.path.insert(0, os.path.dirname(__file__))
from wikiparse import find_infobox

W = sys.argv[1]
UA = {'User-Agent': 'Mozilla/5.0 (wiki-reader)'}
data = json.load(open(f'{W}/dataset.json', encoding='utf-8'))
cargo = json.load(open(f'{W}/cargo.json', encoding='utf-8'))
idx = json.load(open(f'{W}/pages/index.json', encoding='utf-8'))
out_path = f'{W}/images.json'
out = json.load(open(out_path, encoding='utf-8')) if os.path.exists(out_path) else {}

def api(params):
    params['format'] = 'json'
    req = urllib.request.Request('https://dayz.wiki.gg/api.php?' + urllib.parse.urlencode(params), headers=UA)
    return json.load(urllib.request.urlopen(req, timeout=60))

def first_file(v):
    if not v: return None
    v = re.sub(r'<[^>]+>', ',', v)
    for part in re.split(r'[,;\n]', v):
        part = part.strip().strip('[]').replace('File:', '').replace('Image:', '').split('|')[0].strip()
        if re.search(r'\.(png|jpe?g|webp|gif)$', part, re.I): return part
    return None

by_name = {}
for rows in cargo.values():
    for r in rows:
        f = first_file(r.get('mainImage')) or first_file(r.get('Image')) or first_file(r.get('Images'))
        if f: by_name.setdefault(r['Name'].lower(), f); by_name.setdefault(r['Page'].lower(), f)

def page_file(name):
    v = idx.get(name)
    if not v: return None
    _, p = find_infobox(open(f'{W}/pages/' + v['file'], encoding='utf-8').read())
    return first_file(p.get('image')) or first_file(p.get('images')) or first_file(p.get('img'))

GENERIC = re.compile(r'disease icon|placeholder|noimage|no_image', re.I)
want = {}
for table, rows in data.items():
    for it in rows:
        key = f"{table}|{it['englishName']}"
        if key in out: continue
        f = by_name.get(it['englishName'].lower()) or page_file(it['englishName'])
        if f and not GENERIC.search(f): want[key] = f

# resolve thumbnails in batches of 50
files = sorted(set(want.values()))
info = {}
for i in range(0, len(files), 50):
    chunk = files[i:i + 50]
    d = api({'action': 'query', 'prop': 'imageinfo', 'iiprop': 'url', 'iiurlwidth': 256, 'titles': '|'.join('File:' + f for f in chunk)})['query']
    norm = {n['to']: n['from'] for n in d.get('normalized', [])}
    for p in d['pages'].values():
        ii = p.get('imageinfo')
        if ii:
            src = norm.get(p['title'], p['title']).replace('File:', '')
            info[src] = ii[0].get('thumburl') or ii[0]['url']
    time.sleep(1)
for key, f in want.items():
    if f in info: out[key] = {'file': f, 'thumb': info[f], 'how': 'infobox'}

# fallback: search the File namespace by name (reviewed manually afterwards)
for table, rows in data.items():
    for it in rows:
        key = f"{table}|{it['englishName']}"
        if key in out: continue
        q = re.sub(r'\b(\d+rd|rnd|mag)\b', '', it['englishName'], flags=re.I).strip() if table == 'modules' else it['englishName']
        try:
            res = api({'action': 'query', 'list': 'search', 'srnamespace': 6, 'srsearch': q + (' magazine' if 'Mag' in it['englishName'] else ''), 'srlimit': 1})['query']['search']
        except Exception:
            res = []
        if res:
            title = res[0]['title']
            d = api({'action': 'query', 'prop': 'imageinfo', 'iiprop': 'url', 'iiurlwidth': 256, 'titles': title})['query']
            p = next(iter(d['pages'].values()))
            if p.get('imageinfo'):
                out[key] = {'file': title.replace('File:', ''), 'thumb': p['imageinfo'][0].get('thumburl') or p['imageinfo'][0]['url'], 'how': 'search'}
        time.sleep(0.4)

json.dump(out, open(out_path, 'w', encoding='utf-8'), indent=1, ensure_ascii=False)
total = sum(len(v) for v in data.values())
print('resolved', len(out), 'of', total)
print('via search:', [(k, v['file']) for k, v in out.items() if v['how'] == 'search'])
print('missing:', [f"{t}|{it['englishName']}" for t, rows in data.items() for it in rows if f"{t}|{it['englishName']}" not in out])
