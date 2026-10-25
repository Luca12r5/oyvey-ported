#!/usr/bin/env python3
"""Rewrites intermediary names left in mixin strings and @Shadow members (method = "method_4965", target = "Lnet/minecraft/class_1;...")
of the decompiled Bauloo source to Mojang names, using the tiny mappings Loom generated in this CI run.
Usage: fix-mixins.py <decompiled dir> <tiny file>...  (the mappings themselves are not written anywhere)"""
import os
import re
import sys

root, tinies = sys.argv[1], sys.argv[2:]
classes, members = {}, {}
for path in tinies:
    with open(path, encoding='utf-8') as f:
        header = f.readline().rstrip('\n').split('\t')
        if header[0] != 'tiny' or 'intermediary' not in header or 'named' not in header:
            continue
        ns = header[3:]
        ii, ni = ns.index('intermediary'), ns.index('named')
        for line in f:
            p = line.rstrip('\n').split('\t')
            # Some tiny files map intermediary to itself; never let those hide the Mojang name.
            if p[0] == 'c':
                if p[1 + ni] != p[1 + ii]:
                    classes[p[1 + ii]] = p[1 + ni]
            elif len(p) > 2 and p[1] in ('m', 'f'):
                inter, named = p[3 + ii], p[3 + ni]
                if re.fullmatch(r'(method|field)_\d+', inter) and named != inter:
                    members[inter] = named
print(f'MAPPINGS: {len(classes)} classes, {len(members)} members from {len(tinies)} tiny file(s)')

def cls(m):
    name = m.group(0)
    return classes.get(name, name)

def fix(s):
    s = re.sub(r'net/minecraft/class_\d+(?:\$class_\d+)*', lambda m: classes.get(m.group(0), m.group(0)), s)
    s = re.sub(r'net\.minecraft\.class_\d+(?:\$class_\d+)*',
               lambda m: classes.get(m.group(0).replace('.', '/'), m.group(0).replace('.', '/')).replace('/', '.'), s)
    return re.sub(r'\b(method|field)_\d+\b', lambda m: members.get(m.group(0), m.group(0)), s)

left = 0
for dirpath, _, files in os.walk(root):
    for name in files:
        if not name.endswith('.java'):
            continue
        p = os.path.join(dirpath, name)
        src = open(p, encoding='utf-8').read()
        # Strings (mixin targets) and identifiers (@Shadow members keep their intermediary names).
        out = fix(src)
        if out != src:
            open(p, 'w', encoding='utf-8').write(out)
        for m in re.finditer(r'"[^"\n]*\b(?:class|method|field)_\d+[^"\n]*"', out):
            left += 1
            print(f'unmapped: {name}: {m.group(0)}')
print(f'LEFT: {left} strings still contain intermediary names')
