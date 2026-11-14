import sys, json, xml.etree.ElementTree as ET
out = {}
for mapname, path in [(a.split('=')[0], a.split('=')[1]) for a in sys.argv[2:]]:
    root = ET.parse(path).getroot()
    for t in root.findall('type'):
        n = t.get('name'); nom = int((t.findtext('nominal') or '0').strip() or 0)
        if nom <= 0: continue
        cat = t.find('category'); cat = cat.get('name') if cat is not None else ''
        tiers = sorted({v.get('name') for v in t.findall('value') if v.get('name')})
        usage = sorted({u.get('name') for u in t.findall('usage') if u.get('name')})
        e = out.setdefault(n, {'cat': cat, 'maps': {}, 'usage': set()})
        e['maps'][mapname] = {'nominal': nom, 'tiers': tiers}
        e['usage'].update(usage)
for e in out.values(): e['usage'] = sorted(e['usage'])
json.dump(out, open(sys.argv[1], 'w'), indent=0)
from collections import Counter
print(len(out), Counter(e['cat'] for e in out.values()))
