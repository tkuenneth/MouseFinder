package dev.tkuenneth.mousefinder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
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
                    position = (MouseInfo.getPointerInfo().location?.let {
                        with(density) {
                            DpOffset(it.x.toDp() - size / 2, it.y.toDp() - size / 2)
                        }
                    } ?: DpOffset.Zero)
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

@Composable
fun MouseSpot(visible: Boolean, position: DpOffset, size: Dp, onCloseRequest: () -> Unit) {
    if (visible) {
        Window(
            onCloseRequest = onCloseRequest,
            state = rememberWindowState(
                size = DpSize(width = size, height = size),
                position = WindowPosition(x = position.x, y = position.y),
            ),
            alwaysOnTop = true,
            undecorated = true,
            transparent = true
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = CircleShape
                    )
                    .pointerInput(PointerEventPass.Main) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Exit, PointerEventType.Press ->
                                        onCloseRequest()
                                }
                            }
                        }
                    }
            )
        }
    }
}
