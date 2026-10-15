# Multi-database benchmark results (Room & SQLDelight)

**Legend** — `openMs`: build + first query of every database (open, schema check); `observeMs`: time until a Flow on each of the 10 tables emitted once; `writeAllMs`: 200 actions each writing 1 row into all 10 tables (1 transaction per database); `writeOneMs`: 200 transactions writing 1 row into table t0 only; `readMs`: 8 concurrent coroutines reading latest 200 rows of every table ×10 + full scans; `*OpenKb`: memory increase after opening; `*EndKb`: memory increase at the end (all connections open, caches warm). Large-volume suite (L1 = 1 DB, L2 = contacts DB + calendar DB, ~120 MB): `weekQueryMs`: 52 week queries (3 calendars); `monthQueryMs`: 12 month queries (10 calendars); `agendaJoinMs`: 12 months of agenda with contact names (L1 SQL JOIN, L2 in-memory join); `searchContactsMs`: 100 prefix searches; `syncContactsMs`: 2,000-contact upsert; `syncCalendarMs`: 5,000 events + attendees; `parallelSyncMs`: both syncs concurrently. Times in ms, memory in MB (delta vs process baseline). Cells: median (Q1–Q3).

## Room · android-36

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

## Room · ios-26.5

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

## Room · macos-26.7

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

## Room · jvm-27-macosx

Source: `results/jvm.jsonl` — runs per scenario: S1: 20, S2: 20, S5: 20, S10: 20, S5c: 20 — SQLite 3.50.1

| Metric | S1 | S2 | S5 | S10 | S5c |
|---|---:|---:|---:|---:|---:|
| openMs (ms) | 269.3 <sub>(252.5–332.8)</sub> | 270.0 <sub>(253.8–292.8)</sub> | 270.3 <sub>(257.6–289.3)</sub> | 291.1 <sub>(269.7–344.9)</sub> | 275.6 <sub>(259.1–327.2)</sub> |
| observeMs (ms) | 33.9 <sub>(32.4–35.1)</sub> | 31.8 <sub>(31.2–34.9)</sub> | 31.3 <sub>(30.2–34.1)</sub> | 29.5 <sub>(27.5–31.4)</sub> | 29.9 <sub>(29.1–31.8)</sub> |
| writeAllMs (ms) | 125.5 <sub>(121.2–131.1)</sub> | 132.8 <sub>(128.1–140.7)</sub> | 200.4 <sub>(194.9–210.0)</sub> | 285.4 <sub>(276.8–301.5)</sub> | 179.2 <sub>(161.7–316.5)</sub> |
| writeOneMs (ms) | 27.3 <sub>(26.7–28.6)</sub> | 22.8 <sub>(21.8–23.7)</sub> | 25.2 <sub>(24.4–26.7)</sub> | 23.3 <sub>(21.5–24.6)</sub> | 25.9 <sub>(24.1–32.3)</sub> |
| readMs (ms) | 67.9 <sub>(61.3–73.0)</sub> | 56.6 <sub>(52.2–60.0)</sub> | 46.4 <sub>(43.4–55.1)</sub> | 44.5 <sub>(40.5–45.6)</sub> | 61.6 <sub>(54.6–75.1)</sub> |
| javaHeapOpenKb (MB) | -0.4 <sub>(-0.4–-0.4)</sub> | -0.4 <sub>(-0.4–-0.4)</sub> | -0.3 <sub>(-0.3–-0.3)</sub> | -0.3 <sub>(-0.3–-0.3)</sub> | -0.3 <sub>(-0.4–-0.3)</sub> |
| javaHeapEndKb (MB) | 0.0 <sub>(0.0–0.2)</sub> | 0.2 <sub>(0.2–0.2)</sub> | 0.1 <sub>(0.1–0.1)</sub> | 0.1 <sub>(0.1–0.1)</sub> | 0.1 <sub>(0.1–0.1)</sub> |
| rssOpenKb (MB) | 19.1 <sub>(18.5–19.6)</sub> | 19.9 <sub>(19.3–20.7)</sub> | 23.8 <sub>(22.6–25.3)</sub> | 29.9 <sub>(27.7–32.8)</sub> | 24.3 <sub>(22.7–25.3)</sub> |
| rssEndKb (MB) | 105.3 <sub>(101.6–108.7)</sub> | 109.7 <sub>(107.9–112.3)</sub> | 133.8 <sub>(129.7–137.8)</sub> | 148.0 <sub>(139.1–156.2)</sub> | 108.1 <sub>(101.9–112.8)</sub> |

