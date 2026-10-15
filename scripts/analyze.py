#!/usr/bin/env python3
"""Summarise room-multi-db-bench JSONL results as Markdown.

Usage: python3 scripts/analyze.py results/*.jsonl > RESULTS.md
(results/<platform>[-sqldelight][-large].jsonl: Room by default, SQLDelight with the -sqldelight suffix)

Reads "run" lines (small suite, baseline S1) and "large-run" lines (large-volume
suite, scenarios L1/L2, baseline L1). Seed lines are ignored.
"""
import json
import statistics
import sys
from collections import defaultdict

SCENARIOS = ["S1", "S2", "S5", "S10"]
LARGE_SCENARIOS = ["L1", "L2"]
RUN_MODES = ("run", "large-run")
TIME_KEYS = ["openMs", "observeMs", "writeAllMs", "writeOneMs", "readMs"]
LARGE_TIME_KEYS = [
    "openMs", "weekQueryMs", "monthQueryMs", "agendaJoinMs", "searchContactsMs",
    "syncContactsMs", "syncCalendarMs", "parallelSyncMs",
]
LEGEND = (
    "**Legend** — "
    "`openMs`: build + first query of every database (open, schema check); "
    "`observeMs`: time until a Flow on each of the 10 tables emitted once; "
    "`writeAllMs`: 200 actions each writing 1 row into all 10 tables (1 transaction per database); "
    "`writeOneMs`: 200 transactions writing 1 row into table t0 only; "
    "`readMs`: 8 concurrent coroutines reading latest 200 rows of every table ×10 + full scans; "
    "`*OpenKb`: memory increase after opening; "
    "`*EndKb`: memory increase at the end (all connections open, caches warm). "
    "Large-volume suite (L1 = 1 DB, L2 = contacts DB + calendar DB, ~120 MB): "
    "`weekQueryMs`: 52 week queries (3 calendars); `monthQueryMs`: 12 month queries (10 calendars); "
    "`agendaJoinMs`: 12 months of agenda with contact names (L1 SQL JOIN, L2 in-memory join); "
    "`searchContactsMs`: 100 prefix searches; `syncContactsMs`: 2,000-contact upsert; "
    "`syncCalendarMs`: 5,000 events + attendees; `parallelSyncMs`: both syncs concurrently. "
    "Times in ms, memory in MB (delta vs process baseline). "
    "Cells: median (Q1–Q3)."
)


def is_mem(key):
    return key.endswith("OpenKb") or key.endswith("EndKb")


def is_num(v):
    return isinstance(v, (int, float)) and not isinstance(v, bool)


def load(path):
    runs = []
    with open(path, encoding="utf-8") as f:
        for lineno, line in enumerate(f, 1):
            line = line.strip()
            if not line:
                continue
            try:
                obj = json.loads(line)
            except json.JSONDecodeError:
                print(f"warning: {path}:{lineno}: invalid JSON, skipped", file=sys.stderr)
                continue
            if isinstance(obj, dict) and obj.get("mode") in RUN_MODES:
                runs.append(obj)
    return runs


def mem_order(key):
    # group by prefix, Open before End
    if key.endswith("OpenKb"):
        return (key[: -len("OpenKb")], 0)
    return (key[: -len("EndKb")], 1)


def metric_label(key):
    return f"{key} (MB)" if is_mem(key) else f"{key} (ms)"


def scale(key, v):
    return v / 1024.0 if is_mem(key) else v


def summarize(values):
    """Return (median, q1, q3) with q1/q3 None if < 2 values; None if empty."""
    if not values:
        return None
    med = statistics.median(values)
    if len(values) < 2:
        return med, None, None
    q = statistics.quantiles(values, n=4, method="inclusive")
    return med, q[0], q[2]


def fmt_cell(s):
    if s is None:
        return "–"
    med, q1, q3 = s
    if q1 is None:
        return f"{med:.1f}"
    return f"{med:.1f} <sub>({q1:.1f}–{q3:.1f})</sub>"


def fmt_delta(d):
    if d is None:
        return "–"
    if abs(d) < 0.05:
        return "±0.0"
    return f"{d:+.1f}"


def scenario_sort_key(s):
    if s in SCENARIOS:
        return (0, SCENARIOS.index(s), s)
    if s in LARGE_SCENARIOS:
        return (1, LARGE_SCENARIOS.index(s), s)
    return (2, 0, s)


def render(path, runs):
    out = []
    by_scenario = defaultdict(list)
    for r in runs:
        by_scenario[str(r.get("scenario", "?"))].append(r)
    large = any(s in LARGE_SCENARIOS for s in by_scenario)
    order, baseline, time_order = (
        (LARGE_SCENARIOS, "L1", LARGE_TIME_KEYS) if large else (SCENARIOS, "S1", TIME_KEYS)
    )
    scenarios = [s for s in order if s in by_scenario]
    scenarios += sorted((s for s in by_scenario if s not in order), key=scenario_sort_key)

    platforms = sorted({str(r["platform"]) for r in runs if r.get("platform")})
    engine = "SQLDelight" if any(r.get("engine") == "sqldelight" for r in runs) else "Room"
    title = f"{engine} · " + (", ".join(platforms) if platforms else path) + (" · large volume" if large else "")
    sqlite = sorted({str(r["sqlite"]) for r in runs if r.get("sqlite")})
    counts = ", ".join(f"{s}: {len(by_scenario[s])}" for s in scenarios) or "no runs"
    out.append(f"## {title}")
    out.append("")
    out.append(f"Source: `{path}` — runs per scenario: {counts}" + (f" — SQLite {', '.join(sqlite)}" if sqlite else ""))
    out.append("")
    if not runs:
        out.append("_No `run` / `large-run` lines found._")
        out.append("")
        return out

    mem_keys = sorted({k for r in runs for k in r if is_mem(k) and is_num(r[k])}, key=mem_order)
    time_keys = [k for k in time_order if any(is_num(r.get(k)) for r in runs)]
    metrics = time_keys + mem_keys

    stats = {}
    for m in metrics:
        for s in scenarios:
            vals = [scale(m, r[m]) for r in by_scenario[s] if is_num(r.get(m))]
            stats[(m, s)] = summarize(vals)

    out.append("| Metric | " + " | ".join(scenarios) + " |")
    out.append("|---|" + "---:|" * len(scenarios))
    for m in metrics:
        cells = [fmt_cell(stats[(m, s)]) for s in scenarios]
        out.append(f"| {metric_label(m)} | " + " | ".join(cells) + " |")
    out.append("")

    others = [s for s in scenarios if s != baseline]
    if baseline in scenarios and others:
        out.append(f"**Δ vs {baseline} (median)**")
        out.append("")
        out.append("| Metric | " + " | ".join(others) + " |")
        out.append("|---|" + "---:|" * len(others))
        for m in metrics:
            base = stats[(m, baseline)]
            cells = []
            for s in others:
                cur = stats[(m, s)]
                cells.append(fmt_delta(cur[0] - base[0] if cur and base else None))
            out.append(f"| {metric_label(m)} | " + " | ".join(cells) + " |")
        out.append("")
    return out


def main(argv):
    if len(argv) < 2:
        print(__doc__.strip(), file=sys.stderr)
        return 2
    lines = ["# Multi-database benchmark results (Room & SQLDelight)", "", LEGEND, ""]
    for path in argv[1:]:
        try:
            runs = load(path)
        except OSError as e:
            print(f"error: cannot read {path}: {e}", file=sys.stderr)
            return 1
        lines.extend(render(path, runs))
    sys.stdout.write("\n".join(lines).rstrip() + "\n")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
