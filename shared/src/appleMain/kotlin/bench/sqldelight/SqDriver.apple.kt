package bench.sqldelight

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.JournalMode
import co.touchlab.sqliter.SynchronousFlag

actual fun createDriver(schema: SqlSchema<QueryResult.Value<Unit>>, path: String): SqlDriver =
    NativeSqliteDriver(schema, path.substringAfterLast('/'), onConfiguration = { config ->
        config.copy(
            journalMode = JournalMode.WAL,
            extendedConfig = config.extendedConfig.copy(basePath = path.substringBeforeLast('/'), synchronousFlag = SynchronousFlag.NORMAL),
        )
    })
