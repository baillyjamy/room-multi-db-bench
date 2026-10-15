package bench

import kotlinx.cinterop.useContents
import platform.Foundation.NSProcessInfo

actual val PLATFORM: String = NSProcessInfo.processInfo.operatingSystemVersion.useContents { "macos-$majorVersion.$minorVersion" }
