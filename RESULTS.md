# Room multi-database benchmark results

**Legend** — `openMs`: build + first query of every database (open, schema check); `observeMs`: time until a Flow on each of the 10 tables emitted once; `writeAllMs`: 200 actions each writing 1 row into all 10 tables (1 transaction per database); `writeOneMs`: 200 transactions writing 1 row into table t0 only; `readMs`: 8 concurrent coroutines reading latest 200 rows of every table ×10 + full scans; `*OpenKb`: memory increase after opening; `*EndKb`: memory increase at the end (all connections open, caches warm). Large-volume suite (L1 = 1 DB, L2 = contacts DB + calendar DB, ~120 MB): `weekQueryMs`: 52 week queries (3 calendars); `monthQueryMs`: 12 month queries (10 calendars); `agendaJoinMs`: 12 months of agenda with contact names (L1 SQL JOIN, L2 in-memory join); `searchContactsMs`: 100 prefix searches; `syncContactsMs`: 2,000-contact upsert; `syncCalendarMs`: 5,000 events + attendees; `parallelSyncMs`: both syncs concurrently. Times in ms, memory in MB (delta vs process baseline). Cells: median (Q1–Q3).

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

## ios-26.5

Source: `results/ios.jsonl` — runs per scenario: S1: 20, S2: 20, S5: 20, S10: 20, S5c: 20

| Metric | S1 | S2 | S5 | S10 | S5c |
|---|---:|---:|---:|---:|---:|
| openMs (ms) | 3.9 <sub>(3.1–9.2)</sub> | 5.6 <sub>(4.7–9.9)</sub> | 14.4 <sub>(11.7–18.0)</sub> | 23.7 <sub>(20.1–27.4)</sub> | 12.6 <sub>(11.0–24.0)</sub> |
| observeMs (ms) | 6.5 <sub>(5.5–9.0)</sub> | 6.4 <sub>(5.8–8.1)</sub> | 4.9 <sub>(3.9–5.5)</sub> | 3.5 <sub>(2.7–4.1)</sub> | 2.4 <sub>(2.2–3.2)</sub> |
| writeAllMs (ms) | 87.1 <sub>(79.2–92.8)</sub> | 101.5 <sub>(96.8–106.3)</sub> | 163.9 <sub>(155.4–169.2)</sub> | 242.0 <sub>(227.9–247.5)</sub> | 120.6 <sub>(100.1–302.2)</sub> |
| writeOneMs (ms) | 16.1 <sub>(15.4–17.4)</sub> | 17.5 <sub>(16.5–18.8)</sub> | 18.7 <sub>(17.4–19.1)</sub> | 15.7 <sub>(15.4–16.3)</sub> | 19.4 <sub>(18.8–27.1)</sub> |
| readMs (ms) | 91.7 <sub>(88.2–96.9)</sub> | 86.5 <sub>(79.1–89.3)</sub> | 75.1 <sub>(70.5–77.9)</sub> | 75.9 <sub>(72.6–77.2)</sub> | 93.1 <sub>(90.8–95.7)</sub> |
| footprintOpenKb (MB) | 2.2 <sub>(1.6–2.7)</sub> | 3.2 <sub>(2.4–4.0)</sub> | 6.9 <sub>(5.2–8.5)</sub> | 13.0 <sub>(9.9–16.0)</sub> | 6.8 <sub>(5.2–8.5)</sub> |
| footprintEndKb (MB) | 19.5 <sub>(18.0–21.6)</sub> | 28.5 <sub>(26.9–29.8)</sub> | 56.3 <sub>(51.4–58.9)</sub> | 62.9 <sub>(51.2–68.8)</sub> | 31.9 <sub>(29.8–33.8)</sub> |

**Δ vs S1 (median)**

| Metric | S2 | S5 | S10 | S5c |
|---|---:|---:|---:|---:|
| openMs (ms) | +1.7 | +10.5 | +19.8 | +8.7 |
| observeMs (ms) | -0.2 | -1.6 | -3.1 | -4.1 |
| writeAllMs (ms) | +14.4 | +76.8 | +154.9 | +33.5 |
| writeOneMs (ms) | +1.4 | +2.6 | -0.4 | +3.3 |
| readMs (ms) | -5.1 | -16.6 | -15.8 | +1.4 |
| footprintOpenKb (MB) | +1.0 | +4.7 | +10.8 | +4.7 |
| footprintEndKb (MB) | +9.0 | +36.8 | +43.4 | +12.4 |

