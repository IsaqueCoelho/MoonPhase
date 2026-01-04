package com.cdi.moonphase.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp

/**
 * Simple animated moon: a circle with a moving terminator based on illumination.
 * @param illumination 0f..1f illuminated fraction
 * @param waxing if true, light on the right (northern hemisphere convention)
 */
@Composable
fun MoonPhaseAnimation(
    modifier: Modifier = Modifier,
    illumination: Float,
    waxing: Boolean
) {
    val infinite = rememberInfiniteTransition(label = "moon-anim")
    val shimmer by infinite.animateFloat(
        initialValue = -0.05f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    val bg = if (isSystemInDarkTheme()) Color(0xFF0B0E12) else Color(0xFFF2F5F9)
    val moon = if (isSystemInDarkTheme()) Color(0xFFE6E9EF) else Color(0xFFCDD6E5)
    val shadow = if (isSystemInDarkTheme()) Color(0xFF0B0E12) else Color(0xFF1B1F27)

    Canvas(modifier = modifier) {
        val sizePx = minOf(size.width, size.height)
        val radius = sizePx / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Background circle (moon disc)
        drawCircle(color = moon, radius = radius, center = center)

        // Terminator rendering using an oval mask to simulate shadow
        val illum = illumination.coerceIn(0f, 1f)
        drawTerminator(center, radius, 1f - illum + shimmer, waxing, shadow)
        // Thin outline
        drawCircle(color = shadow.copy(alpha = 0.1f), radius = radius, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
    }
}

private fun DrawScope.drawTerminator(center: Offset, radius: Float, darkness: Float, waxing: Boolean, shadow: Color) {
    // darkness 0..1 where 1 = fully dark (new moon)
    val k = darkness.coerceIn(0f, 1f)
    // Create an ellipse width based on k
    val ellipseWidth = radius * (1f + (k * 2f - 1f)) // varies from 0 to 2r
    val leftToRight = waxing

    val rectLeft = if (leftToRight) center.x - ellipseWidth else center.x - radius
    val rectRight = if (leftToRight) center.x + radius else center.x + ellipseWidth
    val rectTop = center.y - radius
    val rectBottom = center.y + radius

    val clipPath = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(rectLeft, rectTop, rectRight, rectBottom))
    }

    // Draw shadow over the disc using the oval clip to create the crescent
    val discRect = androidx.compose.ui.geometry.Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)
    val discPath = Path().apply { addOval(discRect) }

    val fillPath = Path().apply {
        op(discPath, clipPath, androidx.compose.ui.graphics.PathOperation.Difference)
    }
    drawPath(path = fillPath, color = shadow)
}
