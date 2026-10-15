#!/usr/bin/env python3
"""Builds a self-contained report.html from results/*.jsonl (stats are computed in the page)."""
import json
from pathlib import Path

root = Path(__file__).resolve().parent.parent
data = {}
for f in ("ios", "android"):
    p = root / "results" / f"{f}.jsonl"
    if p.exists():
        data[f] = [json.loads(l) for l in p.read_text().splitlines() if l.strip() and json.loads(l)["mode"] == "run"]
html = (root / "scripts" / "report_template.html").read_text().replace("/*__DATA__*/null", json.dumps(data))
(root / "index.html").write_text(html)
print("index.html written")
