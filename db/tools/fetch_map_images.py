# Downloads map covers, galleries and topographic maps from dayz.wiki.gg and converts them to WebP.
# usage: PYTHONPATH=<pylib> fetch_map_images.py <out dir>
import sys, os, io, json, time, urllib.request, urllib.parse
from PIL import Image

OUT = sys.argv[1]; os.makedirs(OUT, exist_ok=True)
UA = {'User-Agent': 'Mozilla/5.0 (wiki-reader)'}
# (output name, wiki file, width)
FILES = [
    ('chernarus-cover', 'Elektrozavodsk_7a.jpg', 1600), ('chernarus-map', '1.0_Hiking.jpg', 1600),
    ('chernarus-1', 'Cherno_overview.jpg', 1200), ('chernarus-2', 'Berezino_5b.jpg', 1200),
    ('chernarus-3', 'TisyArtwork.jpeg', 1200), ('chernarus-4', 'Green_Mountain_TV_Broadcast_Tower.png', 1200),
    ('livonia-cover', 'Livonia_KeyArt.png', 1600), ('livonia-map', 'Livonia_Hiking_Map2.png', 1600),
    ('livonia-1', 'Topolin1.jpg', 1200), ('livonia-2', 'NadborOverview.jpg', 1200),
    ('livonia-3', 'Livonia_Forest.png', 1200), ('livonia-4', 'Livonia_Bunker_Main_Overview.jpg', 1200),
    ('sakhal-cover', 'Sakhal.jpg', 1600), ('sakhal-map', 'Sakhal_map_upscaled.jpg', 1600),
    ('sakhal-1', 'Sakhal_airfield.jpg', 1200), ('sakhal-2', 'Sakhal_water_spring.jpg', 1200),
    ('sakhal-3', 'Sakhalag_overview.jpg', 1200), ('sakhal-4', 'Frostline.webp', 1200),
    ('nasdara-cover', 'Badlands_keyart_steam.jpg', 1232), ('nasdara-1', 'DayZ_Badlands_Release_Trailer_SCR_16.png', 1200),
    ('nasdara-2', 'Badlands_DevBlog_Week70_BeretP08.jpg', 1200), ('nasdara-3', 'Takistan_Map.jpg', 994),
]

def api(params):
    params['format'] = 'json'
    req = urllib.request.Request('https://dayz.wiki.gg/api.php?' + urllib.parse.urlencode(params), headers=UA)
    return json.load(urllib.request.urlopen(req, timeout=60))

for name, f, w in FILES:
    path = os.path.join(OUT, name + '.webp')
    if os.path.exists(path): continue
    d = api({'action': 'query', 'prop': 'imageinfo', 'iiprop': 'url', 'iiurlwidth': w, 'titles': 'File:' + f})['query']
    p = next(iter(d['pages'].values()))
    if not p.get('imageinfo'): print('missing', f); continue
    url = p['imageinfo'][0].get('thumburl') or p['imageinfo'][0]['url']
    raw = urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=120).read()
    im = Image.open(io.BytesIO(raw)).convert('RGB')
    im.thumbnail((w, w))
    im.save(path, 'WEBP', quality=80, method=6)
    print(name, im.size, os.path.getsize(path) // 1024, 'KB')
    time.sleep(0.5)
