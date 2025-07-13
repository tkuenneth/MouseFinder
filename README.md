### Welcome to Mouse Finder

The aim of this app is to find your mouse pointer. Upon a key press, it will highlight its current location. To close the highlighter (the mouse spot), move the mouse pointer outside the circular area or click on it. The keyboard shortcut can be changed in a settings window.

Another feature is *mouse jump*. If the *Mouse Finder* mouse spot is visible, press the keyboard shortcut again to move the mouse pointer to the center of the next screen. If your setup has only one screen, the mouse pointer will be centered.

To get system-wide keyboard shortcuts, *Mouse Finder* uses [JNativeHook](https://github.com/kwhat/jnativehook).

#### Launching Mouse Finder

On Windows, open the Start menu, click *All apps*, and scroll down to find *Mouse Finder* under the *Thomas Kuenneth* section.

#### Granting accessibility access on macOS

When you run *Mouse Finder* on macOS for the first time, you will be alerted by the system that the app requires accessibility access. This is necessary to receive the keyboard shortcuts.

![Requesting accessibility access](screenshots/accessibility_access.png?raw=true "Requesting accessibility access")

Once you have granted access, please quit and relaunch *Mouse Finder*.

#### Known bugs and limitations

- The macOS *.dmg* file currently has no nice background image; instead, you only see an alias to *Applications* and the *Mouse Finder* app. Drag the *Mouse Finder* icon onto the *Applications* alias as usual.
- As mentioned above, once you have granted accessibility access on macOS, you need to quit and restart the app.
- Currently, there is no Linux build. This will be fixed very soon.