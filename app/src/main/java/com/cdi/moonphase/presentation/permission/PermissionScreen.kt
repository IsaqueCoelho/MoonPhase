package com.cdi.moonphase.presentation.permission

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cdi.moonphase.R
import com.cdi.moonphase.presentation.designsystem.MoonDisk
import com.cdi.moonphase.presentation.designsystem.MoonTheme
import com.cdi.moonphase.presentation.designsystem.StarField

@Composable
fun PermissionScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PermissionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.onPermissionResult(granted) }

    LaunchedEffect(Unit) {
        viewModel.navigateToHome.collect { onNavigateToHome() }
    }

    PermissionContent(
        uiState = uiState,
        onRequestPermission = {
            viewModel.onAllowClicked()
            // Privacy by default: only COARSE is ever requested, never FINE.
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        },
        onOpenSettings = {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                "package:${context.packageName}".toUri(),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        },
        onContinueWithout = viewModel::onContinueWithoutLocation,
        modifier = modifier,
    )
}

@Composable
internal fun PermissionContent(
    uiState: PermissionUiState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onContinueWithout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MoonTheme.colors
    val deniedOnce = uiState.stage == PermissionStage.DeniedOnce

    Surface(modifier = modifier.fillMaxSize(), color = palette.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (palette.isDark) {
                StarField(modifier = Modifier.fillMaxSize(), count = 3, maxAlpha = 0.15f)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                PermissionIllustration()

                Spacer(Modifier.height(28.dp))
                Text(
                    text = stringResource(R.string.permission_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.permission_rationale),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(24.dp))
                BenefitsList()

                if (deniedOnce) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.permission_denied_hint),
                        fontSize = 13.sp,
                        color = palette.textTertiary,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(28.dp))
                PrimaryButton(
                    label = stringResource(
                        if (deniedOnce) R.string.permission_open_settings
                        else R.string.permission_allow,
                    ),
                    onClick = if (deniedOnce) onOpenSettings else onRequestPermission,
                )

                Spacer(Modifier.height(28.dp))
                EscapeLink(
                    label = stringResource(R.string.permission_continue_without),
                    onClick = onContinueWithout,
                )
            }
        }
    }
}

@Composable
private fun BenefitsList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BenefitRow(BenefitIcon.Clock, stringResource(R.string.permission_benefit_region))
        BenefitRow(BenefitIcon.Moon, stringResource(R.string.permission_benefit_orientation))
        BenefitRow(BenefitIcon.Lock, stringResource(R.string.permission_benefit_privacy))
    }
}

private enum class BenefitIcon { Clock, Moon, Lock }

@Composable
private fun BenefitRow(icon: BenefitIcon, text: String) {
    val palette = MoonTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(24.dp)) {
            when (icon) {
                BenefitIcon.Clock -> drawClock(palette.amber)
                BenefitIcon.Moon -> drawCrescent(palette.amber, palette.background)
                BenefitIcon.Lock -> drawLock(palette.amber)
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = palette.textSecondary,
        )
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    val palette = MoonTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.amber)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = palette.onAmber,
        )
    }
}

@Composable
private fun EscapeLink(label: String, onClick: () -> Unit) {
    val palette = MoonTheme.colors
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = palette.textTertiary,
        )
    }
}

/** A 120 dp illustration: a haloed Moon with a location pin, on a soft gradient backdrop. */
@Composable
private fun PermissionIllustration() {
    val palette = MoonTheme.colors
    val description = stringResource(R.string.permission_illustration_cd)
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    if (palette.isDark) palette.splashGradient
                    else listOf(Color(0xFFDCE7F2), Color(0xFFFFFFFF)),
                ),
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        MoonDisk(
            illumination = 1f,
            waxing = false,
            haloPulse = true,
            modifier = Modifier.size(84.dp),
        )
        Canvas(
            modifier = Modifier
                .size(34.dp)
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 12.dp),
        ) {
            drawPin(fill = palette.amber, dot = palette.onAmber)
        }
    }
}

// --- Canvas glyphs (no icon-font dependency) ---

private fun DrawScope.drawClock(color: Color) {
    val r = size.minDimension * 0.42f
    val stroke = size.minDimension * 0.08f
    drawCircle(color = color, radius = r, center = center, style = Stroke(width = stroke))
    drawLine(
        color = color,
        start = center,
        end = Offset(center.x, center.y - r * 0.55f),
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color,
        start = center,
        end = Offset(center.x + r * 0.45f, center.y),
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawCrescent(color: Color, background: Color) {
    val r = size.minDimension * 0.42f
    drawCircle(color = color, radius = r, center = center)
    // Carve the crescent by overdrawing with the screen background (no offscreen layer needed).
    drawCircle(
        color = background,
        radius = r,
        center = Offset(center.x + r * 0.5f, center.y - r * 0.28f),
    )
}

private fun DrawScope.drawLock(color: Color) {
    val w = size.minDimension
    val bodyW = w * 0.56f
    val bodyH = w * 0.44f
    val left = center.x - bodyW / 2f
    val top = center.y - bodyH * 0.1f
    val stroke = w * 0.08f
    // Shackle.
    drawArc(
        color = color,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - bodyW * 0.3f, top - bodyH * 0.6f),
        size = Size(bodyW * 0.6f, bodyH * 0.6f),
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
    // Body.
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(bodyW, bodyH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(stroke, stroke),
    )
}

private fun DrawScope.drawPin(fill: Color, dot: Color) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val headRadius = w * 0.35f
    val headCenter = Offset(cx, headRadius)
    val path = Path().apply {
        // Rounded head.
        addOval(
            androidx.compose.ui.geometry.Rect(
                left = cx - headRadius,
                top = 0f,
                right = cx + headRadius,
                bottom = headRadius * 2f,
            ),
        )
        // Tail to the tip.
        moveTo(cx - headRadius * 0.7f, headRadius * 1.5f)
        lineTo(cx, h)
        lineTo(cx + headRadius * 0.7f, headRadius * 1.5f)
        close()
    }
    drawPath(path, color = fill)
    drawCircle(color = dot, radius = headRadius * 0.4f, center = headCenter)
}
