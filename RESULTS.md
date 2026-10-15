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

## ios-26.5 (app)

Source: `results/ios-app.jsonl` — runs per scenario: S1: 20, S2: 20, S5: 20, S10: 20, S5c: 20

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

## ios-26.5 (app)

Source: `results/ios-app-large.jsonl` — runs per scenario: L1: 20, L2: 20

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

## ios-26.5

Source: `results/ios-large.jsonl` — runs per scenario: L1: 20, L2: 20

| Metric | L1 | L2 |
|---|---:|---:|
| openMs (ms) | 5.6 <sub>(4.8–9.2)</sub> | 8.6 <sub>(7.8–11.6)</sub> |
| weekQueryMs (ms) | 115.4 <sub>(111.8–132.8)</sub> | 115.2 <sub>(109.2–131.1)</sub> |
| monthQueryMs (ms) | 121.0 <sub>(117.5–126.2)</sub> | 124.2 <sub>(116.2–129.0)</sub> |
| agendaJoinMs (ms) | 270.6 <sub>(265.8–277.3)</sub> | 353.1 <sub>(342.0–366.5)</sub> |
| searchContactsMs (ms) | 37.2 <sub>(36.1–38.6)</sub> | 36.0 <sub>(35.6–38.7)</sub> |
| syncContactsMs (ms) | 19.5 <sub>(18.8–20.0)</sub> | 19.6 <sub>(18.9–20.8)</sub> |
| syncCalendarMs (ms) | 246.7 <sub>(239.4–260.3)</sub> | 241.2 <sub>(236.5–256.6)</sub> |
| parallelSyncMs (ms) | 260.6 <sub>(252.4–274.7)</sub> | 238.5 <sub>(236.0–243.7)</sub> |
| footprintOpenKb (MB) | 2.8 <sub>(2.8–2.8)</sub> | 3.6 <sub>(3.6–3.6)</sub> |
| footprintEndKb (MB) | 14.8 <sub>(14.3–15.1)</sub> | 22.3 <sub>(21.6–22.8)</sub> |

**Δ vs L1 (median)**

| Metric | L2 |
|---|---:|
| openMs (ms) | +3.1 |
| weekQueryMs (ms) | -0.2 |
| monthQueryMs (ms) | +3.2 |
| agendaJoinMs (ms) | +82.6 |
| searchContactsMs (ms) | -1.2 |
| syncContactsMs (ms) | +0.2 |
| syncCalendarMs (ms) | -5.5 |
| parallelSyncMs (ms) | -22.1 |
| footprintOpenKb (MB) | +0.8 |
| footprintEndKb (MB) | +7.5 |
