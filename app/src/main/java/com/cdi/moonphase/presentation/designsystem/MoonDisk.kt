package com.cdi.moonphase.presentation.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs

/**
 * A single-canvas, parametric Moon. The lit shape is built geometrically from
 * [illumination] (0 = New, 1 = Full) and [waxing] — there are no per-phase bitmaps. The
 * terminator is a half-ellipse whose horizontal radius is `r * (1 - 2*illumination)`, which
 * is exactly the projection of the day/night boundary onto the visible disk.
 *
 * The incoming [illumination] is animated, so the terminator glides smoothly when the value
 * changes (e.g. on refresh) rather than snapping.
 *
 * @param illumination lit fraction in `[0, 1]`.
 * @param waxing if true the lit limb is on the right (northern-hemisphere convention).
 */
@Composable
fun MoonDisk(
    illumination: Float,
    waxing: Boolean,
    modifier: Modifier = Modifier,
) {
    val animatedIllumination by animateFloatAsState(
        targetValue = illumination.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "illumination",
    )

    val dark = isSystemInDarkTheme()
    val litColor = if (dark) MoonColors.NightMoonLit else MoonColors.DayMoonLit
    val shadowColor = if (dark) MoonColors.NightMoonShadow else MoonColors.DayMoonShadow
    val haloColor = if (dark) MoonColors.NightHalo else MoonColors.DayHalo

    Canvas(modifier = modifier) {
        val radius = minOf(size.width, size.height) / 2f * 0.86f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawHalo(center, radius, haloColor)
        // Dark base disk, then paint the lit region on top.
        drawCircle(color = shadowColor, radius = radius, center = center)
        drawPath(litShape(center, radius, animatedIllumination, waxing), color = litColor)
        // Subtle limb outline to keep the disk readable at New Moon.
        drawCircle(
            color = litColor.copy(alpha = 0.18f),
            radius = radius,
            center = center,
            style = Stroke(width = radius * 0.012f),
        )
    }
}

private fun DrawScope.drawHalo(center: Offset, radius: Float, halo: Color) {
    val haloRadius = radius * 1.55f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(halo.copy(alpha = 0.28f), Color.Transparent),
            center = center,
            radius = haloRadius,
        ),
        radius = haloRadius,
        center = center,
    )
}

/**
 * Builds the lit region as the area enclosed by the outer semicircle on the lit side and the
 * terminator half-ellipse. See class docs for the geometry.
 */
private fun litShape(center: Offset, radius: Float, illumination: Float, waxing: Boolean): Path {
    val lit = illumination.coerceIn(0f, 1f)
    val side = if (waxing) 1f else -1f // +1 = lit on the right, -1 = lit on the left
    val signedTerminator = 1f - 2f * lit // + for crescent, - for gibbous, 0 at quarter
    // Avoid a degenerate zero-width oval exactly at the quarters.
    val terminatorRx = (abs(signedTerminator) * radius).coerceAtLeast(0.01f)

    val discRect = Rect(
        center.x - radius, center.y - radius,
        center.x + radius, center.y + radius,
    )
    val terminatorRect = Rect(
        center.x - terminatorRx, center.y - radius,
        center.x + terminatorRx, center.y + radius,
    )

    return Path().apply {
        // Outer semicircle on the lit side: top -> (lit limb) -> bottom.
        moveTo(center.x, center.y - radius)
        arcTo(discRect, startAngleDegrees = -90f, sweepAngleDegrees = 180f * side, forceMoveTo = false)
        // Terminator half-ellipse back from bottom -> top, bulging toward lit (crescent) or
        // away (gibbous) depending on the sign of the terminator radius.
        val innerSweep = -side * (if (signedTerminator >= 0f) 1f else -1f) * 180f
        arcTo(terminatorRect, startAngleDegrees = 90f, sweepAngleDegrees = innerSweep, forceMoveTo = false)
        close()
    }
}
