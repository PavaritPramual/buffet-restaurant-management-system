"""Read-only reference/snippet/hash validation for this documentation snapshot.

Run from any directory with Python 3. No database or network access is used.
This verifies evidence locations and artifact identity, not business correctness.
"""
import hashlib
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[3]
MANIFEST = json.loads(Path(__file__).with_name('validation.json').read_text(encoding='utf-8'))


def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT)


def blob(sha, path):
    return git('show', f'{sha}:{path}')


def verify():
    baseline = MANIFEST['codeBaseline']
    for anchor in MANIFEST['anchors']:
        lines = blob(baseline, anchor['path']).decode('utf-8').splitlines()
        assert anchor['needle'] in lines[anchor['line'] - 1], anchor
    for diagram in MANIFEST['diagrams']:
        for path, expected in diagram['gitBlobSha256'].items():
            assert hashlib.sha256(blob(diagram['sourceCommit'], path)).hexdigest() == expected, path
            # Canonical text comparison also detects stale working-copy previews.
            assert (ROOT/path).read_text(encoding='utf-8').replace('\r\n', '\n') == blob(
                diagram['sourceCommit'], path).decode('utf-8').replace('\r\n', '\n'), path
    docs = ['doc/architecture/pavarit-table-session-solid-jpa.md', 'doc/solid-analysis.md',
            'doc/diagrams/README.md', 'doc/testing/pavarit-module-docs-report-2026-10-08.md']
    relative_links = set()
    source_links = set()
    snippets = 0
    for path in docs:
        content = (ROOT/path).read_text(encoding='utf-8')
        for target in re.findall(r'\]\(([^)]+)\)', content):
            if target.startswith('https://github.com/'):
                match = re.search(r'/blob/([0-9a-f]{7,40})/([^#]+)#L(\d+)', target)
                if match:
                    sha, file, line = match.groups()
                    assert 1 <= int(line) <= len(blob(sha, file).decode('utf-8').splitlines()), target
                    source_links.add(target)
            elif not target.startswith(('http:', 'https:', '#')):
                relative = (ROOT/path).parent/target.split('#')[0]
                assert relative.exists(), (path, target)
                relative_links.add((path, target))
        for snippet in re.findall(r'```java\n(.*?)\n```', content, flags=re.S):
            assert snippet in blob(baseline,
                'code/backend/src/main/java/com/buffetrestaurant/service/CustomerBillingService.java').decode().replace('\r\n', '\n')
            snippets += 1
        assert not re.search(r'eyJ[A-Za-z0-9_-]{15,}\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+', content), path
        assert not re.search(r'postgres(?:ql)?://[^\s]+:[^\s]+@', content), path
    assert not git('diff', baseline, '--', 'code').strip(), 'Runtime diff must remain empty'
    git('diff', '--check')
    print(json.dumps({'sourceAnchors':len(MANIFEST['anchors']), 'sourceLinks':len(source_links),
                      'relativeLinks':len(relative_links), 'codeSnippets':snippets,
                      'diagramHashPairs':len(MANIFEST['diagrams']), 'runtimeDiff':'empty',
                      'result':'passed'}, ensure_ascii=False))


if __name__ == '__main__':
    verify()
