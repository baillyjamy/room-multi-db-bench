package bench.sqldelight

import android.annotation.SuppressLint
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/** Set by the app before running the benchmark (AndroidSqliteDriver needs a Context). */
@SuppressLint("StaticFieldLeak")
lateinit var appContext: Context

actual fun createDriver(schema: SqlSchema<QueryResult.Value<Unit>>, path: String): SqlDriver =
    AndroidSqliteDriver(schema, appContext, path, callback = object : AndroidSqliteDriver.Callback(schema) {
        override fun onConfigure(db: SupportSQLiteDatabase) {
            db.enableWriteAheadLogging()
            db.query("PRAGMA synchronous = NORMAL").close()
        }
    })
