package dev.tkuenneth.mousefinder.mousefinder

import androidx.compose.runtime.*
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
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

@Composable
fun App(onCloseRequest: () -> Unit) {
    Window(
        onCloseRequest = onCloseRequest, title = stringResource(Res.string.app_title), state = rememberWindowState()
    ) {}
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
