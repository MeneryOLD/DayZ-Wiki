# Applies manual image corrections, resolves thumbnails, downloads them and converts to WebP.
# usage: PYTHONPATH=<pylib> fetch_images.py <workdir> <static/img/items dir>
import sys, os, re, json, time, io, urllib.request, urllib.parse
from PIL import Image

W, OUT = sys.argv[1], sys.argv[2]
os.makedirs(OUT, exist_ok=True)
UA = {'User-Agent': 'Mozilla/5.0 (wiki-reader)'}
data = json.load(open(f'{W}/dataset.json', encoding='utf-8'))
imgs = json.load(open(f'{W}/images.json', encoding='utf-8'))

# Wrong guesses from the file search
for bad in ['modules|10rd Mag-C Mag', 'modules|20rd Mag-C Mag', 'modules|30rd Mag-C Mag', 'modules|40rd Mag-C Mag',
            'modules|30rd AUR Mag', 'armors|Knife Sheath', 'diseases|Common Cold']:
    imgs.pop(bad, None)

MANUAL = {
 'modules': {
  '10 Rnd Lee-Enfield Mag': 'Mag_LeeEnfield_10Rnd.png', '10rd CR-550 Mag': 'Mag_CZ550_10rnd.png', '10rd DMR Mag': 'Mag_M14_10Rnd.png',
  '20rd DMR Mag': 'Mag_M14_20Rnd.png', '10rd MK II Mag': 'Mag_MKII_10Rnd.png', '10rd VS-89 Mag': 'Mag_SV98_10Rnd.png',
  '10rd VSD Mag': 'Mag_SVD_10Rnd.png', '8rd IJ-70 Mag': 'Mag_IJ70_8Rnd.png', '15rd CR-75 Mag': 'Mag_CZ75_15Rnd.png',
  '15rd FX-45 Mag': 'Mag_FNX45_15Rnd.png', '15rd Mlock-91 Mag': 'Mag_Glock_15Rnd.png', '15rd RAK-37 Mag': 'Mag_PM73_15Rnd.png',
  '25rd RAK-37 Mag': 'Mag_PM73_25Rnd.png', '15rd SG5-K Mag': 'Mag_MP5_15Rnd.png', '30rd SG5-K Mag': 'Mag_MP5_30Rnd.png',
  '15rd Sporter 22 Mag': 'Mag_Ruger1022_15Rnd.png', '30rd Sporter 22 Mag': 'Mag_Ruger1022_30Rnd.png', '20rd CR-61 Mag': 'Mag_CZ61_20Rnd.png',
  '20rd Drum Vaiga Mag': 'Mag_Saiga_Drum20Rnd.png', '5rd Vaiga Mag': 'Mag_Saiga_5Rnd.png', '8rd Vaiga Mag': 'Mag_Saiga_8Rnd.png',
  '20rd LAR Mag': 'Mag_FAL_20Rnd.png', '20rd SCR 17 Mag': 'Mag_SCARH_20Rnd.png', '20rd SVAL Mag': 'Mag_VAL_20Rnd.png',
  '25rd LE-MAS Mag': 'Mag_FAMAS_25Rnd.png', '25rd USG-45 Mag': 'Mag_UMP_25Rnd.png', '30rd KA-101 Mag': 'Mag_AK101_30Rnd.png',
  '30rd KA-74 Mag': 'Mag_AK74_30Rnd.png', '45rd KA-74 Mag': 'Mag_AK74_45Rnd.png', '30rd KA-M Mag': 'Mag_AKM_30Rnd.png',
  '30rd KA-M Polymer Mag': 'Mag_AKM_Palm30Rnd.png', '75rd KA-M Drum Mag': 'Mag_AKM_Drum75Rnd.png', '30rd Standardized Mag': 'Mag_STANAG_30Rnd.png',
  '60rd Standardized Mag': 'Mag_STANAG_60Rnd.png', '30rd Vikhr Mag': 'Mag_Vikhr_30Rnd.png', '32rd MP-18 Mag': 'Mag_MP18_Drum32Rnd.png',
  '5rd CR-527 Mag': 'Mag_CZ527_5rnd.png', '5rd SSG 82 Mag': 'Mag_SSG82_5rnd.png', '64rd Bizon Mag': 'Mag_PP19_64Rnd.png',
  '7rd Kolt 1911 Mag': 'Mag_1911_7Rnd.png', '8rd Luger Mag': 'Mag_Luger_8Rnd.png', '8rd P1 Mag': 'Mag_P1_8Rnd.png',
  '10rd Mag-C Mag': 'Mag_CMAG_10Rnd.png', '20rd Mag-C Mag': 'Mag_CMAG_20Rnd.png', '30rd Mag-C Mag': 'Mag_CMAG_30Rnd.png',
  '40rd Mag-C Mag': 'Mag_CMAG_40Rnd.png', '30rd AUR Mag': 'Mag_Aug_30Rnd.png',
 },
 # In-game HUD status icons for illnesses
 'diseases': {
  'Brain Prion Disease': 'Gui_IconBacteria.png', 'Cholera': 'Gui_IconBacteria.png', 'Common Cold': 'Gui_IconBacteria.png',
  'Influenza': 'Gui_IconBacteria.png', 'Pneumonia': 'Gui_IconBacteria.png', 'Salmonellosis': 'Gui_IconBacteria.png',
  'Wound Infection': 'Gui_IconBacteria.png', 'Silicosis': 'Gui_IconBacteria.png', 'Irritated Eyes': 'Gui_IconBacteria.png',
  'Food Poisoning': 'IconPoisoned.png', 'Gas Poisoning': 'IconPoisoned.png', 'Heavy Metal Poisoning': 'IconPoisoned.png',
  'Hemolytic Reaction': 'IconBlood2_WARNING_PLUS.png', 'Heat Stroke': 'StatusTemp.png',
 },
}

