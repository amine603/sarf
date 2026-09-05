package com.cash.guide.ui.notebook

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Hand-Drawn Notebook Decorative Flourishes ---

/**
 * Straight underline ending in a solid circle dot (as in student notebooks).
 */
@Composable
fun NotebookUnderlineWithDot(
    modifier: Modifier = Modifier,
    color: Color = HighlighterPink,
    dotOnRight: Boolean = true,
    strokeWidth: Dp = 1.6.dp,
    dotRadius: Dp = 3.5.dp
) {
    Canvas(modifier = modifier.height(8.dp)) {
        val sw = strokeWidth.toPx()
        val r = dotRadius.toPx()
        val y = size.height / 2f

        if (dotOnRight) {
            val lineEnd = size.width - r * 2f
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(lineEnd, y),
                strokeWidth = sw,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = color,
                radius = r,
                center = Offset(size.width - r, y)
            )
        } else {
            val lineStart = r * 2f
            drawCircle(
                color = color,
                radius = r,
                center = Offset(r, y)
            )
            drawLine(
                color = color,
                start = Offset(lineStart, y),
                end = Offset(size.width, y),
                strokeWidth = sw,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Wavy organic underline drawn with hand-like sinusoidal waves.
 */
@Composable
fun NotebookWavyUnderline(
    modifier: Modifier = Modifier,
    color: Color = HighlighterPink,
    strokeWidth: Dp = 1.5.dp,
    waveCount: Int = 8,
    amplitude: Dp = 2.dp
) {
    Canvas(modifier = modifier.height(6.dp)) {
        val sw = strokeWidth.toPx()
        val amp = amplitude.toPx()
        val midY = size.height / 2f
        val w = size.width
        val step = w / waveCount

        val path = Path().apply {
            moveTo(0f, midY)
            var currentX = 0f
            for (i in 0 until waveCount) {
                val cp1X = currentX + step * 0.25f
                val cp1Y = midY - amp
                val cp2X = currentX + step * 0.75f
                val cp2Y = midY + amp
                val endX = currentX + step
                cubicTo(cp1X, cp1Y, cp2X, cp2Y, endX, midY)
                currentX = endX
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = sw, cap = StrokeCap.Round)
        )
    }
}

/**
 * Modifier extension to draw an authentic straight underline ending in a solid dot
 * along the bottom baseline/rule (y = size.height + yOffset) without consuming layout height.
 */
fun Modifier.notebookUnderlineWithDot(
    color: Color = HighlighterPink,
    dotOnRight: Boolean = true,
    strokeWidth: Dp = 1.6.dp,
    dotRadius: Dp = 3.dp,
    yOffset: Dp = 0.dp
): Modifier = drawBehind {
    val sw = strokeWidth.toPx()
    val r = dotRadius.toPx()
    val y = size.height + yOffset.toPx()
    if (dotOnRight) {
        val lineEnd = (size.width - r * 2f).coerceAtLeast(0f)
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(lineEnd, y),
            strokeWidth = sw,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = color,
            radius = r,
            center = Offset(size.width - r, y)
        )
    } else {
        val lineStart = (r * 2f).coerceAtMost(size.width)
        drawCircle(
            color = color,
            radius = r,
            center = Offset(r, y)
        )
        drawLine(
            color = color,
            start = Offset(lineStart, y),
            end = Offset(size.width, y),
            strokeWidth = sw,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Modifier extension to draw a sinusoidal wavy underline
 * along the bottom baseline/rule (y = size.height + yOffset) without consuming layout height.
 */
fun Modifier.notebookWavyUnderline(
    color: Color = HighlighterPink,
    strokeWidth: Dp = 1.5.dp,
    amplitude: Dp = 2.dp,
    wavelength: Dp = 20.dp,
    yOffset: Dp = 0.dp
): Modifier = drawBehind {
    val sw = strokeWidth.toPx()
    val amp = amplitude.toPx()
    val y = size.height + yOffset.toPx()
    val w = size.width
    val waveLenPx = wavelength.toPx()
    val waveCount = (w / waveLenPx).toInt().coerceAtLeast(2)
    val step = w / waveCount

    val path = Path().apply {
        moveTo(0f, y)
        var currentX = 0f
        for (i in 0 until waveCount) {
            val cp1X = currentX + step * 0.25f
            val cp1Y = y - amp
            val cp2X = currentX + step * 0.75f
            val cp2Y = y + amp
            val endX = currentX + step
            cubicTo(cp1X, cp1Y, cp2X, cp2Y, endX, y)
            currentX = endX
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = sw, cap = StrokeCap.Round)
    )
}

/**
 * Authentic pastel highlighter badge for notebook titles and headers:
 * - The colored pill background evenly encompasses ascenders and descenders (ر، و، ي، ع، ج).
 * - Sits naturally centered around the text so all strokes remain fully visible and highlighted.
 */
@Composable
fun NotebookHighlightedBadge(
    text: String,
    highlighterColor: Color,
    modifier: Modifier = Modifier,
    fontFamily: androidx.compose.ui.text.font.FontFamily = TajawalFamily,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    fontWeight: androidx.compose.ui.text.font.FontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
    textColor: Color = JournalInk,
    horizontalPadding: Dp = 10.dp,
    verticalPadding: Dp = 3.5.dp,
    cornerRadius: Dp = 4.dp,
    textOffset: Dp = 0.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(highlighterColor)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = fontFamily,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = textColor,
            style = androidx.compose.ui.text.TextStyle(platformStyle = NoFontPadding),
            modifier = if (textOffset != 0.dp) Modifier.offset(y = textOffset) else Modifier
        )
    }
}

/**
 * Hand-drawn sticky note / post-it with translucent washi tape at top.
 */
@Composable
fun NotebookStickyNote(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFFFF7D6), // Soft pastel yellow
    tapeColor: Color = HighlighterPink.copy(alpha = 0.75f),
    rotationDegrees: Float = -2f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .rotate(rotationDegrees)
            .shadow(2.dp, RoundedCornerShape(2.dp))
            .background(backgroundColor, RoundedCornerShape(2.dp))
            .padding(12.dp)
    ) {
        // Washi tape strip at the top center
        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(36.dp)
                .height(10.dp)
        ) {
            drawRoundRect(
                color = tapeColor,
                topLeft = Offset(0f, -4.dp.toPx()),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }

        Column(modifier = Modifier.padding(top = 4.dp)) {
            content()
        }
    }
}

/**
 * Hand-drawn Circled Number (①, ②, ③).
 */
@Composable
fun NotebookCircledNumber(
    number: Int,
    modifier: Modifier = Modifier,
    color: Color = JournalInk,
    circleColor: Color = HighlighterBlue.copy(alpha = 0.8f),
    size: Dp = 22.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val s = this.size.width
            val strokeW = 1.3.dp.toPx()
            // Slightly imperfect organic hand circle
            val path = Path().apply {
                moveTo(s * 0.5f, strokeW)
                cubicTo(s * 0.95f, strokeW, s - strokeW, s * 0.8f, s * 0.52f, s - strokeW)
                cubicTo(strokeW * 1.5f, s - strokeW * 0.5f, strokeW, s * 0.3f, s * 0.5f, strokeW)
            }
            drawPath(
                path = path,
                color = circleColor,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }
        Text(
            text = number.toString(),
            fontFamily = PatrickHandFamily,
            fontSize = 13.sp,
            color = color
        )
    }
}