**Δ vs S1 (median)**

| Metric | S2 | S5 | S10 | S5c |
|---|---:|---:|---:|---:|
| openMs (ms) | +0.7 | +1.0 | +21.8 | +6.3 |
| observeMs (ms) | -2.1 | -2.6 | -4.5 | -4.0 |
| writeAllMs (ms) | +7.3 | +74.9 | +159.9 | +53.7 |
| writeOneMs (ms) | -4.6 | -2.1 | -4.1 | -1.5 |
| readMs (ms) | -11.3 | -21.5 | -23.4 | -6.3 |
| javaHeapOpenKb (MB) | ±0.0 | ±0.0 | +0.1 | ±0.0 |
| javaHeapEndKb (MB) | +0.2 | +0.1 | ±0.0 | +0.1 |
| rssOpenKb (MB) | +0.8 | +4.7 | +10.8 | +5.2 |
| rssEndKb (MB) | +4.4 | +28.6 | +42.7 | +2.8 |

## Room · android-36 · large volume

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

## Room · ios-26.5 · large volume

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

## Room · macos-26.7 · large volume

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

## Room · jvm-27-macosx · large volume

Source: `results/jvm-large.jsonl` — runs per scenario: L1: 20, L2: 20 — SQLite 3.50.1

| Metric | L1 | L2 |
|---|---:|---:|
| openMs (ms) | 255.4 <sub>(242.9–315.9)</sub> | 272.7 <sub>(249.7–325.7)</sub> |
| weekQueryMs (ms) | 135.1 <sub>(123.8–142.5)</sub> | 123.8 <sub>(118.9–143.2)</sub> |
| monthQueryMs (ms) | 109.6 <sub>(106.3–112.8)</sub> | 109.0 <sub>(106.5–111.4)</sub> |
| agendaJoinMs (ms) | 291.0 <sub>(287.0–305.7)</sub> | 383.7 <sub>(379.6–388.5)</sub> |
| searchContactsMs (ms) | 46.2 <sub>(45.7–47.0)</sub> | 44.1 <sub>(43.5–44.5)</sub> |
| syncContactsMs (ms) | 19.4 <sub>(19.0–20.1)</sub> | 18.6 <sub>(18.2–19.3)</sub> |
| syncCalendarMs (ms) | 159.4 <sub>(157.0–163.3)</sub> | 157.9 <sub>(153.1–171.0)</sub> |
| parallelSyncMs (ms) | 166.0 <sub>(164.4–169.3)</sub> | 150.3 <sub>(147.8–161.6)</sub> |
| javaHeapOpenKb (MB) | -0.5 <sub>(-0.5–-0.5)</sub> | -0.5 <sub>(-0.5–-0.5)</sub> |
| javaHeapEndKb (MB) | -4.9 <sub>(-4.9–-4.9)</sub> | -4.8 <sub>(-4.8–-4.7)</sub> |
| rssOpenKb (MB) | 10.5 <sub>(10.3–10.6)</sub> | 11.1 <sub>(11.0–11.3)</sub> |
| rssEndKb (MB) | 49.4 <sub>(49.0–49.6)</sub> | 79.3 <sub>(74.5–83.2)</sub> |

**Δ vs L1 (median)**

| Metric | L2 |
|---|---:|
| openMs (ms) | +17.4 |
| weekQueryMs (ms) | -11.3 |
| monthQueryMs (ms) | -0.6 |
| agendaJoinMs (ms) | +92.7 |
| searchContactsMs (ms) | -2.2 |
| syncContactsMs (ms) | -0.8 |
| syncCalendarMs (ms) | -1.4 |
| parallelSyncMs (ms) | -15.7 |
| javaHeapOpenKb (MB) | ±0.0 |
| javaHeapEndKb (MB) | +0.1 |
| rssOpenKb (MB) | +0.6 |
| rssEndKb (MB) | +30.0 |

