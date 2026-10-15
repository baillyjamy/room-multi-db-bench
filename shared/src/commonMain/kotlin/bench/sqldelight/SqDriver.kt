package bench.sqldelight

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import bench.DbHandle
import bench.TableOps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

/**
 * Official SQLDelight driver of the platform, configured like Room's default: WAL + synchronous=NORMAL.
 * Android: AndroidSqliteDriver (system SQLite). Apple: NativeSqliteDriver (SQLiter). JVM: JdbcSqliteDriver (sqlite-jdbc).
 */
expect fun createDriver(schema: SqlSchema<QueryResult.Value<Unit>>, path: String): SqlDriver

/** SQLDelight transactions take a non-suspending block: run the (non-suspending) bench block synchronously inside it. */
fun runSync(block: suspend () -> Unit) {
    var result: Result<Unit>? = null
    block.startCoroutine(Continuation(EmptyCoroutineContext) { result = it })
    (result ?: error("block suspended inside a SQLDelight transaction")).getOrThrow()
}

suspend fun <T : Transacter> T.writeTx(block: () -> Unit) = withContext(Dispatchers.IO) { transaction { block() } }

fun SqlDriver.sqliteVersion(): String =
    executeQuery(null, "SELECT sqlite_version()", { c -> c.next(); QueryResult.Value(c.getString(0)!!) }, 0).value

fun SqlDriver.sizeBytes(): Long {
    fun pragma(name: String) = executeQuery(null, "PRAGMA $name", { c -> c.next(); QueryResult.Value(c.getLong(0)!!) }, 0).value
    return pragma("page_count") * pragma("page_size")
}

fun sqHandle(
    name: String,
    path: String,
    schema: SqlSchema<QueryResult.Value<Unit>>,
    make: (SqlDriver) -> Pair<Transacter, List<TableOps>>,
): DbHandle {
    val driver = createDriver(schema, path)
    val (db, tables) = make(driver)
    return DbHandle(
        name, path, tables,
        writeTx = { block -> db.writeTx { runSync(block) } },
        close = driver::close,
        sqliteVersion = { withContext(Dispatchers.IO) { driver.sqliteVersion() } },
    )
}
