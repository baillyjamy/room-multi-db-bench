package bench

import kotlinx.coroutines.runBlocking
import platform.Foundation.NSTemporaryDirectory

/** Usage: <seed|run> <scenario>. Launched in a fresh simulator process for every measurement. */
fun main(args: Array<String>) {
    val (mode, scenario) = args
    val dir = NSTemporaryDirectory() + "roombench"
    makeDir(dir)
    val result = runBlocking { if (mode == "seed") seed(scenario, dir) else run(scenario, dir) }
    println("ROOMBENCH $result")
}