def api(params):
    params['format'] = 'json'
    req = urllib.request.Request('https://dayz.wiki.gg/api.php?' + urllib.parse.urlencode(params), headers=UA)
    return json.load(urllib.request.urlopen(req, timeout=60))

want = {f'{t}|{n}': f for t, m in MANUAL.items() for n, f in m.items()}
files = sorted(set(want.values()))
info = {}
for i in range(0, len(files), 50):
    d = api({'action': 'query', 'prop': 'imageinfo', 'iiprop': 'url', 'iiurlwidth': 256, 'titles': '|'.join('File:' + f for f in files[i:i + 50])})['query']
    norm = {n['to']: n['from'] for n in d.get('normalized', [])}
    for p in d['pages'].values():
        if p.get('imageinfo'):
            src = norm.get(p['title'], p['title']).replace('File:', '')
            info[src] = p['imageinfo'][0].get('thumburl') or p['imageinfo'][0]['url']
    time.sleep(1)
for k, f in want.items():
    if f in info: imgs[k] = {'file': f, 'thumb': info[f], 'how': 'manual'}
    else: print('manual file not found:', f)

def slug(s): return re.sub(r'[^a-z0-9]+', '-', s.lower()).strip('-')

valid = {f"{t}|{it['englishName']}" for t, rows in data.items() for it in rows}
cache = {}
result = {}
for key in sorted(valid):
    if key not in imgs: continue
    thumb = imgs[key]['thumb']
    fname = slug(imgs[key]['file'].rsplit('.', 1)[0]) + '.webp'
    path = os.path.join(OUT, fname)
    if thumb not in cache and not os.path.exists(path):
        try:
            raw = urllib.request.urlopen(urllib.request.Request(thumb, headers=UA), timeout=60).read()
            im = Image.open(io.BytesIO(raw))
            im = im.convert('RGBA') if im.mode in ('P', 'LA', 'RGBA', 'L') else im.convert('RGB')
            im.thumbnail((256, 256))
            im.save(path, 'WEBP', quality=82, method=6)
        except Exception as e:
            print('download failed', key, e); continue
        time.sleep(0.15)
    cache[thumb] = fname
    result[key] = '/img/items/' + fname
json.dump(imgs, open(f'{W}/images.json', 'w', encoding='utf-8'), indent=1, ensure_ascii=False)
json.dump(result, open(f'{W}/image_paths.json', 'w', encoding='utf-8'), indent=1, ensure_ascii=False)
print('entries with image:', len(result), 'of', len(valid))
print('without image:', sorted(valid - set(result)))
