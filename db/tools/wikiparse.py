import re
def split_top(s):
    parts=[];depth=0;cur='';i=0
    while i<len(s):
        two=s[i:i+2]
        if two in ('{{','[['): depth+=1; cur+=two; i+=2; continue
        if two in ('}}',']]'): depth-=1; cur+=two; i+=2; continue
        if s[i]=='|' and depth==0: parts.append(cur); cur=''; i+=1; continue
        cur+=s[i]; i+=1
    parts.append(cur); return parts
def find_infobox(text):
    text=re.sub(r'<!--.*?-->','',text,flags=re.S)
    for m in re.finditer(r'\{\{\s*([A-Za-z _]*(?:Infobox|Template|infobox)[A-Za-z _]*)\s*\n?\|',text):
        name=m.group(1).strip(); start=m.start(); depth=0; i=start
        while i<len(text):
            if text.startswith('{{',i): depth+=1; i+=2; continue
            if text.startswith('}}',i):
                depth-=1; i+=2
                if depth==0: break
                continue
            i+=1
        body=text[start+2:i-2]; parts=split_top(body)
        params={}
        for p in parts[1:]:
            if '=' in p:
                k,v=p.split('=',1); params[k.strip().lower()]=v.strip()
        return name,params
    return None,{}
def clean(v):
    if v is None: return ''
    v=re.sub(r'<ref[^>]*/>','',v); v=re.sub(r'<ref.*?</ref>','',v,flags=re.S)
    v=re.sub(r'<br\s*/?>',', ',v,flags=re.I)
    v=re.sub(r'\[\[(?:File|Image):[^\]]*\]\]','',v)
    v=re.sub(r'\[\[[^\]|]*\|([^\]]*)\]\]',r'\1',v); v=re.sub(r'\[\[([^\]]*)\]\]',r'\1',v)
    v=re.sub(r'\[https?://\S+\s([^\]]*)\]',r'\1',v)
    for _ in range(3): v=re.sub(r'\{\{[^{}]*\}\}','',v)
    v=re.sub(r"'''?",'',v); v=re.sub(r'<[^>]+>','',v)
    v=v.replace('&amp;','&').replace('&nbsp;',' ')
    v=re.sub(r'\s*,\s*(,\s*)+',', ',v); v=re.sub(r'\s+',' ',v).strip(' ,;')
    return v
