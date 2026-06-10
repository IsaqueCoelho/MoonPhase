package com.cdi.moonphase.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cdi.moonphase.R
import com.cdi.moonphase.domain.model.MoonInfo
import com.cdi.moonphase.presentation.common.descriptionRes
import com.cdi.moonphase.presentation.common.formatFull
import com.cdi.moonphase.presentation.common.formatLong
import com.cdi.moonphase.presentation.common.nameRes
import com.cdi.moonphase.presentation.designsystem.MoonDisk
import com.cdi.moonphase.presentation.designsystem.MoonTheme
import com.cdi.moonphase.presentation.share.ImageSharer
import com.cdi.moonphase.presentation.share.ShareCardRenderer
import com.cdi.moonphase.presentation.share.rememberShareTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Day-detail bottom sheet (Tela 06). Rises over the calendar — which stays visible as context —
 * and reuses the Home components (disc, name, illumination pill, description) at sheet scale. Its
 * primary action exports the phase card (Tela 07). Works for any date, so it doubles as the
 * "histórico" mechanism.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailSheet(
    moonInfo: MoonInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MoonTheme.colors
    // Backdrop darkens the calendar behind: 35% in light, 55% in dark (Tela 06 spec).
    val scrim = Color.Black.copy(alpha = if (palette.isDark) 0.55f else 0.35f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(),
        containerColor = palette.background,
        contentColor = palette.textPrimary,
        scrimColor = scrim,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { SheetHandle() },
    ) {
        DayDetailContent(moonInfo)
    }
}

@Composable
private fun DayDetailContent(moonInfo: MoonInfo) {
    val palette = MoonTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val shareTheme = rememberShareTheme()

    val phaseName = stringResource(moonInfo.phase.type.nameRes)
    val percent = moonInfo.illumination.percent
    val caption = stringResource(
        R.string.share_caption_format,
        moonInfo.date.formatLong(),
        percent,
    )
    val brand = stringResource(R.string.share_brand)
    val chooserTitle = stringResource(R.string.share_chooser_title)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = moonInfo.date.formatFull(),
            fontSize = 13.sp,
            color = palette.textSecondary,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(20.dp))
        MoonDisk(
            illumination = moonInfo.illumination.value.toFloat(),
            waxing = moonInfo.phase.isWaxing,
            haloPulse = true,
            contentDescription = phaseName,
            modifier = Modifier
                .size(108.dp)
                .aspectRatio(1f),
        )

        Spacer(Modifier.height(16.dp))
        Text(
            text = phaseName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = palette.textPrimary,
        )

        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(palette.surface)
                .padding(horizontal = 14.dp, vertical = 7.dp),
        ) {
            Text(
                text = stringResource(R.string.home_illumination_format, percent),
                fontSize = 13.sp,
                color = palette.textSecondary,
            )
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(moonInfo.phase.type.descriptionRes),
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = palette.textSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))
        ShareButton(
            label = stringResource(R.string.sheet_share_phase),
            onClick = {
                scope.launch {
                    val bitmap = withContext(Dispatchers.Default) {
                        ShareCardRenderer.renderPhaseCard(
                            theme = shareTheme,
                            illumination = moonInfo.illumination.value.toFloat(),
                            waxing = moonInfo.phase.isWaxing,
                            title = phaseName,
                            caption = caption,
                            brand = brand,
                        )
                    }
                    ImageSharer.share(context, bitmap, "moon-phase.png", chooserTitle)
                }
            },
        )
    }
}

@Composable
private fun SheetHandle() {
    val palette = MoonTheme.colors
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(CircleShape)
                .background(palette.textTertiary),
        )
    }
}

@Composable
private fun ShareButton(label: String, onClick: () -> Unit) {
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
