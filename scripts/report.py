#!/usr/bin/env python3
"""Builds a self-contained index.html from results/*.jsonl (stats are computed in the page)."""
import json
from pathlib import Path

root = Path(__file__).resolve().parent.parent


def read(name):
    p = root / "results" / f"{name}.jsonl"
    return [json.loads(l) for l in p.read_text().splitlines() if l.strip()] if p.exists() else None


data, large = {}, {}
for f in ("android", "ios", "macos", "jvm"):
    rows = read(f)
    if rows is not None:
        data[f] = [r for r in rows if r.get("mode") == "run"]
    rows = read(f"{f}-large")
    if rows is not None:
        runs = [r for r in rows if r.get("mode") == "large-run"]
        if runs:
            large[f] = {"runs": runs, "seeds": [r for r in rows if r.get("mode") == "large-seed"]}
html = (root / "scripts" / "report_template.html").read_text()
html = html.replace("/*__DATA__*/null", json.dumps(data)).replace("/*__LARGE__*/null", json.dumps(large))
(root / "index.html").write_text(html)
print("index.html written" + ("" if large else " (no large-volume data yet)"))
