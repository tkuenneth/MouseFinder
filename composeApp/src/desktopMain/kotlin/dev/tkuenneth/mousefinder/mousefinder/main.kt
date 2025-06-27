package dev.tkuenneth.mousefinder.mousefinder

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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import mousefinder.composeapp.generated.resources.Res
import mousefinder.composeapp.generated.resources.app_icon
import mousefinder.composeapp.generated.resources.app_title
import mousefinder.composeapp.generated.resources.exit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.util.logging.Level
import java.util.logging.Logger

fun main() = application {
    var isVisible by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        var keyListener: GlobalKeyListener? = null
        try {
            val logger = Logger.getLogger(GlobalScreen::class.java.getPackage().name)
            logger.level = Level.OFF
            logger.useParentHandlers = false
            GlobalScreen.registerNativeHook()
            keyListener = GlobalKeyListener(
                onShowWindow = { isVisible = true })
            GlobalScreen.addNativeKeyListener(keyListener)
        } catch (ex: NativeHookException) {
            println("Failed to register global hotkeys: ${ex.message}")
            println("You may need to grant accessibility permissions to this application.")
        }
        onDispose {
            try {
                keyListener?.let { GlobalScreen.removeNativeKeyListener(it) }
                GlobalScreen.unregisterNativeHook()
            } catch (ex: NativeHookException) {
                println("Error during cleanup: ${ex.message}")
            }
        }
    }
    if (isVisible) {
        App { isVisible = false }
    }
    Tray(
        icon = painterResource(Res.drawable.app_icon),
        tooltip = stringResource(Res.string.app_title),
        onAction = { isVisible = true },
        menu = {
            Item(
                stringResource(Res.string.exit), onClick = ::exitApplication
            )
        })
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App(size: Dp = 200.dp, onCloseRequest: () -> Unit) {
    val density = LocalDensity.current
    val mouseLocation = remember {
        try {
            val pointerInfo = java.awt.MouseInfo.getPointerInfo()
            val location = pointerInfo.location
            with(density) { DpOffset(location.x.toDp(), location.y.toDp()) }
        } catch (_: Exception) {
            DpOffset.Zero
        }
    }
    Window(
        onCloseRequest = onCloseRequest, state = rememberWindowState(
            position = WindowPosition(
                x = mouseLocation.x - size / 2, y = mouseLocation.y - size / 2
            ), size = DpSize(width = size, height = size)
        ), alwaysOnTop = true, undecorated = true, transparent = true
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                color = MaterialTheme.colorScheme.background, shape = CircleShape
            ).onPointerEvent(PointerEventType.Exit) {
                onCloseRequest()
            })
    }
}

class GlobalKeyListener(
    private val onShowWindow: () -> Unit
) : NativeKeyListener {
    private var ctrlPressed = false
    private var altPressed = false

    override fun nativeKeyPressed(e: NativeKeyEvent) {
        when (e.keyCode) {
            NativeKeyEvent.VC_CONTROL -> ctrlPressed = true
            NativeKeyEvent.VC_ALT -> altPressed = true
            NativeKeyEvent.VC_M -> {
                if (ctrlPressed && altPressed) {
                    onShowWindow()
                }
            }
        }
    }

    override fun nativeKeyReleased(e: NativeKeyEvent) {
        when (e.keyCode) {
            NativeKeyEvent.VC_CONTROL -> ctrlPressed = false
            NativeKeyEvent.VC_ALT -> altPressed = false
        }
    }
}
