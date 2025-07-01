import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.FileInputStream
import java.io.InputStreamReader

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

val content = with(rootProject.file("composeApp/src/desktopMain/kotlin/dev/tkuenneth/mousefinder/Version.kt")) {
    if (isFile) {
        InputStreamReader(FileInputStream(this), Charsets.UTF_8).use { reader ->
            reader.readText()
        }
    } else {
        error("$absolutePath not found")
    }
}
val humanReadableVersionString = with(content) {
    val regex = """const val VERSION = "([^"]+)"""".toRegex()
    regex.find(this)?.groupValues?.get(1)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.jnativeHook)
        }
    }
}


val macExtraPlistKeys: String
    get() = """
        <key>LSUIElement</key>
        <string>1</string>
    """.trim()

compose.desktop {
    application {
        mainClass = "dev.tkuenneth.mousefinder.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Mouse Finder"
            packageVersion = humanReadableVersionString.toString()
            description = "Highlights the location of the mouse pointer"
            copyright = "2025 Thomas Kuenneth. All rights reserved."
            vendor = "Thomas Kuenneth"
            macOS {
                bundleID = "dev.tkuenneth.mousefinder"
                signing {
                    sign.set(true)
                    identity.set("Thomas Kuenneth")
                }
                notarization {
                    appleID.set("thomas.kuenneth@icloud.com")
                    password.set("@keychain:NOTARIZATION_PASSWORD")
                }
                infoPlist {
                    extraKeysRawXml = macExtraPlistKeys
                }
                iconFile.set(rootProject.file("artwork/MouseFinder.icns"))
            }
            windows {
                iconFile.set(rootProject.file("artwork/MouseFinder.ico"))
                menuGroup = "Thomas Kuenneth"
            }
            modules("java.instrument", "java.prefs", "jdk.unsupported")
        }
        buildTypes.release.proguard {
            configurationFiles.from("rules.pro")
        }
    }
}
