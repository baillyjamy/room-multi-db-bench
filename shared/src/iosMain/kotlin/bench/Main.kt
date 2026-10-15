package bench

import kotlinx.coroutines.runBlocking
import platform.Foundation.NSTemporaryDirectory

/** Usage: <seed|run|large-seed|large-run> <scenario>. Launched in a fresh simulator process for every measurement. */
fun main(args: Array<String>) {
    val (mode, scenario) = args
    val dir = NSTemporaryDirectory() + "roombench"
    makeDir(dir)
    val result = runBlocking { bench.large.dispatch(mode, scenario, dir) }
    println("ROOMBENCH $result")
}
