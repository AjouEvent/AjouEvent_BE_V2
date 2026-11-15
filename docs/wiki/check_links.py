#!/usr/bin/env python3
import pathlib, re, sys
root = pathlib.Path(__file__).resolve().parent
files = list(root.rglob('*.md'))
index = {p.stem for p in files}
missing = []
for path in files:
    for target in re.findall(r'\[\[([^]]+)\]\]', path.read_text()):
        name = target.split('|', 1)[0].split('#', 1)[0].split('/')[-1]
        if name and name not in index:
            missing.append((str(path.relative_to(root)), name))
for path, name in missing:
    print(f'{path}: [[{name}]]')
print(f'broken links: {len(missing)}')
sys.exit(bool(missing))