## SQLDelight · android-36

Source: `results/android-sqldelight.jsonl` — runs per scenario: S1: 20, S2: 20, S5: 20, S10: 20 — SQLite 3.50.4

| Metric | S1 | S2 | S5 | S10 |
|---|---:|---:|---:|---:|
| openMs (ms) | 33.8 <sub>(25.8–40.4)</sub> | 63.8 <sub>(51.8–78.2)</sub> | 134.5 <sub>(125.5–180.9)</sub> | 234.0 <sub>(210.1–249.6)</sub> |
| observeMs (ms) | 14.0 <sub>(11.9–18.3)</sub> | 12.5 <sub>(10.5–14.9)</sub> | 7.0 <sub>(5.9–10.7)</sub> | 5.2 <sub>(3.5–6.4)</sub> |
| writeAllMs (ms) | 736.8 <sub>(685.7–773.5)</sub> | 726.1 <sub>(671.1–828.9)</sub> | 698.8 <sub>(648.2–731.8)</sub> | 796.5 <sub>(782.0–808.9)</sub> |
| writeOneMs (ms) | 51.2 <sub>(47.2–59.9)</sub> | 45.0 <sub>(42.2–49.9)</sub> | 45.8 <sub>(41.0–50.1)</sub> | 51.5 <sub>(44.9–56.5)</sub> |
| readMs (ms) | 123.7 <sub>(112.1–130.8)</sub> | 115.3 <sub>(100.6–130.6)</sub> | 109.9 <sub>(102.3–117.7)</sub> | 112.4 <sub>(106.4–120.1)</sub> |
| javaHeapOpenKb (MB) | 0.0 <sub>(-0.0–0.1)</sub> | 0.0 <sub>(-0.0–0.1)</sub> | -0.0 <sub>(-0.1–0.1)</sub> | -0.0 <sub>(-0.1–0.0)</sub> |
| javaHeapEndKb (MB) | 0.0 <sub>(-0.0–0.1)</sub> | 0.0 <sub>(0.0–0.1)</sub> | 0.1 <sub>(0.0–0.2)</sub> | 0.2 <sub>(0.2–0.2)</sub> |
| nativeHeapOpenKb (MB) | 2.2 <sub>(1.7–2.4)</sub> | 3.3 <sub>(2.8–4.1)</sub> | 7.0 <sub>(5.0–8.2)</sub> | 9.1 <sub>(9.0–9.8)</sub> |
| nativeHeapEndKb (MB) | 8.8 <sub>(8.8–9.2)</sub> | 9.3 <sub>(8.9–9.7)</sub> | 9.2 <sub>(9.2–10.0)</sub> | 9.9 <sub>(9.6–10.3)</sub> |
| pssOpenKb (MB) | 3.1 <sub>(2.6–3.8)</sub> | 4.6 <sub>(3.8–5.2)</sub> | 8.0 <sub>(6.2–9.8)</sub> | 10.7 <sub>(10.2–11.3)</sub> |
| pssEndKb (MB) | 11.9 <sub>(11.5–12.3)</sub> | 12.4 <sub>(11.7–12.9)</sub> | 12.7 <sub>(12.2–13.6)</sub> | 13.9 <sub>(13.4–14.3)</sub> |

**Δ vs S1 (median)**

