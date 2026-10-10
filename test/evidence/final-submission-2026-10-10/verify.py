from pathlib import Path
import json, hashlib, zlib, re, subprocess, xml.etree.ElementTree as ET, zipfile
from pypdf import PdfReader
R=Path(__file__).resolve().parents[3]
E=Path(__file__).parent
def sha(p):
    data=p.read_bytes()
    if p.suffix in {'.puml','.svg','.txt'}: data=data.replace(b'\r\n',b'\n')
    return hashlib.sha256(data).hexdigest()
history=json.loads((E/'flyway-history.json').read_text())
checks=[]
for row in history['rows']:
    matches=list((R/'code/backend/src/main/resources/db/migration').rglob(row['script']))
    p=next(p for p in matches if 'postgresql' in p.parts or 'common' in p.parts)
    crc=0
    for line in p.read_text(encoding='utf-8-sig').splitlines(): crc=zlib.crc32(line.encode('utf-8'),crc)
    if crc>=2**31: crc-=2**32
    checks.append(dict(version=row['version'],path=p.relative_to(R).as_posix(),history=row['checksum'],calculated=crc,match=crc==row['checksum'] and row['success']))
(E/'flyway-checksums.json').write_text(json.dumps(checks,indent=2)+'\n')
assert all(x['match'] for x in checks), 'Applied migration mismatch: inspect without repair'
diagrams=[]
for p in sorted((R/'doc/diagrams').glob('*.puml')):
    svg=p.parent/'previews'/p.with_suffix('.svg').name
    ET.parse(svg)
    assert not re.search(r'Syntax Error|An error has occur|java.lang.IllegalStateException',svg.read_text(encoding='utf-8'))
    diagrams.append(dict(source=p.relative_to(R).as_posix(),source_sha256=sha(p),preview=svg.relative_to(R).as_posix(),preview_sha256=sha(svg)))
assert len(diagrams)==28
(E/'diagram-manifest.json').write_text(json.dumps(diagrams,indent=2)+'\n')
slides=R/'doc/slide'; stem='team-final-canva-v4-2026-10-10'
pdf=slides/(stem+'.pdf'); pptx=slides/(stem+'.pptx')
with zipfile.ZipFile(pptx) as z: count=len([n for n in z.namelist() if re.fullmatch(r'ppt/slides/slide\d+\.xml',n)])
assert len(PdfReader(pdf).pages)==count==24
(E/'slide-manifest.json').write_text(json.dumps(dict(design_id='DAHXmHjoiFU',canva='https://canva.link/43kx8nvyrmulyam',pages=24,exported='2026-10-10',content='Existing chosen Canva version, unchanged; original title includes Draft v4',files=[dict(path=p.relative_to(R).as_posix(),sha256=sha(p)) for p in [pdf,pptx,slides/(stem+'.txt')]]),indent=2)+'\n')
changed=subprocess.check_output(['git','diff','--name-only'],cwd=R,text=True).splitlines()
assert not any(p.startswith('code/') for p in changed)
broken=[]
for name in changed+['doc/planning/final-submission-checklist.md']:
    p=R/name
    if p.suffix!='.md': continue
    for dest in re.findall(r'\]\(([^)]+)\)',p.read_text(encoding='utf-8')):
        dest=dest.split('#')[0].strip('<>')
        if not dest or re.match(r'\w+://',dest) or dest.startswith('/'): continue
        if not (p.parent/dest).exists(): broken.append(dict(file=name,target=dest))
(E/'relative-link-check.json').write_text(json.dumps(broken,ensure_ascii=False,indent=2)+'\n')
print(json.dumps(dict(migration_checks=len(checks),matched=sum(x['match'] for x in checks),diagrams=len(diagrams),slides=count,broken_links=broken),ensure_ascii=False))
