package bench

import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.value
import platform.Foundation.NSFileManager
import platform.UIKit.UIDevice
import platform.darwin.TASK_VM_INFO
import platform.darwin.integer_tVar
import platform.darwin.mach_msg_type_number_tVar
import platform.darwin.mach_task_self_
import platform.darwin.task_info
import platform.darwin.task_vm_info_data_t
import kotlin.native.runtime.GC

actual val PLATFORM: String = "ios-${UIDevice.currentDevice.systemVersion}"

/** phys_footprint is the metric Xcode and jetsam use for an app's memory. */
actual fun memory(): Map<String, Long> = memScoped {
    val info = alloc<task_vm_info_data_t>()
    val count = alloc<mach_msg_type_number_tVar>()
    count.value = (sizeOf<task_vm_info_data_t>() / sizeOf<integer_tVar>()).convert()
    task_info(mach_task_self_, TASK_VM_INFO.convert(), info.ptr.reinterpret(), count.ptr)
    mapOf("footprint" to info.phys_footprint.toLong())
}

actual fun fullGc() {
    GC.collect()
    platform.posix.usleep(100_000u)
}

actual fun deleteDir(path: String) {
    NSFileManager.defaultManager.removeItemAtPath(path, null)
}

actual fun makeDir(path: String) {
    NSFileManager.defaultManager.createDirectoryAtPath(path, true, null, null)
}
