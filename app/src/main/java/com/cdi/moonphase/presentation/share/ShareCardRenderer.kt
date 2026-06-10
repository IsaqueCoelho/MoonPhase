package com.cdi.moonphase.presentation.share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.cdi.moonphase.domain.model.CalendarMonth
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * The colors the exported share cards need, as plain ARGB ints so the renderer stays free of
 * Compose. Built from the active [com.cdi.moonphase.presentation.designsystem.MoonPalette] at the
 * call site (`.toArgb()`), so the asset follows the active theme (Tela 07).
 */
data class ShareTheme(
    val gradientTop: Int,
    val gradientBottom: Int,
    val moonLit: Int,
    val moonShadow: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val accent: Int,
)

/**
 * Renders the shareable assets off-screen onto a [Bitmap] at full export resolution (never a
 * screenshot, never an upscaled view) — see Tela 07. Two variants: a 1:1 phase card and a 4:5
 * month card. Drawing uses raw [android.graphics] so it has no Compose/measure dependency.
 */
object ShareCardRenderer {

    private const val PHASE_SIZE = 1080
    private const val MONTH_WIDTH = 1080
    private const val MONTH_HEIGHT = 1350 // 4:5

    /** The 1:1 "phase of the day" card: gradient, disc in the upper third, title, caption, brand. */
    fun renderPhaseCard(
        theme: ShareTheme,
        illumination: Float,
        waxing: Boolean,
        title: String,
        caption: String,
        brand: String,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(PHASE_SIZE, PHASE_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val w = PHASE_SIZE.toFloat()
        val h = PHASE_SIZE.toFloat()

        drawBackground(canvas, theme, w, h)

        // Disc ~36% of width, centered in the upper third.
        val radius = w * 0.18f
        drawMoon(canvas, theme, w / 2f, h * 0.34f, radius, illumination, waxing)

        val titlePaint = textPaint(theme.textPrimary, w * 0.066f, FontWeight.MEDIUM)
        canvas.drawText(title, w / 2f, h * 0.66f, titlePaint)

        val captionPaint = textPaint(theme.textSecondary, w * 0.038f, FontWeight.NORMAL)
        canvas.drawText(caption, w / 2f, h * 0.73f, captionPaint)

        drawBrand(canvas, theme, brand, w / 2f, h * 0.94f, w * 0.03f)
        return bitmap
    }

    /** The month card: header, weekday row, mini-Moon grid with today's ring, brand. */
    fun renderMonthCard(
        theme: ShareTheme,
        calendar: CalendarMonth,
        today: LocalDate,
        monthTitle: String,
        weekdayInitials: List<String>,
        brand: String,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(MONTH_WIDTH, MONTH_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val w = MONTH_WIDTH.toFloat()
        val h = MONTH_HEIGHT.toFloat()

        drawBackground(canvas, theme, w, h)

        canvas.drawText(monthTitle, w / 2f, h * 0.085f, textPaint(theme.textPrimary, w * 0.05f, FontWeight.MEDIUM))

        val sidePadding = w * 0.07f
        val gridWidth = w - sidePadding * 2f
        val columnStep = gridWidth / 7f
        val columnCenter = { col: Int -> sidePadding + columnStep * (col + 0.5f) }

        // Weekday initials.
        val weekdayPaint = textPaint(theme.textSecondary, w * 0.028f, FontWeight.NORMAL)
        val weekdayY = h * 0.135f
        weekdayInitials.forEachIndexed { col, label ->
            canvas.drawText(label, columnCenter(col), weekdayY, weekdayPaint)
        }

        // Grid. Week starts Sunday: SUNDAY -> column 0.
        val firstDay = calendar.yearMonth.atDay(1)
        val leadingBlanks = firstDay.dayOfWeek.value % 7
        val rowCount = ceil((leadingBlanks + calendar.days.size) / 7.0).toInt()
        val gridTop = h * 0.18f
        val gridBottom = h * 0.9f
        val rowStep = (gridBottom - gridTop) / max(rowCount, 1)
        val miniRadius = min(columnStep, rowStep) * 0.26f

        val dayNumberPaint = textPaint(theme.textSecondary, w * 0.024f, FontWeight.NORMAL)
        calendar.days.forEach { day ->
            val cellIndex = leadingBlanks + (day.date.dayOfMonth - 1)
            val col = cellIndex % 7
            val row = cellIndex / 7
            val cx = columnCenter(col)
            val cy = gridTop + rowStep * (row + 0.4f)

            if (day.date == today) {
                val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = w * 0.006f
                    color = theme.accent
                }
                canvas.drawCircle(cx, cy, miniRadius * 1.9f, ringPaint)
            }

            drawMoon(
                canvas, theme, cx, cy, miniRadius,
                day.illumination.value.toFloat(), day.phase.isWaxing,
            )
            canvas.drawText(day.date.dayOfMonth.toString(), cx, cy + miniRadius * 2.6f, dayNumberPaint)
        }

        drawBrand(canvas, theme, brand, w / 2f, h * 0.965f, w * 0.026f)
        return bitmap
    }

    private fun drawBackground(canvas: Canvas, theme: ShareTheme, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h,
                theme.gradientTop, theme.gradientBottom,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, w, h, paint)
    }

    /**
     * Draws the parametric Moon — the same terminator geometry as the Compose
     * [com.cdi.moonphase.presentation.designsystem.MoonDisk], ported to [android.graphics] so the
     * exported asset matches the on-screen disc.
     */
    private fun drawMoon(
        canvas: Canvas,
        theme: ShareTheme,
        cx: Float,
        cy: Float,
        radius: Float,
        illumination: Float,
        waxing: Boolean,
    ) {
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.moonShadow }
        canvas.drawCircle(cx, cy, radius, shadowPaint)

        val litPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.moonLit }
        canvas.drawPath(litPath(cx, cy, radius, illumination, waxing), litPaint)
    }

    private fun litPath(cx: Float, cy: Float, radius: Float, illumination: Float, waxing: Boolean): Path {
        val lit = illumination.coerceIn(0f, 1f)
        val side = if (waxing) 1f else -1f
        val signedTerminator = 1f - 2f * lit
        val terminatorRx = max(abs(signedTerminator) * radius, 0.01f)

        val disc = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        val terminator = RectF(cx - terminatorRx, cy - radius, cx + terminatorRx, cy + radius)

        return Path().apply {
            moveTo(cx, cy - radius)
            arcTo(disc, -90f, 180f * side)
            val innerSweep = -side * (if (signedTerminator >= 0f) 1f else -1f) * 180f
            arcTo(terminator, 90f, innerSweep)
            close()
        }
    }

    private fun drawBrand(canvas: Canvas, theme: ShareTheme, brand: String, x: Float, y: Float, textSize: Float) {
        val paint = textPaint(theme.textSecondary, textSize, FontWeight.NORMAL).apply {
            alpha = (0.7f * 255).toInt()
        }
        canvas.drawText(brand, x, y, paint)
    }

    private enum class FontWeight { NORMAL, MEDIUM }

    private fun textPaint(color: Int, size: Float, weight: FontWeight): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            textAlign = Paint.Align.CENTER
            typeface = when (weight) {
                FontWeight.MEDIUM -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                FontWeight.NORMAL -> Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
        }
}
