# Room multi-database benchmark results

**Legend** — `openMs`: build + first query of every database (open, schema check); `observeMs`: time until a Flow on each of the 10 tables emitted once; `writeAllMs`: 200 actions each writing 1 row into all 10 tables (1 transaction per database); `writeOneMs`: 200 transactions writing 1 row into table t0 only; `readMs`: 8 concurrent coroutines reading latest 200 rows of every table ×10 + full scans; `*OpenKb`: memory increase after opening; `*EndKb`: memory increase at the end (all connections open, caches warm). Times in ms, memory in MB (delta vs process baseline). Cells: median (Q1–Q3).

## ios-26.5

Source: `results/ios.jsonl` — runs per scenario: S1: 20, S2: 20, S5: 20, S10: 20, S5c: 20

| Metric | S1 | S2 | S5 | S10 | S5c |
|---|---:|---:|---:|---:|---:|
| openMs (ms) | 3.4 <sub>(3.1–3.8)</sub> | 6.0 <sub>(4.6–7.2)</sub> | 12.6 <sub>(10.5–16.2)</sub> | 23.3 <sub>(20.4–29.0)</sub> | 15.7 <sub>(10.2–29.6)</sub> |
| observeMs (ms) | 8.5 <sub>(6.8–12.2)</sub> | 8.5 <sub>(7.7–12.1)</sub> | 7.7 <sub>(5.8–9.5)</sub> | 6.9 <sub>(5.9–8.2)</sub> | 4.7 <sub>(3.2–6.0)</sub> |
| writeAllMs (ms) | 88.8 <sub>(86.3–98.8)</sub> | 102.7 <sub>(91.9–110.4)</sub> | 173.1 <sub>(160.6–208.9)</sub> | 283.0 <sub>(259.1–305.9)</sub> | 124.6 <sub>(108.2–269.4)</sub> |
| writeOneMs (ms) | 22.4 <sub>(17.2–27.7)</sub> | 19.6 <sub>(15.9–27.1)</sub> | 20.9 <sub>(19.8–24.6)</sub> | 19.1 <sub>(16.6–25.7)</sub> | 20.4 <sub>(19.0–26.4)</sub> |
| readMs (ms) | 92.6 <sub>(88.2–101.4)</sub> | 91.2 <sub>(79.8–97.6)</sub> | 73.8 <sub>(69.5–78.6)</sub> | 75.0 <sub>(70.6–79.5)</sub> | 94.7 <sub>(85.9–98.5)</sub> |
| footprintOpenKb (MB) | 2.3 <sub>(1.8–2.9)</sub> | 3.5 <sub>(2.6–4.3)</sub> | 7.1 <sub>(5.4–8.8)</sub> | 13.2 <sub>(10.1–16.2)</sub> | 7.1 <sub>(5.4–8.8)</sub> |
| footprintEndKb (MB) | 20.2 <sub>(19.8–22.1)</sub> | 29.1 <sub>(26.1–31.1)</sub> | 56.3 <sub>(50.9–59.7)</sub> | 61.7 <sub>(54.7–71.2)</sub> | 33.1 <sub>(30.2–37.0)</sub> |

**Δ vs S1 (median)**

| Metric | S2 | S5 | S10 | S5c |
|---|---:|---:|---:|---:|
| openMs (ms) | +2.6 | +9.2 | +19.9 | +12.4 |
| observeMs (ms) | ±0.0 | -0.9 | -1.7 | -3.8 |
| writeAllMs (ms) | +13.9 | +84.3 | +194.1 | +35.8 |
| writeOneMs (ms) | -2.8 | -1.5 | -3.2 | -2.0 |
| readMs (ms) | -1.4 | -18.8 | -17.6 | +2.1 |
| footprintOpenKb (MB) | +1.1 | +4.8 | +10.9 | +4.8 |
| footprintEndKb (MB) | +8.9 | +36.1 | +41.4 | +12.9 |

## android-36

Source: `results/android.jsonl` — runs per scenario: S1: 20, S2: 20, S5: 20, S10: 20, S5c: 20

