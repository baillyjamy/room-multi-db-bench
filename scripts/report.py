#!/usr/bin/env python3
"""Builds a self-contained index.html from results/*.jsonl (stats are computed in the page)."""
import json
from pathlib import Path

root = Path(__file__).resolve().parent.parent


def read(name):
    p = root / "results" / f"{name}.jsonl"
    return [json.loads(l) for l in p.read_text().splitlines() if l.strip()] if p.exists() else None


data, large, light = {}, {}, {}
for engine, suffix in (("room", ""), ("sqldelight", "-sqldelight")):
    data[engine], large[engine], light[engine] = {}, {}, {}
    for f in ("android", "ios", "macos", "jvm"):
        rows = read(f"{f}{suffix}")
        if rows is not None:
            data[engine][f] = [r for r in rows if r.get("mode") == "run"]
        for vol, out in (("large", large), ("light", light)):
            rows = read(f"{f}{suffix}-{vol}")
            runs = [r for r in rows or [] if r.get("mode") == f"{vol}-run"]
            if runs:
                out[engine][f] = {"runs": runs, "seeds": [r for r in rows if r.get("mode") == f"{vol}-seed"]}
html = (root / "scripts" / "report_template.html").read_text()
html = html.replace("/*__DATA__*/null", json.dumps(data)).replace("/*__LARGE__*/null", json.dumps(large)).replace("/*__LIGHT__*/null", json.dumps(light))
(root / "index.html").write_text(html)
print("index.html written: " + ", ".join(f"{e} {len(d)} platform(s)" for e, d in data.items()))
