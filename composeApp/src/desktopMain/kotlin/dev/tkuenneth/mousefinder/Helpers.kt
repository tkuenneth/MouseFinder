package dev.tkuenneth.mousefinder

val osName = System.getProperty("os.name") ?: ""

val platformInfo: String = StringBuilder().also {
    val osName = osName
    val osVersion = System.getProperty("os.version") ?: ""
    val javaVendorVersion = System.getProperty("java.vendor.version") ?: ""
    val javaVendor = System.getProperty("java.vendor") ?: ""
    val osArch = System.getProperty("os.arch") ?: ""
    it.append(" $osName $osVersion")
    it.appendLine()
    it.append("$javaVendor $javaVendorVersion ($osArch)")
}.toString()

enum class OperatingSystem {
    Linux, Windows, MacOS, Unknown;
}

val operatingSystem = osName.lowercase().let { name ->
    if (name.contains("mac os x")) {
        OperatingSystem.MacOS
    } else if (name.contains("windows")) {
        OperatingSystem.Windows
    } else if (name.contains("linux")) {
        OperatingSystem.Linux
    } else {
        OperatingSystem.Unknown
    }
}
