# Room KMP : coût de plusieurs bases de données

**Question :** répartir un même schéma Room sur plusieurs bases SQLite (par ex. contacts + calendrier) a-t-il un coût de performance significatif sur Android et iOS ?

## TL;DR

Pour **2 bases** (le cas contacts + calendrier), par rapport à une base unique :

| | Android (émulateur API 36) | iOS app SwiftUI (simulateur 26.5) | iOS exécutable K/N |
|---|---:|---:|---:|
| Ouverture (build + 1re requête) | **+4,9 ms** | **+1,7 ms** | **+2,6 ms** |
| Mémoire après ouverture | **+0,9 Mo** (PSS) | **+1,0 Mo** (phys_footprint) | **+1,1 Mo** |
| Mémoire pire cas (toutes les connexions ouvertes, caches pleins) | +8,1 Mo | +9,0 Mo | +8,9 Mo |
| Écriture sur 1 table | ≈ (pas plus lent) | ≈ | ≈ |
| Lectures concurrentes | ≈ / plus rapide | ≈ / plus rapide | ≈ |
| Action qui écrit **dans les 2 bases** | +0,8 ms par action | +0,07 ms par action | +0,07 ms par action |

**Gros volume** (vraie volumétrie de calendrier, ~120 Mo : 20 000 contacts, 205 000 événements, ~513 000 participants). Comparaison L2 (contacts + calendrier) vs L1 (une base), médianes sur 20 runs. Les checksums sont identiques : les deux renvoient exactement les mêmes données.

| | Android | iOS app | iOS exécutable |
|---|---:|---:|---:|
| Ouverture | +7 ms | ≈ (−2 ms) | +3 ms |
| Vues semaine / mois, recherche de contacts | ≈ | ≈ | ≈ |
| **Agenda 12 mois + nom du contact de chaque participant** (JOIN SQL vs jointure en mémoire) | **+118 ms (+36 %)**, soit ~10 ms par mois affiché | **+57 ms (+19 %)**, ~5 ms par mois | **+83 ms (+31 %)**, ~7 ms par mois |
| Synchros contacts / calendrier seules | ≈ | ≈ | ≈ |
| **Les deux synchros en parallèle** | **−17 ms (−10 %)** | **−29 ms (−10 %)** | **−22 ms (−8 %)** |
| Mémoire pire cas | +5,8 Mo | +7,8 Mo | +7,5 Mo |

Conclusions factuelles :

