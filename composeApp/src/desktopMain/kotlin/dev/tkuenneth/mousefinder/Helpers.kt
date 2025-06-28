package dev.tkuenneth.mousefinder

val platformName: String = StringBuilder().also {
    val osName = System.getProperty("os.name") ?: ""
    val osVersion = System.getProperty("os.version") ?: ""
    val javaVendorVersion = System.getProperty("java.vendor.version") ?: ""
    val javaVendor = System.getProperty("java.vendor") ?: ""
    val osArch = System.getProperty("os.arch") ?: ""
    it.append(" $osName $osVersion")
    it.appendLine()
    it.append("$javaVendor $javaVendorVersion ($osArch)")
}.toString()
