package dev.tkuenneth.mousefinder

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.awaitCancellation
import mousefinder.composeapp.generated.resources.Res
import mousefinder.composeapp.generated.resources.app_icon
import mousefinder.composeapp.generated.resources.cancel
import mousefinder.composeapp.generated.resources.app_title
import mousefinder.composeapp.generated.resources.menu_settings
import mousefinder.composeapp.generated.resources.mouse_jump
import mousefinder.composeapp.generated.resources.new_shortcut
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.util.prefs.Preferences

private const val PREF_KEY_MOUSE_FINDER_MODIFIERS = "modifiers"
private const val PREF_KEY_MOUSE_FINDER_KEYCODE = "keyCode"

private const val PREF_KEY_MOUSE_JUMP_MODIFIERS = "modifiersMouseJump"
private const val PREF_KEY_MOUSE_JUMP_KEYCODE = "keyCodeMouseJump"

data class MouseFinderShortcut(
    val modifiers: Int,
    val keyCode: Int
)

enum class MouseFinderShortcutType {
    MouseFinder,
    MouseJump
}

private val prefs = Preferences.userNodeForPackage(MouseFinderShortcut::class.java)

fun getMouseFinderShortcut(): MouseFinderShortcut {
    val modifiers = prefs.getInt(PREF_KEY_MOUSE_FINDER_MODIFIERS, NativeKeyEvent.ALT_MASK or NativeKeyEvent.CTRL_MASK)
    val keyCode = prefs.getInt(PREF_KEY_MOUSE_FINDER_KEYCODE, NativeKeyEvent.VC_M)
    return MouseFinderShortcut(
        modifiers = modifiers,
        keyCode = keyCode
    )
}

fun putMouseFinderShortcut(shortcut: MouseFinderShortcut) {
    prefs.putInt(PREF_KEY_MOUSE_FINDER_MODIFIERS, shortcut.modifiers)
    prefs.putInt(PREF_KEY_MOUSE_FINDER_KEYCODE, shortcut.keyCode)
}

fun getMouseJumpShortcut(): MouseFinderShortcut {
    val modifiers = prefs.getInt(PREF_KEY_MOUSE_JUMP_MODIFIERS, NativeKeyEvent.ALT_MASK or NativeKeyEvent.CTRL_MASK)
    val keyCode = prefs.getInt(PREF_KEY_MOUSE_JUMP_KEYCODE, NativeKeyEvent.VC_W)
    return MouseFinderShortcut(
        modifiers = modifiers,
        keyCode = keyCode
    )
}

fun putMouseJumpShortcut(shortcut: MouseFinderShortcut) {
    prefs.putInt(PREF_KEY_MOUSE_JUMP_MODIFIERS, shortcut.modifiers)
    prefs.putInt(PREF_KEY_MOUSE_JUMP_KEYCODE, shortcut.keyCode)
}

@Composable
fun SettingsWindow(
    visible: Boolean,
    shortcutMouseFinder: MouseFinderShortcut,
    shortcutMouseJump: MouseFinderShortcut,
    allowShortcuts: (Boolean) -> Unit,
    onShortcutChanged: (MouseFinderShortcut, MouseFinderShortcutType) -> Unit,
    onCloseRequest: () -> Unit
) {
    if (visible) {
        DialogWindow(
            state = rememberDialogState(),
            onCloseRequest = onCloseRequest,
            icon = painterResource(Res.drawable.app_icon),
            resizable = false,
            title = stringResource(Res.string.menu_settings),
        ) {
            Settings(
                shortcutMouseFinder = shortcutMouseFinder,
                shortcutMouseJump = shortcutMouseJump,
                allowShortcuts = allowShortcuts,
                onShortcutChanged = onShortcutChanged
            )
        }
    }
}

@Composable
fun Settings(
    shortcutMouseFinder: MouseFinderShortcut,
    shortcutMouseJump: MouseFinderShortcut,
    allowShortcuts: (Boolean) -> Unit,
    onShortcutChanged: (MouseFinderShortcut, MouseFinderShortcutType) -> Unit,
) {
    var changePendingFor: MouseFinderShortcutType? by remember { mutableStateOf(null) }
    LaunchedEffect(changePendingFor) {
        allowShortcuts(changePendingFor == null)
        if (changePendingFor != null) {
            val listener = SettingsKeyListener {
                val current = changePendingFor
                if (current != null) {
                    onShortcutChanged(it, current)
                }
                changePendingFor = null
            }
            GlobalScreen.addNativeKeyListener(listener)
            try {
                awaitCancellation()
            } finally {
                GlobalScreen.removeNativeKeyListener(listener)
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Crossfade(
            targetState = changePendingFor == null,
            animationSpec = tween()
        ) { isVisible ->
            if (isVisible) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        MouseFinderShortcutColumn(
                            shortcut = shortcutMouseFinder,
                            type = MouseFinderShortcutType.MouseFinder
                        ) { changePendingFor = MouseFinderShortcutType.MouseFinder }
                        MouseFinderShortcutColumn(
                            shortcut = shortcutMouseJump,
                            type = MouseFinderShortcutType.MouseJump
                        ) {
                            changePendingFor = MouseFinderShortcutType.MouseJump
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(Res.string.new_shortcut),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(onClick = { changePendingFor = null }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun MouseFinderShortcutColumn(
    shortcut: MouseFinderShortcut, type: MouseFinderShortcutType, onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = getTextForMouseFinderShortcutType(type),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onClick) {
            val text = "${NativeKeyEvent.getModifiersText(shortcut.modifiers)}+${
                NativeKeyEvent.getKeyText(shortcut.keyCode)
            }"
            Text(
                text = text
            )
        }
    }
}

@Composable
private fun getTextForMouseFinderShortcutType(type: MouseFinderShortcutType) = stringResource(
    when (type) {
        MouseFinderShortcutType.MouseFinder -> Res.string.app_title
        MouseFinderShortcutType.MouseJump -> Res.string.mouse_jump
    }
)

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
