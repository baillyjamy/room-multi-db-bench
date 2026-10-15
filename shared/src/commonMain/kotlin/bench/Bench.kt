package bench

import androidx.room3.RoomDatabase
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

/** Same configuration as a typical Room KMP setup: bundled driver, WAL, default connection pool. */
fun <T : RoomDatabase> RoomDatabase.Builder<T>.configure(): RoomDatabase.Builder<T> =
    setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO)

class TableOps(
    val insert: suspend (Int) -> Unit,
    val count: suspend () -> Int,
    val latest: suspend () -> Int,
    val scan: suspend () -> Long,
    val observe: () -> Flow<Int>,
)

class DbHandle(val name: String, val path: String, val db: RoomDatabase, val tables: List<TableOps>)

private val PAYLOAD = "x".repeat(200)
fun payloadFor(i: Int) = "$PAYLOAD-$i"

const val SEED_ROWS = 2_000
const val ACTIONS = 200
const val READERS = 8
const val READ_ITERATIONS = 10

private inline fun timedMs(block: () -> Unit): Double {
    val mark = TimeSource.Monotonic.markNow()
    block()
    return mark.elapsedNow().inWholeMicroseconds / 1000.0
}

/** Fills the databases once, so that "run" measures opening existing files (the real app startup case). */
suspend fun seed(scenario: String, baseDir: String): String {
    val dir = "$baseDir/$scenario"
    deleteDir(dir)
    makeDir(dir)
    val handles = openScenario(scenario, dir)
    val ms = timedMs {
        handles.forEach { h ->
            h.db.withWriteTransaction { repeat(SEED_ROWS) { i -> h.tables.forEach { it.insert(i) } } }
        }
    }
    handles.forEach { it.db.close() }
    return """{"mode":"seed","platform":"$PLATFORM","scenario":"$scenario","seedMs":$ms}"""
}

suspend fun run(scenario: String, baseDir: String): String = coroutineScope {
    val dir = "$baseDir/$scenario"
    fullGc()
    val m0 = memory()

    // 1. Open: build every database and run a first query (forces open + schema validation).
    lateinit var handles: List<DbHandle>
    val openMs = timedMs {
        handles = openScenario(scenario, dir)
        handles.forEach { it.tables.first().count() }
    }
    fullGc()
    val mOpen = memory()

    // 2. Observe every table with a Flow, like a UI would (InvalidationTracker setup per database).
    val observers = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val observeMs = timedMs {
        handles.flatMap { it.tables }.map { t ->
            CompletableDeferred<Unit>().also { ready -> observers.launch { t.observe().collect { ready.complete(Unit) } } }
        }.awaitAll()
    }

    // 3. A "logical action" writing one row in each of the 10 tables -> 1 transaction per database.
    val writeAllMs = timedMs {
        repeat(ACTIONS) { i ->
            handles.forEach { h -> h.db.withWriteTransaction { h.tables.forEach { it.insert(SEED_ROWS + i) } } }
        }
    }

    // 4. Writes touching a single table (table t0 only), identical work in every scenario.
    val t0 = handles.first()
    val writeOneMs = timedMs {
        repeat(ACTIONS) { i -> t0.db.withWriteTransaction { t0.tables.first().insert(i) } }
    }

    // 5. Concurrent reads on every table (opens all reader connections of every pool) + full scans.
    val readMs = timedMs {
        (1..READERS).map {
            async(Dispatchers.Default) {
                repeat(READ_ITERATIONS) { handles.forEach { h -> h.tables.forEach { it.latest() } } }
                handles.forEach { h -> h.tables.forEach { it.scan() } }
            }
        }.awaitAll()
    }

    delay(300)
    fullGc()
    val mEnd = memory()
    observers.cancel()
    handles.forEach { it.db.close() }

    buildString {
        append("""{"mode":"run","platform":"$PLATFORM","scenario":"$scenario","dbCount":${handles.size}""")
        append(""","openMs":$openMs,"observeMs":$observeMs,"writeAllMs":$writeAllMs,"writeOneMs":$writeOneMs,"readMs":$readMs""")
        m0.keys.forEach { k ->
            append(""","${k}OpenKb":${(mOpen.getValue(k) - m0.getValue(k)) / 1024}""")
            append(""","${k}EndKb":${(mEnd.getValue(k) - m0.getValue(k)) / 1024}""")
        }
        append("}")
    }
}
