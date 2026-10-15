package bench

import kotlinx.coroutines.runBlocking

/** Entry point for the SwiftUI app (iosApp): same benchmark code, but running inside a real iOS app process. */
object AppBench {
    fun run(mode: String, scenario: String, dir: String): String {
        makeDir(dir)
        val json = runBlocking { bench.large.dispatch(mode, scenario, dir) }
        return json.dropLast(1) + ""","host":"app"}"""
    }
}
