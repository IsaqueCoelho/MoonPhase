package com.cdi.moonphase.presentation.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.abs

/**
 * A single-canvas, parametric Moon. All eight phases are built geometrically from
 * [illumination] (0 = New, 1 = Full) and [waxing] — there are no per-phase bitmaps. The
 * terminator is a half-ellipse whose horizontal radius is `r * (1 - 2*illumination)`, exactly
 * the projection of the day/night boundary onto the visible disk.
 *
 * Rendering follows the spec per theme: light draws a faint black-6% limb so the disk does not
 * dissolve into the pale background; dark draws a soft radial halo instead of an outline, and
 * (when [haloPulse] is on and motion is allowed) the halo pulses on a ~5 s loop.
 *
 * The incoming [illumination] is animated (~800 ms, FastOutSlowIn) so the terminator glides
 * when the value changes (e.g. on refresh) rather than snapping.
 *
 * @param illumination lit fraction in `[0, 1]`.
 * @param waxing if true the lit limb is on the right (northern-hemisphere convention).
 * @param contentDescription accessibility label, typically the phase name.
 * @param haloPulse animate the dark-theme halo; ignored in light or when motion is reduced.
 * @param decorate draw the per-theme halo (dark) and faint limb (light). Turn this off for the
 *   tiny calendar mini-moons, which read better as plain lit/shadow discs (see the prototype).
 */
@Composable
fun MoonDisk(
    illumination: Float,
    waxing: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    haloPulse: Boolean = false,
    decorate: Boolean = true,
) {
    val palette = MoonTheme.colors

    val animatedIllumination by animateFloatAsState(
        targetValue = illumination.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "illumination",
    )

    val reduceMotion = rememberReduceMotion()
    val haloScale = if (palette.isDark && decorate && haloPulse && !reduceMotion) {
        val transition = rememberInfiniteTransition(label = "halo")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 5_000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "haloScale",
        ).value
    } else {
        1f
    }

    val diskModifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else {
        modifier
    }

    Canvas(modifier = diskModifier) {
        val radius = minOf(size.width, size.height) / 2f * 0.86f
        val center = Offset(size.width / 2f, size.height / 2f)

        if (palette.isDark && decorate) {
            drawHalo(center, radius * haloScale, palette.halo)
        }

        // Shadowed base disk, then paint the lit region on top.
        drawCircle(color = palette.moonShadow, radius = radius, center = center)
        drawPath(
            litShape(center, radius, animatedIllumination, waxing),
            color = palette.moonLit,
        )

        // Light theme keeps a faint limb so the pale disk does not dissolve into the background.
        if (decorate && palette.moonOutline.alpha > 0f) {
            drawCircle(
                color = palette.moonOutline,
                radius = radius,
                center = center,
                style = Stroke(width = radius * 0.06f),
            )
        }
    }
}

private fun DrawScope.drawHalo(center: Offset, radius: Float, halo: Color) {
    val haloRadius = radius * 1.6f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(halo.copy(alpha = 0.22f), Color.Transparent),
            center = center,
            radius = haloRadius,
        ),
        radius = haloRadius,
        center = center,
    )
}

/**
 * Builds the lit region as the area enclosed by the outer semicircle on the lit side and the
 * terminator half-ellipse. See the class docs for the geometry.
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