| Metric | S1 | S2 | S5 | S10 | S5c |
|---|---:|---:|---:|---:|---:|
| openMs (ms) | 9.1 <sub>(7.1–18.6)</sub> | 14.0 <sub>(11.5–19.4)</sub> | 42.0 <sub>(33.1–54.1)</sub> | 56.8 <sub>(44.1–82.0)</sub> | 39.1 <sub>(32.3–53.2)</sub> |
| observeMs (ms) | 26.5 <sub>(15.3–37.6)</sub> | 14.4 <sub>(10.1–27.3)</sub> | 22.4 <sub>(14.4–25.5)</sub> | 12.2 <sub>(8.7–20.0)</sub> | 18.5 <sub>(14.6–22.7)</sub> |
| writeAllMs (ms) | 506.3 <sub>(466.3–545.2)</sub> | 669.9 <sub>(635.2–721.1)</sub> | 976.5 <sub>(842.8–1008.2)</sub> | 1164.5 <sub>(1142.2–1207.6)</sub> | 839.9 <sub>(794.0–885.1)</sub> |
| writeOneMs (ms) | 116.2 <sub>(107.2–130.0)</sub> | 83.2 <sub>(79.6–93.6)</sub> | 77.6 <sub>(70.7–80.1)</sub> | 73.5 <sub>(67.5–78.4)</sub> | 95.9 <sub>(92.3–112.1)</sub> |
| readMs (ms) | 130.4 <sub>(118.2–155.1)</sub> | 109.2 <sub>(98.6–118.2)</sub> | 88.3 <sub>(76.4–97.8)</sub> | 81.8 <sub>(73.4–85.9)</sub> | 133.2 <sub>(121.9–149.0)</sub> |
| javaHeapOpenKb (MB) | 0.0 <sub>(-0.0–0.1)</sub> | 0.1 <sub>(0.0–0.2)</sub> | 0.1 <sub>(0.0–0.2)</sub> | 0.2 <sub>(0.1–0.2)</sub> | 0.1 <sub>(0.1–0.2)</sub> |
| javaHeapEndKb (MB) | 0.0 <sub>(0.0–0.1)</sub> | 0.1 <sub>(0.0–0.1)</sub> | 0.1 <sub>(0.1–0.2)</sub> | 0.2 <sub>(0.1–0.2)</sub> | 0.1 <sub>(0.1–0.2)</sub> |
| nativeHeapOpenKb (MB) | 2.1 <sub>(1.8–2.5)</sub> | 3.1 <sub>(2.4–3.6)</sub> | 6.7 <sub>(4.8–7.9)</sub> | 11.5 <sub>(8.9–14.5)</sub> | 6.6 <sub>(5.3–7.7)</sub> |
| nativeHeapEndKb (MB) | 10.3 <sub>(10.2–11.1)</sub> | 18.3 <sub>(16.9–19.4)</sub> | 36.7 <sub>(30.9–39.9)</sub> | 37.6 <sub>(29.6–47.0)</sub> | 11.4 <sub>(9.8–12.4)</sub> |
| pssOpenKb (MB) | 4.8 <sub>(4.3–5.4)</sub> | 5.7 <sub>(5.2–6.4)</sub> | 9.7 <sub>(7.6–11.1)</sub> | 14.6 <sub>(12.3–17.4)</sub> | 9.7 <sub>(7.8–10.7)</sub> |
| pssEndKb (MB) | 14.9 <sub>(14.2–15.7)</sub> | 23.0 <sub>(21.0–23.8)</sub> | 41.4 <sub>(35.2–44.7)</sub> | 41.9 <sub>(34.5–51.7)</sub> | 16.0 <sub>(14.4–16.9)</sub> |

**Δ vs S1 (median)**

| Metric | S2 | S5 | S10 | S5c |
|---|---:|---:|---:|---:|
| openMs (ms) | +4.9 | +32.9 | +47.6 | +30.0 |
| observeMs (ms) | -12.1 | -4.1 | -14.3 | -8.0 |
| writeAllMs (ms) | +163.6 | +470.2 | +658.1 | +333.5 |
| writeOneMs (ms) | -32.9 | -38.6 | -42.6 | -20.2 |
| readMs (ms) | -21.1 | -42.0 | -48.6 | +2.8 |
| javaHeapOpenKb (MB) | +0.1 | +0.1 | +0.1 | +0.1 |
| javaHeapEndKb (MB) | +0.1 | +0.1 | +0.1 | +0.1 |
| nativeHeapOpenKb (MB) | +1.0 | +4.6 | +9.4 | +4.5 |
| nativeHeapEndKb (MB) | +8.0 | +26.3 | +27.3 | +1.1 |
| pssOpenKb (MB) | +0.9 | +4.9 | +9.7 | +4.8 |
| pssEndKb (MB) | +8.1 | +26.6 | +27.0 | +1.1 |
