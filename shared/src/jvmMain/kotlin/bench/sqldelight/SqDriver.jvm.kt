package bench.sqldelight

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.Properties

actual fun createDriver(schema: SqlSchema<QueryResult.Value<Unit>>, path: String): SqlDriver =
    JdbcSqliteDriver("jdbc:sqlite:$path", Properties().apply {
        put("journal_mode", "WAL")
        put("synchronous", "NORMAL")
    }, schema)