| Metric | S2 | S5 | S10 |
|---|---:|---:|---:|
| openMs (ms) | +30.0 | +100.6 | +200.2 |
| observeMs (ms) | -1.5 | -7.0 | -8.8 |
| writeAllMs (ms) | -10.8 | -38.0 | +59.7 |
| writeOneMs (ms) | -6.2 | -5.4 | +0.4 |
| readMs (ms) | -8.4 | -13.7 | -11.3 |
| javaHeapOpenKb (MB) | ±0.0 | ±0.0 | ±0.0 |
| javaHeapEndKb (MB) | ±0.0 | +0.1 | +0.2 |
| nativeHeapOpenKb (MB) | +1.1 | +4.9 | +6.9 |
| nativeHeapEndKb (MB) | +0.4 | +0.3 | +1.0 |
| pssOpenKb (MB) | +1.5 | +4.9 | +7.6 |
| pssEndKb (MB) | +0.6 | +0.8 | +2.0 |

## SQLDelight · ios-26.5

Source: `results/ios-sqldelight.jsonl` — runs per scenario: S1: 20, S2: 20, S5: 20, S10: 20 — SQLite 3.50.1

| Metric | S1 | S2 | S5 | S10 |
|---|---:|---:|---:|---:|
| openMs (ms) | 4.0 <sub>(3.5–4.3)</sub> | 6.4 <sub>(5.8–7.1)</sub> | 15.3 <sub>(13.7–16.3)</sub> | 25.3 <sub>(24.2–27.2)</sub> |
| observeMs (ms) | 4.2 <sub>(3.4–5.2)</sub> | 4.1 <sub>(3.3–5.1)</sub> | 2.0 <sub>(1.6–2.7)</sub> | 0.7 <sub>(0.6–0.9)</sub> |
| writeAllMs (ms) | 52.3 <sub>(49.6–56.5)</sub> | 63.6 <sub>(59.9–68.1)</sub> | 117.1 <sub>(114.6–120.6)</sub> | 202.8 <sub>(198.8–207.9)</sub> |
| writeOneMs (ms) | 11.0 <sub>(9.6–12.2)</sub> | 12.1 <sub>(11.4–12.7)</sub> | 13.7 <sub>(13.2–14.2)</sub> | 11.3 <sub>(10.9–11.8)</sub> |
| readMs (ms) | 177.8 <sub>(169.7–187.0)</sub> | 147.3 <sub>(140.4–163.7)</sub> | 79.9 <sub>(69.3–88.0)</sub> | 93.7 <sub>(91.5–97.6)</sub> |
| footprintOpenKb (MB) | 2.0 <sub>(1.4–2.6)</sub> | 3.0 <sub>(2.2–3.9)</sub> | 6.6 <sub>(4.9–8.2)</sub> | 12.6 <sub>(9.5–15.6)</sub> |
| footprintEndKb (MB) | 14.5 <sub>(13.4–14.9)</sub> | 16.7 <sub>(14.6–17.8)</sub> | 19.2 <sub>(18.2–20.2)</sub> | 21.6 <sub>(20.7–22.5)</sub> |

**Δ vs S1 (median)**

| Metric | S2 | S5 | S10 |
|---|---:|---:|---:|
| openMs (ms) | +2.4 | +11.3 | +21.3 |
| observeMs (ms) | -0.2 | -2.2 | -3.5 |
| writeAllMs (ms) | +11.3 | +64.8 | +150.5 |
| writeOneMs (ms) | +1.1 | +2.7 | +0.2 |
| readMs (ms) | -30.5 | -97.9 | -84.0 |
| footprintOpenKb (MB) | +1.0 | +4.6 | +10.6 |
| footprintEndKb (MB) | +2.2 | +4.7 | +7.1 |

## SQLDelight · macos-26.7

Source: `results/macos-sqldelight.jsonl` — runs per scenario: S1: 20, S2: 20, S5: 20, S10: 20 — SQLite 3.50.1

