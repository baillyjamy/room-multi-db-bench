package bench

expect val PLATFORM: String

/** Memory metrics in bytes, keyed by metric name. */
expect fun memory(): Map<String, Long>

expect fun fullGc()

expect fun deleteDir(path: String)

expect fun makeDir(path: String)
