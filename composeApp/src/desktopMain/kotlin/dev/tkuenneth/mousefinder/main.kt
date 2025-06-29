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
import mousefinder.composeapp.generated.resources.menu_abut
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.awt.MouseInfo
import java.util.logging.Level
import java.util.logging.Logger

fun main() = application {
    val size = 200.dp
    var mouseSpotVisible by remember { mutableStateOf(false) }
    var aboutWindowVisible by remember { mutableStateOf(false) }
    var position by remember { mutableStateOf(DpOffset.Zero) }
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
                    position = MouseInfo.getPointerInfo().location?.let {
                        with(density) {
                            DpOffset(it.x.toDp() - size / 2, it.y.toDp() - size / 2)
                        }
                    } ?: DpOffset.Zero
                    mouseSpotVisible = true
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
    val appTitle = stringResource(Res.string.app_title)
    val menuAbout = stringResource(
        Res.string.menu_abut, appTitle
    )
    Tray(
        icon = painterResource(Res.drawable.app_icon),
        tooltip = appTitle,
        onAction = { mouseSpotVisible = true },
        menu = {
            Item(
                text = menuAbout, onClick = { aboutWindowVisible = true })
            Item(
                stringResource(Res.string.exit), onClick = ::exitApplication
            )
        })
    if (mouseSpotVisible) {
        MouseSpot(position = position, size = size) { mouseSpotVisible = false }
    }
    AboutWindow(visible = aboutWindowVisible) { aboutWindowVisible = false }
}

@Composable
fun MouseSpot(position: DpOffset, size: Dp, onCloseRequest: () -> Unit) {
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
