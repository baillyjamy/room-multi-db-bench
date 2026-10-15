package bench

import java.io.File

/** Usage: <seed|run|large-seed|large-run> <scenario>. Launched in a fresh JVM for every measurement. */
fun main(args: Array<String>) {
    val (mode, scenario) = args
    val dir = File(System.getProperty("java.io.tmpdir"), "roombench").absolutePath
    makeDir(dir)
    val result = kotlinx.coroutines.runBlocking { bench.large.dispatch(mode, scenario, dir) }
    println("ROOMBENCH $result")
}
