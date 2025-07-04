package dev.tkuenneth.mousefinder

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import java.awt.GraphicsEnvironment
import java.awt.MouseInfo

fun main() = application {
    if (operatingSystem == OperatingSystem.Windows) {
        System.setProperty("skiko.renderApi", "OPENGL")
        System.setProperty("jnativehook.lib.path", System.getProperty("java.io.tmpdir"))
    }
    val size = 200.dp
    var mouseSpotVisible by remember { mutableStateOf(false) }
    var aboutWindowVisible by remember { mutableStateOf(false) }
    var settingsVisible by remember { mutableStateOf(false) }
    var position by remember { mutableStateOf(DpOffset.Zero) }
    var shortcut by remember { mutableStateOf(getMouseFinderShortcut()) }
    var allowShortcuts by remember { mutableStateOf(true) }
    var keyListener: GlobalKeyListener? by remember { mutableStateOf(null) }
    val density = LocalDensity.current

    DisposableEffect(Unit) {
        keyListener = GlobalKeyListener(
            initialShortcut = shortcut,
            onShowWindow = {
                if (allowShortcuts) {
                    position = MouseInfo.getPointerInfo().location?.let {
                        val graphicsConfig = GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .defaultScreenDevice.defaultConfiguration
                        val transform = graphicsConfig.defaultTransform
                        val scaleX = transform.scaleX.toFloat()
                        val scaleY = transform.scaleY.toFloat()
                        with(density) {
                            DpOffset(
                                (it.x / scaleX).toDp() - size / 2,
                                (it.y / scaleY).toDp() - size / 2
                            )
                        }
                    } ?: DpOffset.Zero
                    mouseSpotVisible = true
                }
            }).also { it.register() }
        onDispose {
            keyListener.unregister()
        }
    }
    MouseFinderTray(
        settingsClicked = { settingsVisible = true },
        aboutClicked = { aboutWindowVisible = true }
    )
    MouseSpot(
        visible = mouseSpotVisible,
        position = position,
        size = size
    ) { mouseSpotVisible = false }
    AboutWindow(visible = aboutWindowVisible) { aboutWindowVisible = false }
    SettingsWindow(
        visible = settingsVisible,
        shortcut = shortcut,
        allowShortcuts = { allowShortcuts = it },
        onShortcutChanged = {
            shortcut = it
            putMouseFinderShortcut(it)
            keyListener?.updateShortcut(it)
        },
        onCloseRequest = {
            settingsVisible = false
            allowShortcuts = true
        }
    )
}