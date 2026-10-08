"""Read-only verification of the integrated runtime and documentation evidence."""
from pathlib import Path
import hashlib, json, re, subprocess

ROOT=Path(__file__).resolve().parents[3]
HERE=Path(__file__).resolve().parent
M=json.loads((HERE/'validation.json').read_text(encoding='utf-8'))
def git(*args): return subprocess.check_output(['git',*args],cwd=ROOT)
def blob(sha,path): return git('show',f'{sha}:{path}')
def load(path): return json.loads((HERE/path).read_text(encoding='utf-8'))

def verify():
    assert not git('diff',M['codeBaseline'],'--','code','test/browser').strip(), 'Runtime/runner changed after tested revision'
    subprocess.run(['node','test/browser/evidence-source-hashes.cjs','verify',M['codeBaseline'],str(HERE/'canonical-source-hashes.json')],cwd=ROOT,check=True)
    for a in M['anchors']:
        assert a['needle'] in blob(a['commit'],a['path']).decode().splitlines()[a['line']-1],a
    for d in M['diagrams']:
        for p,h in d['hashes'].items():
            raw=blob(d['sourceCommit'],p)
            assert hashlib.sha256(raw).hexdigest()==h,p
            assert (ROOT/p).read_text(encoding='utf-8')==raw.decode().replace('\r\n','\n'),p
    links=set()
    for p in M['documents']:
        text=(ROOT/p).read_text(encoding='utf-8')
        for target in re.findall(r'\]\(([^)]+)\)',text):
            source=re.search(r'/blob/([0-9a-f]{40})/([^#]+)#L(\d+)',target)
            if source:
                sha,file,line=source.groups()
                assert 1<=int(line)<=len(blob(sha,file).decode().splitlines()),target
            elif not target.startswith(('http:','https:','#')):
                destination=((ROOT/p).parent/target.split('#')[0]).resolve()
                # This script creates its own result after all checks pass.
                assert destination.exists() or destination==HERE/'verification-result.json',target
            links.add((p,target))
        assert not re.search(r'postgres(?:ql)?://[^\s]+:[^\s]+@',text),p
    backend=load('backend-summary.json')
    assert backend['codeCommit']==M['codeBaseline']
    assert backend['totals']==M['backend']
    for key in ['tests','failures','errors','skipped']:
        assert sum(s[key] for s in backend['suites'])==backend['totals'][key]
    assert all(backend['totals'][k]==0 for k in ['failures','errors','skipped'])
    frontend=(HERE/'frontend-test.txt').read_text(encoding='utf-8')
    guards=(HERE/'frontend-guards.txt').read_text(encoding='utf-8')
    assert re.search(r'Tests\s+121 passed \(121\)',frontend)
    assert re.search(r'pass 6\b',guards) and re.search(r'fail 0\b',guards)
    lint=(HERE/'frontend-lint.txt').read_text(encoding='utf-8')
    assert len(re.findall(r'^src/[^\n]+: warning ',lint,re.M))==4
    assert not re.search(r'^src/[^\n]+: error ',lint,re.M)
    pg=[s for s in backend['suites'] if '.Postgres' in s['name']]
    assert pg and all(s['tests']>0 and s['skipped']==0 for s in pg)
    runtime=load('browser/runtime-summary.json')
    assert runtime['sourceCommit']==M['codeBaseline'] and runtime['publicAcceptance'] is False
    assert runtime['envFileImport'] is False and runtime['seedFixtures'] is False
    assert all(s['exitCode']==0 for s in runtime['scripts'])
    # Runtime runner SHA256 is the original checkout-byte hash, not a Git blob hash.
    # Canonical source identity is proven separately above, independent of CRLF.
    results={
        'core-flow':load('browser/core-flow/results.json'),
        'stock-profile':load('browser/stock-profile/results.json'),
        'concurrency-fixtures':load('browser/concurrency-fixtures/browser-concurrency-results.json'),
        'ui-state-fixtures':load('browser/ui-state-fixtures/results.json'),
        'stock-profile-fixtures':load('browser/stock-profile-fixtures/results.json'),
    }
    for label,data in results.items():
        assert all(x['result']=='PASS' for x in data['results']),label
        count=sum(len(x['states']) for x in data['results']) if label=='ui-state-fixtures' else len(data['results'])
        assert count==M['expectedBrowser'][label],label
        if label in ['core-flow','stock-profile']:
            assert data['httpMocks'] is False and data['sourceCommit']==M['codeBaseline']
        if label in ['concurrency-fixtures','stock-profile-fixtures']: assert data['httpMocks'] is True
    pictures=load('screenshots.json')
    for p in pictures:
        assert hashlib.sha256((ROOT/p['path']).read_bytes()).hexdigest()==p['sha256'],p['path']
    for p in HERE.rglob('*.json'):
        assert not re.search(r'"(?:password|credential|sessionToken|tokenHash)"\s*:',p.read_text(encoding='utf-8')),p
    git('diff','--check')
    result={'result':'passed','canonicalFiles':len(load('canonical-source-hashes.json')['files']),
        'sourceAnchors':len(M['anchors']),'documentLinks':len(links),'diagrams':len(M['diagrams']),
        'backend':backend['totals'],'postgresSuites':len(pg),'frontendTests':121,'urlGuards':6,
        'lint':{'errors':0,'warnings':4},'browser':M['expectedBrowser'],
        'screenshots':len(pictures),'runtimeDiff':'empty','publicAcceptance':False}
    print(json.dumps(result,ensure_ascii=False))
    return result

if __name__=='__main__':
    result=verify()
    (HERE/'verification-result.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
