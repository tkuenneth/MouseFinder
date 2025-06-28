package dev.tkuenneth.mousefinder

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import mousefinder.composeapp.generated.resources.Res
import mousefinder.composeapp.generated.resources.app_icon
import mousefinder.composeapp.generated.resources.app_title
import mousefinder.composeapp.generated.resources.homepage
import mousefinder.composeapp.generated.resources.made_with_love
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun About(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.app_icon), null,
            modifier = Modifier.size(96.dp)
        )
        Text(
            text = "${stringResource(Res.string.app_title)} $VERSION",
            modifier = Modifier.padding(vertical = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(Res.string.made_with_love),
            color = MaterialTheme.colorScheme.onSurface
        )
        val url = stringResource(Res.string.homepage)
        Text(
            text = url,
            modifier = Modifier.clickable {
                uriHandler.openUri(url)
            },
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = platformName,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AboutWindow(visible: Boolean, onCloseRequest: () -> Unit) {
    if (visible) {
        DialogWindow(
            state = rememberDialogState(),
            onCloseRequest = onCloseRequest,
            icon = painterResource(Res.drawable.app_icon),
            resizable = false,
            title = stringResource(Res.string.app_title)
        ) {
            About(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
        }
    }
}