/**
 * Hand-drawn Green Checkmark (✓).
 */
@Composable
fun NotebookCheckmark(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF388E3C),
    size: Dp = 18.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 1.8.dp.toPx()

        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.82f)
            lineTo(w * 0.88f, h * 0.18f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
    }
}

/**
 * Hand-drawn Coral Cross (✕).
 */
@Composable
fun NotebookCross(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFD32F2F),
    size: Dp = 18.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = 1.8.dp.toPx()

        drawLine(
            color = color,
            start = Offset(w * 0.22f, h * 0.22f),
            end = Offset(w * 0.78f, h * 0.78f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.78f, h * 0.22f),
            end = Offset(w * 0.22f, h * 0.78f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Hand-drawn 4-point Sparkle / Star doodle.
 */
@Composable
fun NotebookSparkle(
    modifier: Modifier = Modifier,
    color: Color = HighlighterYellow,
    size: Dp = 16.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            quadraticTo(w * 0.5f, h * 0.5f, w, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, h)
            quadraticTo(w * 0.5f, h * 0.5f, 0f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.5f, w * 0.5f, 0f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

/**
 * Hand-drawn Little Heart doodle.
 */
@Composable
fun NotebookHeart(
    modifier: Modifier = Modifier,
    color: Color = HighlighterPink,
    size: Dp = 16.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.8f)
            cubicTo(w * 0.1f, h * 0.55f, 0f, h * 0.3f, w * 0.25f, h * 0.1f)
            cubicTo(w * 0.45f, 0f, w * 0.5f, h * 0.25f, w * 0.5f, h * 0.25f)
            cubicTo(w * 0.5f, h * 0.25f, w * 0.55f, 0f, w * 0.75f, h * 0.1f)
            cubicTo(w, h * 0.3f, w * 0.9f, h * 0.55f, w * 0.5f, h * 0.8f)
            close()
        }
        drawPath(path = path, color = color)
    }
}
