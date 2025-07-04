package dev.tkuenneth.mousefinder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState


@Composable
fun MouseSpot(visible: Boolean, position: DpOffset, size: Dp, onCloseRequest: () -> Unit) {
    if (visible) {
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
            val density = LocalDensity.current
            val rectangleColor = MaterialTheme.colorScheme.onBackground
            val backgroundColor = MaterialTheme.colorScheme.background

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Exit || event.type == PointerEventType.Press) {
                                    onCloseRequest()
                                }
                            }
                        }
                    }
            ) {
                val centerX = this.size.width / 2
                val centerY = this.size.height / 2
                val center = Offset(centerX, centerY)
                val borderWidth = with(density) { 8.dp.toPx() }
                val radius = (this.size.minDimension / 2) - (borderWidth / 2)

                drawCircle(
                    color = backgroundColor,
                    radius = radius + (borderWidth / 2),
                    center = center
                )

                drawCircle(
                    color = rectangleColor,
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth)
                )

                val rectangleLength = with(density) { (size / 4).toPx() }
                val rectangleWidth = borderWidth // Reuse the same width
                val halfRectangleWidth = rectangleWidth / 2
                val offset = with(density) { (size / 8).toPx() }

                drawRect(
                    color = rectangleColor,
                    topLeft = Offset(centerX - halfRectangleWidth, centerY - rectangleLength - offset),
                    size = Size(rectangleWidth, rectangleLength)
                )

                drawRect(
                    color = rectangleColor,
                    topLeft = Offset(centerX - halfRectangleWidth, centerY + offset),
                    size = Size(rectangleWidth, rectangleLength)
                )

                drawRect(
                    color = rectangleColor,
                    topLeft = Offset(centerX - rectangleLength - offset, centerY - halfRectangleWidth),
                    size = Size(rectangleLength, rectangleWidth)
                )

                drawRect(
                    color = rectangleColor,
                    topLeft = Offset(centerX + offset, centerY - halfRectangleWidth),
                    size = Size(rectangleLength, rectangleWidth)
                )
            }
        }
    }
}