| Metric | S1 | S2 | S5 | S10 |
|---|---:|---:|---:|---:|
| openMs (ms) | 3.9 <sub>(2.8–4.7)</sub> | 8.4 <sub>(5.4–10.0)</sub> | 12.8 <sub>(8.6–17.4)</sub> | 18.1 <sub>(15.2–19.2)</sub> |
| observeMs (ms) | 8.4 <sub>(5.2–12.7)</sub> | 9.6 <sub>(5.0–14.0)</sub> | 4.8 <sub>(2.9–6.4)</sub> | 1.4 <sub>(1.1–2.3)</sub> |
| writeAllMs (ms) | 39.7 <sub>(38.0–42.5)</sub> | 46.5 <sub>(44.4–53.4)</sub> | 92.3 <sub>(87.3–98.3)</sub> | 179.9 <sub>(174.6–185.0)</sub> |
| writeOneMs (ms) | 7.4 <sub>(6.7–9.2)</sub> | 9.0 <sub>(8.3–9.6)</sub> | 10.6 <sub>(10.1–11.8)</sub> | 9.2 <sub>(8.8–9.5)</sub> |
| readMs (ms) | 186.9 <sub>(170.5–195.0)</sub> | 150.7 <sub>(141.4–167.5)</sub> | 79.5 <sub>(74.8–87.8)</sub> | 90.4 <sub>(85.1–93.9)</sub> |
| footprintOpenKb (MB) | 2.2 <sub>(1.7–2.8)</sub> | 3.3 <sub>(2.4–4.1)</sub> | 6.8 <sub>(5.2–8.4)</sub> | 12.6 <sub>(9.6–15.5)</sub> |
| footprintEndKb (MB) | 19.0 <sub>(18.2–19.7)</sub> | 24.4 <sub>(23.4–24.9)</sub> | 30.9 <sub>(29.9–31.9)</sub> | 35.1 <sub>(34.2–37.8)</sub> |

**Δ vs S1 (median)**

| Metric | S2 | S5 | S10 |
|---|---:|---:|---:|
| openMs (ms) | +4.5 | +8.9 | +14.2 |
| observeMs (ms) | +1.3 | -3.6 | -7.0 |
| writeAllMs (ms) | +6.8 | +52.6 | +140.3 |
| writeOneMs (ms) | +1.6 | +3.2 | +1.8 |
| readMs (ms) | -36.2 | -107.4 | -96.5 |
| footprintOpenKb (MB) | +1.1 | +4.5 | +10.4 |
| footprintEndKb (MB) | +5.4 | +11.9 | +16.1 |

## SQLDelight · jvm-27-macosx

Source: `results/jvm-sqldelight.jsonl` — runs per scenario: S1: 20, S2: 20, S5: 20, S10: 20 — SQLite 3.53.4

| Metric | S1 | S2 | S5 | S10 |
|---|---:|---:|---:|---:|
| openMs (ms) | 330.7 <sub>(291.0–373.3)</sub> | 349.2 <sub>(299.6–369.9)</sub> | 317.9 <sub>(303.7–371.9)</sub> | 318.1 <sub>(316.7–333.6)</sub> |
| observeMs (ms) | 20.2 <sub>(18.8–21.5)</sub> | 20.1 <sub>(17.7–21.4)</sub> | 19.7 <sub>(18.1–21.8)</sub> | 20.8 <sub>(19.4–24.2)</sub> |
| writeAllMs (ms) | 288.3 <sub>(266.6–320.8)</sub> | 473.1 <sub>(447.7–527.1)</sub> | 1214.3 <sub>(1166.1–1269.0)</sub> | 2020.3 <sub>(1959.9–2098.8)</sub> |
| writeOneMs (ms) | 132.0 <sub>(122.8–141.8)</sub> | 116.7 <sub>(92.8–128.6)</sub> | 95.5 <sub>(84.5–110.7)</sub> | 127.6 <sub>(97.5–163.7)</sub> |
| readMs (ms) | 125.4 <sub>(119.1–131.1)</sub> | 129.9 <sub>(121.0–138.0)</sub> | 182.2 <sub>(173.0–186.7)</sub> | 223.7 <sub>(204.3–237.6)</sub> |
| javaHeapOpenKb (MB) | 0.4 <sub>(0.4–0.4)</sub> | 0.5 <sub>(0.5–0.5)</sub> | 0.5 <sub>(0.5–0.5)</sub> | 0.5 <sub>(0.5–0.5)</sub> |
| javaHeapEndKb (MB) | 0.7 <sub>(0.7–0.7)</sub> | 0.7 <sub>(0.7–0.7)</sub> | 0.7 <sub>(0.7–0.7)</sub> | 0.7 <sub>(0.7–0.7)</sub> |
| rssOpenKb (MB) | 23.5 <sub>(23.1–24.2)</sub> | 23.1 <sub>(22.6–23.9)</sub> | 23.6 <sub>(23.3–24.8)</sub> | 23.5 <sub>(23.0–24.2)</sub> |
| rssEndKb (MB) | 128.0 <sub>(125.2–133.1)</sub> | 127.5 <sub>(125.3–129.2)</sub> | 123.2 <sub>(120.1–127.4)</sub> | 120.1 <sub>(116.5–123.5)</sub> |