## macos-26.7

Source: `results/macos.jsonl` — runs per scenario: S1: 20, S2: 20, S5: 20, S10: 20, S5c: 20

| Metric | S1 | S2 | S5 | S10 | S5c |
|---|---:|---:|---:|---:|---:|
| openMs (ms) | 6.5 <sub>(3.7–9.3)</sub> | 7.8 <sub>(5.4–10.9)</sub> | 16.2 <sub>(14.6–18.3)</sub> | 17.9 <sub>(14.6–22.2)</sub> | 9.9 <sub>(9.3–11.2)</sub> |
| observeMs (ms) | 10.0 <sub>(7.4–13.9)</sub> | 8.4 <sub>(5.5–12.7)</sub> | 7.9 <sub>(7.2–11.6)</sub> | 5.4 <sub>(3.0–6.7)</sub> | 6.4 <sub>(4.8–12.0)</sub> |
| writeAllMs (ms) | 71.5 <sub>(62.5–76.3)</sub> | 84.1 <sub>(79.1–87.2)</sub> | 149.4 <sub>(135.1–157.0)</sub> | 235.9 <sub>(217.8–249.3)</sub> | 115.6 <sub>(97.5–297.2)</sub> |
| writeOneMs (ms) | 14.9 <sub>(13.0–18.6)</sub> | 15.7 <sub>(13.7–16.3)</sub> | 16.7 <sub>(15.8–18.3)</sub> | 13.6 <sub>(13.3–14.1)</sub> | 16.3 <sub>(15.7–18.6)</sub> |
| readMs (ms) | 94.5 <sub>(92.7–96.3)</sub> | 88.6 <sub>(84.5–91.9)</sub> | 77.1 <sub>(70.9–80.2)</sub> | 76.2 <sub>(73.3–78.9)</sub> | 94.1 <sub>(92.3–100.7)</sub> |
| footprintOpenKb (MB) | 2.4 <sub>(1.9–3.0)</sub> | 3.5 <sub>(2.7–4.3)</sub> | 7.2 <sub>(5.6–8.8)</sub> | 13.2 <sub>(10.3–16.1)</sub> | 7.1 <sub>(5.5–8.8)</sub> |
| footprintEndKb (MB) | 29.8 <sub>(28.6–31.2)</sub> | 39.3 <sub>(38.2–40.9)</sub> | 71.7 <sub>(63.1–73.5)</sub> | 76.9 <sub>(62.0–89.1)</sub> | 32.3 <sub>(30.4–34.1)</sub> |

**Δ vs S1 (median)**

| Metric | S2 | S5 | S10 | S5c |
|---|---:|---:|---:|---:|
| openMs (ms) | +1.3 | +9.7 | +11.5 | +3.5 |
| observeMs (ms) | -1.6 | -2.1 | -4.6 | -3.6 |
| writeAllMs (ms) | +12.6 | +77.9 | +164.4 | +44.2 |
| writeOneMs (ms) | +0.8 | +1.8 | -1.3 | +1.4 |
| readMs (ms) | -6.0 | -17.5 | -18.4 | -0.5 |
| footprintOpenKb (MB) | +1.1 | +4.7 | +10.7 | +4.7 |
| footprintEndKb (MB) | +9.5 | +41.9 | +47.1 | +2.5 |

## jvm-25-macosx

Source: `results/jvm.jsonl` — runs per scenario: S1: 20, S2: 20, S5: 20, S10: 20, S5c: 20

