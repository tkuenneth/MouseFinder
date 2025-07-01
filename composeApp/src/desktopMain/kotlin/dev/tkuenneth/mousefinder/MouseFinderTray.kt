package dev.tkuenneth.mousefinder

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import mousefinder.composeapp.generated.resources.Res
import mousefinder.composeapp.generated.resources.app_icon
import mousefinder.composeapp.generated.resources.app_title
import mousefinder.composeapp.generated.resources.exit
import mousefinder.composeapp.generated.resources.menu_abut
import mousefinder.composeapp.generated.resources.menu_settings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ApplicationScope.MouseFinderTray(settingsClicked: () -> Unit, aboutClicked: () -> Unit) {
    val appTitle = stringResource(Res.string.app_title)
    val menuAbout = stringResource(
        Res.string.menu_abut, appTitle
    )
    Tray(
        icon = painterResource(Res.drawable.app_icon), tooltip = appTitle, menu = {
            Item(
                text = stringResource(Res.string.menu_settings), onClick = settingsClicked
            )
            Item(
                text = menuAbout, onClick = aboutClicked
            )
            Item(
                stringResource(Res.string.exit), onClick = ::exitApplication
            )
        })
}