1. **Le surcoût fixe d'une base de plus est de quelques ms à l'ouverture et d'environ 1 Mo de mémoire.** C'est négligeable pour 2 bases, et ça reste modéré pour 5 bases (+9 à 33 ms, +5 Mo).
2. **Le surcoût mémoire en pire cas ne vient pas du nombre de bases, mais du nombre de connexions.** Room ouvre jusqu'à 1 writer + 4 readers **par base**, et SQLite garde un cache de pages (~2 Mo max) **par connexion**. Le scénario de contrôle `S5c` (5 bases avec 5 connexions au total, comme une base unique) coûte seulement **+1,1 Mo** de plus qu'une base unique sur Android, contre +26,6 Mo pour 5 bases avec le pool par défaut. Ce coût se règle avec `setMultipleConnectionPool(...)` / `setSingleConnectionPool()`.
3. **Le seul cas vraiment plus lent :** une action qui écrit dans plusieurs bases. Elle fait une transaction par base, donc plusieurs commits. Elle n'est pas non plus atomique entre les bases.
4. **Écrire ou lire dans une seule base n'est pas plus lent**, et c'est souvent un peu plus rapide (base plus petite, verrou d'écriture non partagé).

Au-delà de 2 bases (5 ou 10), les coûts augmentent à peu près linéairement, mais ils restent modérés (voir les tableaux complets).

**Gros volume :** les volumes réalistes ne changent pas ces conclusions.

- **Le vrai coût de 2 bases est fonctionnel :** on perd le `JOIN` SQL entre contacts et événements. L'agenda doit faire une 2e requête et une jointure en mémoire, ce qui coûte +19 à +36 % sur cette requête, soit 5 à 10 ms par mois affiché.
- **En contrepartie,** des synchros contacts et calendrier simultanées ne se bloquent plus mutuellement (−8 à −10 %).
- **Le reste est identique**, que ce soit dans une vraie app iOS ou en exécutable.

## Méthodologie

**Principe :** même schéma, mêmes données et même charge de travail. Seul le découpage en fichiers change.

- **Schéma :** 10 tables identiques (`t0`…`t9` : id, name, value, payload ~210 octets).
- **Stack :** Kotlin 2.4.10, Room **3.0.2** (`androidx.room3`), `BundledSQLiteDriver` (sqlite-bundled 2.6.2), `setQueryCoroutineContext(Dispatchers.IO)`. Configuration Room par défaut : WAL, pool de **1 writer + 4 readers par base** (`WAL_DEFAULT_NUMBER_OF_READERS = 4`, relevé dans le bytecode de room3-runtime 3.0.2).

| Scénario | Découpage |
|---|---|
| **S1** | 1 base, 10 tables |
| **S2** | 2 bases : « contacts » (2 tables) + « calendrier » (8 tables) |
| **S5** | 5 bases de 2 tables |
| **S10** | 10 bases d'une table |
| **S5c** (contrôle) | S5 avec `setSingleConnectionPool()` → 5 connexions au total, comme S1 |

**Protocole :**

- **Préparation :** chaque scénario est rempli une fois avec 2 000 lignes par table (~5 Mo de données). Les mesures portent ensuite sur l'**ouverture de fichiers existants**, comme au démarrage réel d'une app.
- **Un process neuf par mesure :**
  - **Android :** APK **release** non debuggable, lancé avec `am start -S`.
  - **iOS (app) :** vraie app SwiftUI ([`iosApp/`](iosApp/)) qui lie le framework Kotlin `Shared`, relancée à chaque mesure avec `xcrun simctl launch --terminate-running-process`. Les données sont dans le sandbox de l'app (`Application Support`).
  - **iOS (exécutable) :** le même code en exécutable Kotlin/Native lancé avec `xcrun simctl spawn`. C'est la première version du protocole, conservée pour comparaison.
- **Répétitions :** 20 runs par scénario, avec les scénarios **alternés** (S1, S2, S5, S10, S5c, S1…) pour répartir le bruit. Un warm-up non compté au début.
- **Statistiques :** médiane et écart interquartile (Q1–Q3).

**Mesures dans chaque run** (`shared/src/commonMain/kotlin/bench/Bench.kt`) :

| Métrique | Contenu |
|---|---|
| `openMs` | `build()` de toutes les bases + une première requête par base (ouverture, vérification du schéma) |
| `observeMs` | Un `Flow` actif sur chacune des 10 tables (mise en place de l'`InvalidationTracker`), jusqu'à la première émission |
| `writeAllMs` | 200 « actions » qui écrivent 1 ligne dans les 10 tables, avec 1 transaction par base. C'est le **pire cas** pour plusieurs bases |
| `writeOneMs` | 200 transactions sur la seule table `t0`. Le travail est identique dans tous les scénarios |
| `readMs` | 8 coroutines concurrentes lisent les 200 dernières lignes de chaque table (×10), puis font un scan complet. Toutes les connexions s'ouvrent et les caches se remplissent |
| `*OpenKb` / `*EndKb` | Augmentation de la mémoire par rapport au début du process, après un GC, mesurée **après l'ouverture** puis **en fin de run** |

**Suite « gros volume »** (`shared/src/commonMain/kotlin/bench/large/`) : une vraie volumétrie de calendrier.

- **Données** (graines fixes, identiques dans les deux scénarios) :
  - 20 000 contacts ;
  - 10 calendriers avec 200 000 événements sur 5 ans, plus un calendrier « synchro » de 5 000 événements ;
  - environ 513 000 participants, dont 80 % sont des contacts ;
  - au total, environ 120 Mo.
- **Index :** `events(calendarId, startMs)`, `attendees(eventId)`, `attendees(email)`, `contacts(email)`, `contacts(name)`.
- **Scénarios :** **L1** = 1 base. **L2** = 2 bases (contacts / calendrier).

| Métrique | Contenu |
|---|---|
| `openMs` | Ouverture des bases existantes + premières requêtes |
| `weekQueryMs` | 52 vues « semaine » sur 3 calendriers (événements + participants) |
| `monthQueryMs` | 12 vues « mois » sur les 10 calendriers |
| `agendaJoinMs` | 12 mois d'agenda avec le nom du contact de chaque participant. **L1 :** un `JOIN` SQL. **L2 :** une requête calendrier, puis `WHERE email IN (…)` par paquets de 900 dans la base contacts, puis une jointure en mémoire |
| `searchContactsMs` | 100 recherches de contacts par préfixe |
| `syncContactsMs` / `syncCalendarMs` | Upsert de 2 000 contacts / remplacement des 5 000 événements du calendrier « synchro » (1 transaction chacun, rejoués à l'identique pour que la base ne grossisse pas) |
| `parallelSyncMs` | Les deux synchros en même temps. **L1 :** un seul writer partagé. **L2 :** un writer par base |
| `checksum` | Empreinte de tous les résultats lus. Elle doit être **identique** pour L1 et L2 |

**Mémoire relevée :**
- **Android :** `Debug.MemoryInfo.totalPss`, `Debug.getNativeHeapAllocatedSize()` (où vit SQLite) et le heap Java.
- **iOS :** `task_vm_info.phys_footprint`, la métrique affichée par Xcode et utilisée par le système (jetsam) pour décider de tuer une app.

**Environnement :**
- **Machine :** MacBook Apple M3 Max, 36 Go, macOS 26.7.
- **Android :** émulateur `sdk_gphone16k_arm64`, Android 16 (API 36), 4 cœurs, 16 Go.
- **iOS :** simulateur iPhone 17, iOS 26.5.

## Résultats

**Rapport visuel : [baillyjamy.github.io/room-multi-db-bench](https://baillyjamy.github.io/room-multi-db-bench/)** (`index.html`) (régénérer : `python3 scripts/report.py`).

Tableaux complets (médiane et Q1–Q3, écart par rapport à S1) : **[RESULTS.md](RESULTS.md)**. Données brutes, une ligne JSON par run : [`results/`](results/).

## Limites

- **Pas de vrais appareils :** les mesures tournent sur un émulateur et un simulateur sur un Mac M3 Max. Les valeurs absolues sont donc optimistes, surtout pour les I/O. La **comparaison entre scénarios** reste valable, car tous tournent sur la même machine, avec le même build et en alternance.
- **Gros volume synthétique :** la volumétrie est réaliste, mais la distribution des données peut différer de celle d'une vraie app.
- **Pire cas mémoire :** `*EndKb` force l'ouverture de toutes les connexions et le remplissage de tous les caches. Dans une vraie app, les readers ne s'ouvrent qu'en cas d'accès concurrents.
- **Partage entre apps non testé :** l'accès multi-process (App Group iOS) n'est pas mesuré.

## Reproduire

Prérequis : JDK 17+, Android SDK, Xcode, un émulateur ou appareil Android (`adb`) et un simulateur iOS démarré.

```bash
python3 scripts/generate.py   # génère les entités, DAO et bases (déjà fait)
./gradlew :androidApp:assembleRelease :shared:linkReleaseExecutableIosSimulatorArm64
# App iOS (le projet est généré par Tuist depuis iosApp/Project.swift ; la phase de build lance Gradle pour le framework)
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Release \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath build/ios-app build

# Suite de base (S1…S10, S5c)
RUNS=20 ./scripts/run_ios_app.sh
RUNS=20 ./scripts/run_ios.sh
RUNS=20 ./scripts/run_android.sh   # ANDROID_SERIAL=... pour choisir l'appareil
# Suite gros volume (L1, L2) -> results/*-large.jsonl
export PREFIX=large- SCENARIOS="L1 L2"
RUNS=20 ./scripts/run_ios_app.sh && RUNS=20 ./scripts/run_ios.sh && RUNS=20 POLLS=600 ./scripts/run_android.sh

python3 scripts/analyze.py results/{android,ios-app,ios}.jsonl results/{android,ios-app,ios}-large.jsonl > RESULTS.md
python3 scripts/report.py
```

## Sources

- **[Room – Save data in a local database](https://developer.android.com/training/data-storage/room) :** « *Each RoomDatabase instance is fairly expensive* ». Cette recommandation porte sur les **instances multiples d'une même base** (singleton), pas sur le nombre de bases.
- **[SQLite – PRAGMA cache_size](https://www.sqlite.org/pragma.html#pragma_cache_size) :** le cache par défaut est de `-2000`, soit ~2 Mo **par connexion**. C'est un plafond, pas une allocation fixe.
- **[SQLite – Write-Ahead Logging](https://www.sqlite.org/wal.html) :** un seul writer à la fois **par base**, et les lecteurs ne bloquent pas l'écriture.
- **[Android – SQLite performance best practices](https://developer.android.com/topic/performance/sqlite-performance-best-practices) :** WAL, et chaque commit coûte un `fsync`.
- **[RoomDatabase.Builder.setMultipleConnectionPool](https://developer.android.com/reference/kotlin/androidx/room/RoomDatabase.Builder#setMultipleConnectionPool(kotlin.Int,kotlin.Int)) :** taille du pool de connexions configurable.