| Metric | S1 | S2 | S5 | S10 | S5c |
|---|---:|---:|---:|---:|---:|
| openMs (ms) | 272.8 <sub>(261.2–342.0)</sub> | 261.9 <sub>(259.3–275.2)</sub> | 270.9 <sub>(263.8–284.1)</sub> | 284.5 <sub>(273.3–356.8)</sub> | 273.6 <sub>(268.8–300.2)</sub> |
| observeMs (ms) | 35.5 <sub>(34.6–37.4)</sub> | 34.6 <sub>(32.8–36.0)</sub> | 34.2 <sub>(33.3–34.9)</sub> | 31.0 <sub>(29.6–32.0)</sub> | 32.7 <sub>(31.0–33.7)</sub> |
| writeAllMs (ms) | 125.2 <sub>(121.8–134.3)</sub> | 131.8 <sub>(127.7–137.0)</sub> | 203.4 <sub>(198.9–210.4)</sub> | 290.3 <sub>(281.3–301.7)</sub> | 177.3 <sub>(164.2–334.0)</sub> |
| writeOneMs (ms) | 27.9 <sub>(27.3–29.6)</sub> | 22.6 <sub>(21.9–24.1)</sub> | 25.8 <sub>(25.0–26.8)</sub> | 22.7 <sub>(22.4–23.7)</sub> | 23.0 <sub>(22.3–26.1)</sub> |
| readMs (ms) | 66.5 <sub>(62.5–72.1)</sub> | 55.6 <sub>(51.0–59.9)</sub> | 47.8 <sub>(42.1–51.1)</sub> | 46.4 <sub>(41.7–48.3)</sub> | 62.1 <sub>(54.1–73.8)</sub> |
| javaHeapOpenKb (MB) | 0.1 <sub>(0.1–0.1)</sub> | 0.1 <sub>(0.1–0.1)</sub> | 0.1 <sub>(0.1–0.1)</sub> | 0.1 <sub>(0.1–0.1)</sub> | 0.1 <sub>(0.1–0.1)</sub> |
| javaHeapEndKb (MB) | 0.3 <sub>(0.3–0.4)</sub> | 0.3 <sub>(0.3–0.3)</sub> | 0.4 <sub>(0.4–0.4)</sub> | 0.4 <sub>(0.4–0.5)</sub> | 0.3 <sub>(0.3–0.4)</sub> |
| rssOpenKb (MB) | 24.4 <sub>(23.8–24.8)</sub> | 25.6 <sub>(24.6–26.3)</sub> | 29.1 <sub>(27.6–30.6)</sub> | 35.9 <sub>(33.5–39.8)</sub> | 28.5 <sub>(27.4–30.2)</sub> |
| rssEndKb (MB) | 69.0 <sub>(58.6–78.1)</sub> | 81.1 <sub>(67.7–85.9)</sub> | 107.9 <sub>(99.5–115.7)</sub> | 122.1 <sub>(108.5–135.7)</sub> | 77.9 <sub>(66.7–82.6)</sub> |

**Δ vs S1 (median)**

| Metric | S2 | S5 | S10 | S5c |
|---|---:|---:|---:|---:|
| openMs (ms) | -10.9 | -2.0 | +11.6 | +0.7 |
| observeMs (ms) | -0.9 | -1.3 | -4.4 | -2.8 |
| writeAllMs (ms) | +6.6 | +78.2 | +165.1 | +52.1 |
| writeOneMs (ms) | -5.3 | -2.1 | -5.2 | -4.9 |
| readMs (ms) | -10.9 | -18.7 | -20.1 | -4.4 |
| javaHeapOpenKb (MB) | ±0.0 | ±0.0 | +0.1 | ±0.0 |
| javaHeapEndKb (MB) | ±0.0 | +0.1 | +0.1 | ±0.0 |
| rssOpenKb (MB) | +1.2 | +4.6 | +11.4 | +4.1 |
| rssEndKb (MB) | +12.1 | +38.9 | +53.1 | +8.8 |

## android-36

Source: `results/android-large.jsonl` — runs per scenario: L1: 20, L2: 20

| Metric | L1 | L2 |
|---|---:|---:|
| openMs (ms) | 7.7 <sub>(5.6–11.6)</sub> | 15.1 <sub>(8.2–20.8)</sub> |
| weekQueryMs (ms) | 223.7 <sub>(211.3–239.1)</sub> | 215.6 <sub>(206.5–236.5)</sub> |
| monthQueryMs (ms) | 127.6 <sub>(117.9–138.4)</sub> | 126.7 <sub>(117.4–139.3)</sub> |
| agendaJoinMs (ms) | 325.9 <sub>(318.0–339.8)</sub> | 444.2 <sub>(429.9–454.3)</sub> |
| searchContactsMs (ms) | 84.1 <sub>(80.9–85.9)</sub> | 79.0 <sub>(77.3–80.1)</sub> |
| syncContactsMs (ms) | 22.9 <sub>(21.8–27.3)</sub> | 21.4 <sub>(20.8–22.7)</sub> |
| syncCalendarMs (ms) | 164.6 <sub>(157.8–170.6)</sub> | 161.7 <sub>(158.8–167.7)</sub> |
| parallelSyncMs (ms) | 172.6 <sub>(165.1–182.9)</sub> | 156.0 <sub>(150.7–162.4)</sub> |
| javaHeapOpenKb (MB) | -0.0 <sub>(-0.1–0.1)</sub> | -0.0 <sub>(-0.1–0.0)</sub> |
| javaHeapEndKb (MB) | -5.1 <sub>(-5.1–-5.0)</sub> | -5.1 <sub>(-5.1–-5.0)</sub> |
| nativeHeapOpenKb (MB) | 2.3 <sub>(2.3–2.4)</sub> | 3.1 <sub>(3.0–3.1)</sub> |
| nativeHeapEndKb (MB) | 4.6 <sub>(4.6–4.7)</sub> | 10.5 <sub>(10.4–10.5)</sub> |
| pssOpenKb (MB) | 3.8 <sub>(3.7–4.3)</sub> | 4.9 <sub>(4.6–5.1)</sub> |
| pssEndKb (MB) | 23.3 <sub>(23.2–23.6)</sub> | 29.2 <sub>(28.9–29.3)</sub> |

