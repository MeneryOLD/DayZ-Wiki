import sys, json, time, urllib.request, urllib.parse
UA={'User-Agent':'Mozilla/5.0 (wiki-reader)'}
def get(params):
    params['format']='json'
    req=urllib.request.Request('https://dayz.wiki.gg/api.php?'+urllib.parse.urlencode(params), headers=UA)
    return json.load(urllib.request.urlopen(req, timeout=60))
out={}
for t in sys.argv[2:]:
    fields=list(get({'action':'cargofields','table':t})['cargofields'].keys())
    rows=[]; off=0
    while True:
        d=get({'action':'cargoquery','tables':t,'fields':','.join(['_pageName=Page']+[f'{f}={f}' for f in fields]),'limit':500,'offset':off})
        if 'error' in d: print(t, d['error']); break
        r=[x['title'] for x in d['cargoquery']]; rows+=r; off+=500; time.sleep(1)
        if len(r)<500: break
    out[t]=rows; print(t, len(rows), fields)
json.dump(out, open(sys.argv[1],'w',encoding='utf-8'), indent=0, ensure_ascii=False)
