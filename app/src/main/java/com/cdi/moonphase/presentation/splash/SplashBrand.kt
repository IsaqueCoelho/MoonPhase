package com.cdi.moonphase.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cdi.moonphase.R
import com.cdi.moonphase.presentation.designsystem.MoonDisk
import com.cdi.moonphase.presentation.designsystem.MoonTheme
import com.cdi.moonphase.presentation.designsystem.StarField
import kotlinx.coroutines.delay

/**
 * The Compose "brand phase" of the splash (REQ-02, stage 2). This is *not* a separate Activity
 * or navigation route — it is the first composition shown while the Home screen completes its
 * initial location/calculation work, so there is never a blank white frame between the system
 * splash icon and real content.
 *
 * The decorative gradient and the haloed Moon live here in Compose (the solid
 * `windowSplashScreenBackground` cannot host a gradient). The spinner is conditional: it only
 * fades in if loading runs past [spinnerDelayMs], so fast cold starts cut straight to Home.
 */
@Composable
fun SplashBrand(
    modifier: Modifier = Modifier,
    spinnerDelayMs: Long = 500,
) {
    val palette = MoonTheme.colors

    var showSpinner by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(spinnerDelayMs)
        showSpinner = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val brush = if (palette.splashRadial) {
                    Brush.radialGradient(
                        colors = palette.splashGradient,
                        center = Offset(size.width / 2f, size.height * 0.32f),
                        radius = size.maxDimension * 0.8f,
                    )
                } else {
                    Brush.linearGradient(colors = palette.splashGradient)
                }
                drawRect(brush)
            },
    ) {
        if (palette.isDark) {
            StarField(modifier = Modifier.fillMaxSize(), count = 6, maxAlpha = 0.15f)
        }

        // Brand block: Moon at ~30% height, name + tagline beneath it.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.fillMaxSize(0.30f))
            MoonDisk(
                illumination = 1f,
                waxing = false,
                haloPulse = true,
                contentDescription = null,
                modifier = Modifier.size(148.dp),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                color = palette.textPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.app_tagline),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )
        }

        // Spinner anchored 40 dp from the base, only once loading exceeds the threshold.
        AnimatedVisibility(
            visible = showSpinner,
            enter = fadeIn(animationSpec = tween(durationMillis = 200, easing = LinearEasing)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = palette.amber,
                trackColor = palette.textTertiary.copy(alpha = 0.25f),
                strokeWidth = 3.dp,
            )
        }
    }
}