**Δ vs L1 (median)**

| Metric | L2 |
|---|---:|
| openMs (ms) | +7.4 |
| weekQueryMs (ms) | -8.1 |
| monthQueryMs (ms) | -0.9 |
| agendaJoinMs (ms) | +118.3 |
| searchContactsMs (ms) | -5.1 |
| syncContactsMs (ms) | -1.5 |
| syncCalendarMs (ms) | -3.0 |
| parallelSyncMs (ms) | -16.6 |
| javaHeapOpenKb (MB) | ±0.0 |
| javaHeapEndKb (MB) | ±0.0 |
| nativeHeapOpenKb (MB) | +0.8 |
| nativeHeapEndKb (MB) | +5.9 |
| pssOpenKb (MB) | +1.1 |
| pssEndKb (MB) | +5.8 |

## ios-26.5

Source: `results/ios-large.jsonl` — runs per scenario: L1: 20, L2: 20

| Metric | L1 | L2 |
|---|---:|---:|
| openMs (ms) | 9.2 <sub>(4.5–12.8)</sub> | 7.1 <sub>(6.2–15.4)</sub> |
| weekQueryMs (ms) | 125.8 <sub>(111.1–153.9)</sub> | 125.8 <sub>(113.7–140.9)</sub> |
| monthQueryMs (ms) | 132.4 <sub>(124.4–138.4)</sub> | 127.7 <sub>(122.5–136.9)</sub> |
| agendaJoinMs (ms) | 300.3 <sub>(279.1–311.7)</sub> | 357.1 <sub>(351.1–363.2)</sub> |
| searchContactsMs (ms) | 36.5 <sub>(36.0–38.5)</sub> | 35.5 <sub>(34.9–36.6)</sub> |
| syncContactsMs (ms) | 20.3 <sub>(19.6–21.1)</sub> | 19.7 <sub>(19.5–20.3)</sub> |
| syncCalendarMs (ms) | 240.9 <sub>(237.9–266.5)</sub> | 244.0 <sub>(237.2–261.3)</sub> |
| parallelSyncMs (ms) | 276.8 <sub>(257.5–294.2)</sub> | 248.2 <sub>(239.1–275.5)</sub> |
| footprintOpenKb (MB) | 2.5 <sub>(2.5–2.6)</sub> | 3.2 <sub>(3.2–3.3)</sub> |
| footprintEndKb (MB) | 13.7 <sub>(13.5–15.2)</sub> | 21.6 <sub>(20.9–22.3)</sub> |

**Δ vs L1 (median)**

| Metric | L2 |
|---|---:|
| openMs (ms) | -2.1 |
| weekQueryMs (ms) | +0.1 |
| monthQueryMs (ms) | -4.7 |
| agendaJoinMs (ms) | +56.8 |
| searchContactsMs (ms) | -1.1 |
| syncContactsMs (ms) | -0.6 |
| syncCalendarMs (ms) | +3.1 |
| parallelSyncMs (ms) | -28.6 |
| footprintOpenKb (MB) | +0.8 |
| footprintEndKb (MB) | +7.8 |

## macos-26.7

Source: `results/macos-large.jsonl` — runs per scenario: L1: 20, L2: 20

