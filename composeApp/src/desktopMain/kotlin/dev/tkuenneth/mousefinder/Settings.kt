package dev.tkuenneth.mousefinder

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import mousefinder.composeapp.generated.resources.Res
import mousefinder.composeapp.generated.resources.app_icon
import mousefinder.composeapp.generated.resources.app_title
import mousefinder.composeapp.generated.resources.cancel
import mousefinder.composeapp.generated.resources.change
import mousefinder.composeapp.generated.resources.current_shortcut
import mousefinder.composeapp.generated.resources.new_shortcut
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.util.prefs.Preferences

private const val PREF_KEY_MODIFIERS = "modifiers"
private const val PREF_KEY_KEYCODE = "keyCode"

data class MouseFinderShortcut(
    val modifiers: Int,
    val keyCode: Int
)

private val prefs = Preferences.userNodeForPackage(MouseFinderShortcut::class.java)

fun getMouseFinderShortcut(): MouseFinderShortcut {
    val modifiers = prefs.getInt(PREF_KEY_MODIFIERS, NativeKeyEvent.ALT_MASK or NativeKeyEvent.CTRL_MASK)
    val keyCode = prefs.getInt(PREF_KEY_KEYCODE, NativeKeyEvent.VC_M)
    return MouseFinderShortcut(
        modifiers = modifiers,
        keyCode = keyCode
    )
}

fun putMouseFinderShortcut(shortcut: MouseFinderShortcut) {
    prefs.putInt(PREF_KEY_MODIFIERS, shortcut.modifiers)
    prefs.putInt(PREF_KEY_KEYCODE, shortcut.keyCode)
}

@Composable
fun SettingsWindow(
    visible: Boolean,
    shortcut: MouseFinderShortcut,
    allowShortcuts: (Boolean) -> Unit,
    onShortcutChanged: (MouseFinderShortcut) -> Unit,
    onCloseRequest: () -> Unit
) {
    if (visible) {
        DialogWindow(
            state = rememberDialogState(),
            onCloseRequest = onCloseRequest,
            icon = painterResource(Res.drawable.app_icon),
            resizable = false,
            title = stringResource(Res.string.app_title),
        ) {
            Settings(
                shortcut = shortcut,
                allowShortcuts = allowShortcuts,
                onShortcutChanged = onShortcutChanged
            )
        }
    }
}

@Composable
fun Settings(
    shortcut: MouseFinderShortcut,
    allowShortcuts: (Boolean) -> Unit,
    onShortcutChanged: (MouseFinderShortcut) -> Unit,
) {
    var changeButtonVisible by remember(shortcut) { mutableStateOf(true) }
    LaunchedEffect(changeButtonVisible) {
        allowShortcuts(changeButtonVisible)
    }
    if (!changeButtonVisible) {
        DisposableEffect(Unit) {
            val listener = SettingsKeyListener {
                onShortcutChanged(it)
                changeButtonVisible = true
            }
            GlobalScreen.addNativeKeyListener(listener)
            onDispose {
                GlobalScreen.removeNativeKeyListener(listener)
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        val text = "${NativeKeyEvent.getModifiersText(shortcut.modifiers)}+${
            NativeKeyEvent.getKeyText(shortcut.keyCode)
        }"
        Crossfade(
            targetState = changeButtonVisible,
            animationSpec = tween()
        ) { isVisible ->
            if (isVisible) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.current_shortcut),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(onClick = { changeButtonVisible = false }) {
                        Text(stringResource(Res.string.change))
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.new_shortcut),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(onClick = { changeButtonVisible = true }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        }
    }
}

private class SettingsKeyListener(
    private val onShortcutChanged: (MouseFinderShortcut) -> Unit
) : NativeKeyListener {

    private var modifiers = 0

    override fun nativeKeyPressed(nativeEvent: NativeKeyEvent) {
        when (nativeEvent.keyCode) {
            NativeKeyEvent.VC_CONTROL -> modifiers = modifiers or NativeKeyEvent.CTRL_MASK
            NativeKeyEvent.VC_ALT -> modifiers = modifiers or NativeKeyEvent.ALT_MASK
            NativeKeyEvent.VC_SHIFT -> modifiers = modifiers or NativeKeyEvent.SHIFT_MASK
            NativeKeyEvent.VC_META -> modifiers = modifiers or NativeKeyEvent.META_MASK
            nativeEvent.keyCode -> {
                onShortcutChanged(
                    MouseFinderShortcut(
                        modifiers = modifiers,
                        keyCode = nativeEvent.keyCode
                    )
                )
            }
        }
    }

    override fun nativeKeyReleased(e: NativeKeyEvent) {
        when (e.keyCode) {
            NativeKeyEvent.VC_CONTROL -> modifiers = modifiers and NativeKeyEvent.CTRL_MASK.inv()
            NativeKeyEvent.VC_ALT -> modifiers = modifiers and NativeKeyEvent.ALT_MASK.inv()
            NativeKeyEvent.VC_SHIFT -> modifiers = modifiers and NativeKeyEvent.SHIFT_MASK.inv()
            NativeKeyEvent.VC_META -> modifiers = modifiers and NativeKeyEvent.META_MASK.inv()
        }
    }
}