package dev.tkuenneth.mousefinder.mousefinder

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "mousefinder",
    ) {
        App()
    }
}