**Δ vs S1 (median)**

| Metric | S2 | S5 | S10 |
|---|---:|---:|---:|
| openMs (ms) | +18.5 | -12.8 | -12.6 |
| observeMs (ms) | -0.1 | -0.5 | +0.6 |
| writeAllMs (ms) | +184.8 | +926.1 | +1732.0 |
| writeOneMs (ms) | -15.3 | -36.5 | -4.4 |
| readMs (ms) | +4.5 | +56.8 | +98.3 |
| javaHeapOpenKb (MB) | ±0.0 | ±0.0 | ±0.0 |
| javaHeapEndKb (MB) | ±0.0 | ±0.0 | ±0.0 |
| rssOpenKb (MB) | -0.4 | +0.1 | ±0.0 |
| rssEndKb (MB) | -0.5 | -4.8 | -7.9 |

## SQLDelight · android-36 · large volume

Source: `results/android-sqldelight-large.jsonl` — runs per scenario: L1: 20, L2: 20 — SQLite 3.50.4

| Metric | L1 | L2 |
|---|---:|---:|
| openMs (ms) | 37.9 <sub>(34.1–48.8)</sub> | 58.7 <sub>(48.7–64.4)</sub> |
| weekQueryMs (ms) | 223.9 <sub>(206.1–240.8)</sub> | 233.0 <sub>(211.5–253.2)</sub> |
| monthQueryMs (ms) | 160.1 <sub>(141.1–163.2)</sub> | 149.4 <sub>(147.1–158.3)</sub> |
| agendaJoinMs (ms) | 333.9 <sub>(319.5–347.1)</sub> | 450.0 <sub>(431.0–453.7)</sub> |
| searchContactsMs (ms) | 52.4 <sub>(51.3–54.7)</sub> | 52.5 <sub>(51.0–54.1)</sub> |
| syncContactsMs (ms) | 17.9 <sub>(17.0–19.2)</sub> | 17.8 <sub>(16.2–18.4)</sub> |
| syncCalendarMs (ms) | 144.5 <sub>(138.9–159.3)</sub> | 144.7 <sub>(139.3–155.6)</sub> |
| parallelSyncMs (ms) | 154.5 <sub>(147.9–163.8)</sub> | 144.4 <sub>(132.9–150.1)</sub> |
| javaHeapOpenKb (MB) | -0.1 <sub>(-0.2–-0.0)</sub> | -0.1 <sub>(-0.2–-0.1)</sub> |
| javaHeapEndKb (MB) | -5.1 <sub>(-5.1–-5.0)</sub> | -5.0 <sub>(-5.0–-4.9)</sub> |
| nativeHeapOpenKb (MB) | 2.3 <sub>(2.3–2.4)</sub> | 3.2 <sub>(3.2–3.3)</sub> |
| nativeHeapEndKb (MB) | 4.5 <sub>(4.5–4.6)</sub> | 8.5 <sub>(8.5–8.6)</sub> |
| pssOpenKb (MB) | 2.7 <sub>(2.6–3.1)</sub> | 3.6 <sub>(3.5–3.9)</sub> |
| pssEndKb (MB) | 3.6 <sub>(3.4–3.9)</sub> | 8.5 <sub>(8.5–8.8)</sub> |

**Δ vs L1 (median)**

