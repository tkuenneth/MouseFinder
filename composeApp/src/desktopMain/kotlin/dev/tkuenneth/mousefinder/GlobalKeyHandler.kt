package dev.tkuenneth.mousefinder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import java.awt.MouseInfo
import java.util.logging.Level
import java.util.logging.Logger

@Composable
fun GlobalKeyHandler(size: Dp, onShowWindow: (DpOffset) -> Unit) {
    val density = LocalDensity.current
    DisposableEffect(Unit) {
        var keyListener: GlobalKeyListener? = null
        try {
            val logger = Logger.getLogger(GlobalScreen::class.java.getPackage().name)
            logger.level = Level.OFF
            logger.useParentHandlers = false
            GlobalScreen.registerNativeHook()
            keyListener = GlobalKeyListener(
                onShowWindow = {
                    onShowWindow(MouseInfo.getPointerInfo().location?.let {
                        with(density) {
                            DpOffset(it.x.toDp() - size / 2, it.y.toDp() - size / 2)
                        }
                    } ?: DpOffset.Zero)
                }
            )
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
}

private class GlobalKeyListener(
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
