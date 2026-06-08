package com.cdi.moonphase.presentation.designsystem

import android.provider.Settings
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * True when the user (or the system, e.g. battery saver / "remove animations") has reduced or
 * disabled animations via `ANIMATOR_DURATION_SCALE`. Continuous loops should be skipped when
 * this is set, per the accessibility requirements.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        scale == 0f
    }
}

/** A twinkling star, expressed in fractional canvas coordinates so it scales with its box. */
private data class Star(val xFraction: Float, val yFraction: Float, val radiusDp: Float, val phase: Float)

// Deterministic, hand-placed constellation — no per-frame allocation, no randomness at runtime.
private val STARS = listOf(
    Star(0.12f, 0.10f, 1.6f, 0.0f),
    Star(0.85f, 0.08f, 1.2f, 0.25f),
    Star(0.22f, 0.30f, 1.0f, 0.5f),
    Star(0.78f, 0.34f, 1.8f, 0.75f),
    Star(0.50f, 0.06f, 1.1f, 0.4f),
    Star(0.92f, 0.55f, 1.3f, 0.15f),
    Star(0.08f, 0.52f, 1.4f, 0.65f),
)

/**
 * A field of softly twinkling stars for the dark theme. Stars cintilam in alpha on
 * desynchronised loops; the whole field is inert (drawn at a steady low alpha) when motion is
 * reduced. Renders nothing in light mode by leaving [count] at 0 from the caller.
 *
 * @param count how many of the predefined stars to show (corners-first ordering).
 * @param maxAlpha the peak alpha a star reaches, in `[0, 1]`.
 */
@Composable
fun StarField(
    modifier: Modifier = Modifier,
    count: Int = STARS.size,
    color: Color = Color.White,
    maxAlpha: Float = 0.15f,
) {
    val stars = remember(count) { STARS.take(count.coerceIn(0, STARS.size)) }
    if (stars.isEmpty()) return

    val reduceMotion = rememberReduceMotion()
    val transition = rememberInfiniteTransition(label = "stars")
    val twinkle = if (reduceMotion) {
        null
    } else {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 5_000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "twinkle",
        )
    }

    Canvas(modifier = modifier) {
        stars.forEach { star ->
            // Each star phases its twinkle differently so the field shimmers asynchronously.
            val t = twinkle?.value ?: 0.6f
            val cycled = (t + star.phase) % 1f
            val alpha = (0.25f + 0.75f * cycled) * maxAlpha
            drawCircle(
                color = color.copy(alpha = alpha.coerceIn(0f, 1f)),
                radius = star.radiusDp * density,
                center = Offset(star.xFraction * size.width, star.yFraction * size.height),
            )
        }
    }
}
