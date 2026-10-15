package bench

import android.os.Build
import android.os.Debug
import java.io.File

actual val PLATFORM: String = "android-${Build.VERSION.SDK_INT}"

actual fun memory(): Map<String, Long> {
    val info = Debug.MemoryInfo().also(Debug::getMemoryInfo)
    val rt = Runtime.getRuntime()
    return mapOf(
        "pss" to info.totalPss * 1024L,
        "nativeHeap" to Debug.getNativeHeapAllocatedSize(),
        "javaHeap" to rt.totalMemory() - rt.freeMemory(),
    )
}

actual fun fullGc() {
    repeat(2) {
        Runtime.getRuntime().gc()
        System.runFinalization()
    }
    Thread.sleep(100)
}

actual fun deleteDir(path: String) {
    File(path).deleteRecursively()
}

actual fun makeDir(path: String) {
    File(path).mkdirs()
}
