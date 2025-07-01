package dev.tkuenneth.mousefinder

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import java.util.logging.Level
import java.util.logging.Logger

class GlobalKeyListener(
    initialShortcut: MouseFinderShortcut, private val onShowWindow: () -> Unit
) : NativeKeyListener {
    private var activeShortcut = initialShortcut
    private var modifiers = 0

    fun updateShortcut(shortcut: MouseFinderShortcut) {
        activeShortcut = shortcut
    }

    override fun nativeKeyPressed(e: NativeKeyEvent) {
        when (e.keyCode) {
            NativeKeyEvent.VC_CONTROL -> modifiers = modifiers or NativeKeyEvent.CTRL_MASK
            NativeKeyEvent.VC_ALT -> modifiers = modifiers or NativeKeyEvent.ALT_MASK
            NativeKeyEvent.VC_SHIFT -> modifiers = modifiers or NativeKeyEvent.SHIFT_MASK
            NativeKeyEvent.VC_META -> modifiers = modifiers or NativeKeyEvent.META_MASK
            activeShortcut.keyCode -> {
                if (activeShortcut.modifiers and modifiers == modifiers) {
                    onShowWindow()
                }
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

fun NativeKeyListener.register() {
    Logger.getLogger(GlobalScreen::class.java.getPackage().name).run {
        level = Level.OFF
        useParentHandlers = false
    }
    try {
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(this)
    } catch (ex: NativeHookException) {
        println("Failed to register global hotkeys: ${ex.message}")
        println("You may need to grant accessibility permissions to this application.")
    }
}

fun NativeKeyListener?.unregister() {
    try {
        GlobalScreen.removeNativeKeyListener(this)
        GlobalScreen.unregisterNativeHook()
    } catch (ex: NativeHookException) {
        println("Error during cleanup: ${ex.message}")
    }
}
