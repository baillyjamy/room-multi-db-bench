package bench

import java.io.File

actual val PLATFORM: String = "jvm-${System.getProperty("java.specification.version")}-${System.getProperty("os.name").lowercase().replace(" ", "")}"

/** rss = resident memory of the whole JVM process (bundled SQLite allocates natively, outside the Java heap). */
actual fun memory(): Map<String, Long> {
    val rt = Runtime.getRuntime()
    val rssKb = ProcessBuilder("ps", "-o", "rss=", "-p", ProcessHandle.current().pid().toString())
        .start().inputStream.bufferedReader().readText().trim().toLong()
    return mapOf("rss" to rssKb * 1024, "javaHeap" to rt.totalMemory() - rt.freeMemory())
}

actual fun fullGc() {
    repeat(2) { System.gc() }
    Thread.sleep(100)
}

actual fun deleteDir(path: String) {
    File(path).deleteRecursively()
}

actual fun makeDir(path: String) {
    File(path).mkdirs()
}