| Metric | L1 | L2 |
|---|---:|---:|
| openMs (ms) | 4.2 <sub>(4.0–4.8)</sub> | 6.1 <sub>(4.8–9.0)</sub> |
| weekQueryMs (ms) | 111.5 <sub>(107.9–116.6)</sub> | 114.0 <sub>(109.3–123.3)</sub> |
| monthQueryMs (ms) | 123.9 <sub>(121.2–126.6)</sub> | 123.0 <sub>(119.0–127.8)</sub> |
| agendaJoinMs (ms) | 271.4 <sub>(268.2–277.4)</sub> | 343.6 <sub>(340.6–349.3)</sub> |
| searchContactsMs (ms) | 36.3 <sub>(35.9–37.9)</sub> | 35.1 <sub>(34.8–36.2)</sub> |
| syncContactsMs (ms) | 15.4 <sub>(15.2–15.6)</sub> | 15.4 <sub>(15.0–15.8)</sub> |
| syncCalendarMs (ms) | 151.3 <sub>(149.3–161.8)</sub> | 150.4 <sub>(147.1–158.0)</sub> |
| parallelSyncMs (ms) | 163.8 <sub>(161.0–168.1)</sub> | 152.0 <sub>(147.8–158.1)</sub> |
| footprintOpenKb (MB) | 2.8 <sub>(2.8–2.9)</sub> | 3.7 <sub>(3.6–3.7)</sub> |
| footprintEndKb (MB) | 20.1 <sub>(18.9–23.1)</sub> | 26.6 <sub>(26.0–27.1)</sub> |

**Δ vs L1 (median)**

| Metric | L2 |
|---|---:|
| openMs (ms) | +1.8 |
| weekQueryMs (ms) | +2.6 |
| monthQueryMs (ms) | -0.9 |
| agendaJoinMs (ms) | +72.2 |
| searchContactsMs (ms) | -1.2 |
| syncContactsMs (ms) | ±0.0 |
| syncCalendarMs (ms) | -0.9 |
| parallelSyncMs (ms) | -11.8 |
| footprintOpenKb (MB) | +0.8 |
| footprintEndKb (MB) | +6.5 |

## jvm-25-macosx

Source: `results/jvm-large.jsonl` — runs per scenario: L1: 20, L2: 20

| Metric | L1 | L2 |
|---|---:|---:|
| openMs (ms) | 249.4 <sub>(246.8–267.9)</sub> | 268.2 <sub>(257.2–311.4)</sub> |
| weekQueryMs (ms) | 113.1 <sub>(108.2–130.3)</sub> | 114.4 <sub>(107.1–132.6)</sub> |
| monthQueryMs (ms) | 104.5 <sub>(100.6–110.1)</sub> | 105.1 <sub>(103.4–111.3)</sub> |
| agendaJoinMs (ms) | 286.1 <sub>(284.1–300.1)</sub> | 387.2 <sub>(380.6–400.3)</sub> |
| searchContactsMs (ms) | 46.3 <sub>(45.9–48.3)</sub> | 43.7 <sub>(43.2–44.1)</sub> |
| syncContactsMs (ms) | 19.3 <sub>(18.4–19.8)</sub> | 18.4 <sub>(18.0–18.8)</sub> |
| syncCalendarMs (ms) | 159.1 <sub>(153.8–165.3)</sub> | 154.9 <sub>(153.0–158.6)</sub> |
| parallelSyncMs (ms) | 167.3 <sub>(165.7–170.4)</sub> | 153.4 <sub>(150.4–165.2)</sub> |
| javaHeapOpenKb (MB) | -0.3 <sub>(-0.3–-0.3)</sub> | -0.3 <sub>(-0.3–-0.3)</sub> |
| javaHeapEndKb (MB) | -4.9 <sub>(-4.9–-4.9)</sub> | -4.9 <sub>(-4.9–-4.9)</sub> |
| rssOpenKb (MB) | 16.1 <sub>(16.0–16.1)</sub> | 16.9 <sub>(16.7–17.0)</sub> |
| rssEndKb (MB) | 49.2 <sub>(48.9–49.7)</sub> | 77.6 <sub>(76.3–78.0)</sub> |

**Δ vs L1 (median)**

| Metric | L2 |
|---|---:|
| openMs (ms) | +18.7 |
| weekQueryMs (ms) | +1.4 |
| monthQueryMs (ms) | +0.5 |
| agendaJoinMs (ms) | +101.1 |
| searchContactsMs (ms) | -2.6 |
| syncContactsMs (ms) | -0.9 |
| syncCalendarMs (ms) | -4.2 |
| parallelSyncMs (ms) | -13.9 |
| javaHeapOpenKb (MB) | ±0.0 |
| javaHeapEndKb (MB) | +0.1 |
| rssOpenKb (MB) | +0.8 |
| rssEndKb (MB) | +28.4 |
