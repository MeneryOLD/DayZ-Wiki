# usage: wikifetch.py outdir title1 title2 ...  -> saves raw wikitext per title (follows redirects) + index.json
import sys, json, os, time, urllib.request, urllib.parse
out = sys.argv[1]; os.makedirs(out, exist_ok=True)
titles = sys.argv[2:]
if titles and titles[0].startswith('@'):
    titles = [l.strip() for l in open(titles[0][1:], encoding='utf-8') if l.strip()]
idx_path = os.path.join(out, 'index.json')
idx = json.load(open(idx_path, encoding='utf-8')) if os.path.exists(idx_path) else {}
todo = [t for t in titles if t not in idx]
for i in range(0, len(todo), 50):
    chunk = todo[i:i+50]
    q = urllib.parse.urlencode({'action':'query','prop':'revisions','rvprop':'content','rvslots':'main','format':'json','formatversion':'2','redirects':'1','titles':'|'.join(chunk)})
    req = urllib.request.Request('https://dayz.wiki.gg/api.php?'+q, headers={'User-Agent':'Mozilla/5.0 (wiki-reader)'})
    d = json.load(urllib.request.urlopen(req, timeout=60))['query']
    redir = {r['from']: r['to'] for r in d.get('redirects', [])}
    norm = {r['from']: r['to'] for r in d.get('normalized', [])}
    pages = {p['title']: p for p in d['pages']}
    for t in chunk:
        tt = norm.get(t, t); tt = redir.get(tt, tt)
        p = pages.get(tt)
        if not p or 'revisions' not in p: idx[t] = None; continue
        fn = ''.join(c if c.isalnum() else '_' for c in tt) + '.txt'
        open(os.path.join(out, fn), 'w', encoding='utf-8').write(p['revisions'][0]['slots']['main']['content'])
        idx[t] = {'title': tt, 'file': fn}
    time.sleep(1)
json.dump(idx, open(idx_path, 'w', encoding='utf-8'), indent=0)
print('fetched', len(todo), 'missing', [t for t in titles if idx.get(t) is None][:40])
