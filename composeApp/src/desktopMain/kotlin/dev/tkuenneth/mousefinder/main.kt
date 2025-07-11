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
import dev.tkuenneth.mousefinder.MacHelp.activateApp
import java.awt.GraphicsEnvironment
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Rectangle
import java.awt.Robot

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
    val screens = remember { getScreens() }
    val robot = remember { Robot() }
    val updatePosition: (Point) -> Unit = { location ->
        val graphicsConfig = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .defaultScreenDevice.defaultConfiguration
        val transform = graphicsConfig.defaultTransform
        val scaleX = transform.scaleX.toFloat()
        val scaleY = transform.scaleY.toFloat()
        position = with(density) {
            DpOffset(
                (location.x * scaleX).toDp() - size / 2,
                (location.y * scaleY).toDp() - size / 2
            )
        }
    }

    DisposableEffect(Unit) {
        keyListener = GlobalKeyListener(
            initialShortcut = shortcut,
            onShowWindow = {
                if (allowShortcuts) {
                    MouseInfo.getPointerInfo().location?.let { currentLocation ->
                        if (mouseSpotVisible) {
                            val nextLocation = findNextScreen(screens, currentLocation)
                            setMouseLocation(nextLocation, robot)
                            mouseSpotVisible = false
                        } else {
                            updatePosition(currentLocation)
                            mouseSpotVisible = true
                        }
                    }
                }
            }).also { it.register() }
        onDispose {
            keyListener.unregister()
        }
    }
    MouseFinderTray(
        settingsClicked = {
            settingsVisible = true
            activateMe()
        },
        aboutClicked = {
            aboutWindowVisible = true
            activateMe()
        }
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

private fun activateMe() {
    if (operatingSystem == OperatingSystem.MacOS) {
        activateApp("Mouse Finder")
    }
}

private fun getScreens(): List<Rectangle> {
    with(GraphicsEnvironment.getLocalGraphicsEnvironment()) {
        return screenDevices.map { device ->
            device.defaultConfiguration.bounds
        }
    }
}

private fun findNextScreen(screens: List<Rectangle>, currentPosition: Point): Point {
    val currentScreenIndex = screens.indexOfFirst { screen ->
        screen.contains(currentPosition)
    }
    return if (currentScreenIndex == -1) {
        currentPosition
    } else {
        with(screens[(currentScreenIndex + 1) % screens.size]) {
            Point(x + width / 2, y + height / 2)
        }
    }
}

private fun setMouseLocation(position: Point, robot: Robot) {
    robot.mouseMove(position.x, position.y)
}