| Metric | L2 |
|---|---:|
| openMs (ms) | +20.8 |
| weekQueryMs (ms) | +9.1 |
| monthQueryMs (ms) | -10.6 |
| agendaJoinMs (ms) | +116.1 |
| searchContactsMs (ms) | +0.1 |
| syncContactsMs (ms) | -0.1 |
| syncCalendarMs (ms) | +0.2 |
| parallelSyncMs (ms) | -10.1 |
| javaHeapOpenKb (MB) | ±0.0 |
| javaHeapEndKb (MB) | +0.1 |
| nativeHeapOpenKb (MB) | +0.8 |
| nativeHeapEndKb (MB) | +3.9 |
| pssOpenKb (MB) | +0.8 |
| pssEndKb (MB) | +5.0 |

## SQLDelight · ios-26.5 · large volume

Source: `results/ios-sqldelight-large.jsonl` — runs per scenario: L1: 20, L2: 20 — SQLite 3.50.1

| Metric | L1 | L2 |
|---|---:|---:|
| openMs (ms) | 8.4 <sub>(4.8–13.2)</sub> | 14.3 <sub>(6.4–18.1)</sub> |
| weekQueryMs (ms) | 147.9 <sub>(136.1–168.4)</sub> | 153.2 <sub>(135.5–165.3)</sub> |
| monthQueryMs (ms) | 161.9 <sub>(157.2–170.5)</sub> | 163.0 <sub>(158.6–168.1)</sub> |
| agendaJoinMs (ms) | 342.7 <sub>(330.9–366.1)</sub> | 477.4 <sub>(456.2–490.6)</sub> |
| searchContactsMs (ms) | 41.4 <sub>(40.3–42.5)</sub> | 40.4 <sub>(39.4–42.8)</sub> |
| syncContactsMs (ms) | 12.2 <sub>(11.8–12.9)</sub> | 12.0 <sub>(11.8–12.5)</sub> |
| syncCalendarMs (ms) | 253.7 <sub>(243.1–261.4)</sub> | 245.7 <sub>(237.1–258.6)</sub> |
| parallelSyncMs (ms) | 263.7 <sub>(254.1–277.6)</sub> | 248.5 <sub>(240.3–254.1)</sub> |
| footprintOpenKb (MB) | 2.4 <sub>(2.4–2.6)</sub> | 3.3 <sub>(3.2–3.3)</sub> |
| footprintEndKb (MB) | 13.7 <sub>(13.1–13.9)</sub> | 17.6 <sub>(17.4–18.1)</sub> |

**Δ vs L1 (median)**

| Metric | L2 |
|---|---:|
| openMs (ms) | +5.9 |
| weekQueryMs (ms) | +5.3 |
| monthQueryMs (ms) | +1.0 |
| agendaJoinMs (ms) | +134.7 |
| searchContactsMs (ms) | -1.0 |
| syncContactsMs (ms) | -0.3 |
| syncCalendarMs (ms) | -8.0 |
| parallelSyncMs (ms) | -15.2 |
| footprintOpenKb (MB) | +0.9 |
| footprintEndKb (MB) | +3.9 |

## SQLDelight · macos-26.7 · large volume

Source: `results/macos-sqldelight-large.jsonl` — runs per scenario: L1: 20, L2: 20 — SQLite 3.50.1

| Metric | L1 | L2 |
|---|---:|---:|
| openMs (ms) | 3.9 <sub>(3.7–4.5)</sub> | 4.9 <sub>(4.5–6.0)</sub> |
| weekQueryMs (ms) | 132.0 <sub>(130.6–135.1)</sub> | 137.0 <sub>(133.4–142.9)</sub> |
| monthQueryMs (ms) | 154.5 <sub>(150.5–157.6)</sub> | 152.0 <sub>(150.0–156.4)</sub> |
| agendaJoinMs (ms) | 334.4 <sub>(324.9–337.4)</sub> | 465.1 <sub>(460.4–480.3)</sub> |
| searchContactsMs (ms) | 40.9 <sub>(40.3–43.2)</sub> | 40.6 <sub>(39.9–40.9)</sub> |
| syncContactsMs (ms) | 10.1 <sub>(10.0–10.2)</sub> | 9.9 <sub>(9.7–9.9)</sub> |
| syncCalendarMs (ms) | 154.2 <sub>(151.8–156.8)</sub> | 147.4 <sub>(146.7–153.2)</sub> |
| parallelSyncMs (ms) | 159.9 <sub>(158.2–162.0)</sub> | 149.9 <sub>(148.5–154.8)</sub> |
| footprintOpenKb (MB) | 2.7 <sub>(2.7–2.8)</sub> | 3.6 <sub>(3.5–3.6)</sub> |
| footprintEndKb (MB) | 30.6 <sub>(27.1–31.5)</sub> | 32.9 <sub>(32.4–36.1)</sub> |

