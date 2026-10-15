package bench.app

import android.app.Activity
import android.os.Bundle
import android.util.Log
import bench.large.dispatch
import kotlinx.coroutines.runBlocking
import java.io.File

/** Launched with `am start -S` (fresh process) and extras `mode` (seed|run|large-seed|large-run|light-seed|light-run) and `scenario`. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode = intent.getStringExtra("mode") ?: "run"
        val scenario = intent.getStringExtra("scenario") ?: "S1"
        val dir = File(noBackupFilesDir, "roombench").absolutePath
        bench.sqldelight.appContext = applicationContext
        Thread {
            val result = runBlocking { dispatch(mode, scenario, dir) }
            Log.i("ROOMBENCH", result)
            runOnUiThread { finish() }
        }.start()
    }
}
