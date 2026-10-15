package bench

import platform.Foundation.NSTemporaryDirectory

/** Usage: <seed|run|large-seed|large-run> <scenario>. Launched as a fresh native macOS process for every measurement. */
fun main(args: Array<String>) {
    val (mode, scenario) = args
    val dir = NSTemporaryDirectory() + "roombench"
    makeDir(dir)
    val result = kotlinx.coroutines.runBlocking { bench.large.dispatch(mode, scenario, dir) }
    println("ROOMBENCH $result")
}