**Δ vs L1 (median)**

| Metric | L2 |
|---|---:|
| openMs (ms) | +1.0 |
| weekQueryMs (ms) | +5.0 |
| monthQueryMs (ms) | -2.6 |
| agendaJoinMs (ms) | +130.7 |
| searchContactsMs (ms) | -0.3 |
| syncContactsMs (ms) | -0.2 |
| syncCalendarMs (ms) | -6.8 |
| parallelSyncMs (ms) | -9.9 |
| footprintOpenKb (MB) | +0.8 |
| footprintEndKb (MB) | +2.3 |

## SQLDelight · jvm-27-macosx · large volume

Source: `results/jvm-sqldelight-large.jsonl` — runs per scenario: L1: 20, L2: 20 — SQLite 3.53.4

| Metric | L1 | L2 |
|---|---:|---:|
| openMs (ms) | 291.9 <sub>(282.9–312.2)</sub> | 294.2 <sub>(286.8–321.4)</sub> |
| weekQueryMs (ms) | 225.8 <sub>(220.0–251.6)</sub> | 233.4 <sub>(221.4–263.1)</sub> |
| monthQueryMs (ms) | 122.7 <sub>(120.4–131.8)</sub> | 128.8 <sub>(121.5–133.7)</sub> |
| agendaJoinMs (ms) | 342.9 <sub>(334.2–352.8)</sub> | 533.3 <sub>(517.1–549.8)</sub> |
| searchContactsMs (ms) | 122.9 <sub>(118.6–130.3)</sub> | 117.4 <sub>(116.2–121.6)</sub> |
| syncContactsMs (ms) | 28.5 <sub>(28.1–29.4)</sub> | 29.2 <sub>(29.0–30.4)</sub> |
| syncCalendarMs (ms) | 224.9 <sub>(221.7–230.2)</sub> | 222.8 <sub>(220.6–231.4)</sub> |
| parallelSyncMs (ms) | 232.6 <sub>(225.7–240.7)</sub> | 211.3 <sub>(205.5–218.4)</sub> |
| javaHeapOpenKb (MB) | 0.4 <sub>(0.4–0.4)</sub> | 0.4 <sub>(0.4–0.4)</sub> |
| javaHeapEndKb (MB) | -4.0 <sub>(-4.0–-4.0)</sub> | -4.0 <sub>(-4.0–-4.0)</sub> |
| rssOpenKb (MB) | 14.1 <sub>(14.0–14.2)</sub> | 14.2 <sub>(14.0–14.3)</sub> |
| rssEndKb (MB) | 92.2 <sub>(91.7–93.5)</sub> | 96.6 <sub>(95.9–97.5)</sub> |

**Δ vs L1 (median)**

| Metric | L2 |
|---|---:|
| openMs (ms) | +2.3 |
| weekQueryMs (ms) | +7.6 |
| monthQueryMs (ms) | +6.1 |
| agendaJoinMs (ms) | +190.4 |
| searchContactsMs (ms) | -5.6 |
| syncContactsMs (ms) | +0.7 |
| syncCalendarMs (ms) | -2.1 |
| parallelSyncMs (ms) | -21.3 |
| javaHeapOpenKb (MB) | ±0.0 |
| javaHeapEndKb (MB) | ±0.0 |
| rssOpenKb (MB) | ±0.0 |
| rssEndKb (MB) | +4.4 |
