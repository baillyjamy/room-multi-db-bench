package bench

import platform.UIKit.UIDevice

actual val PLATFORM: String = "ios-${UIDevice.currentDevice.systemVersion}"
