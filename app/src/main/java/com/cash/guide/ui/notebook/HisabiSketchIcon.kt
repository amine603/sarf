package com.cash.guide.ui.notebook

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

enum class HisabiSymbol {
    Back,
    More,
    Plus,
    Trash,
    ChevronUp,
    ChevronDown,
    Backspace,
    Exclamation,
    Close,
    Check,
    Wallet,
    Page,
    Home,
    Search,
    Calculator,
    Clock,
    Gear,
    Lightbulb,
    Pencil,
    Copy,
    Calendar
}

@Composable
fun HisabiSketchIcon(
    symbol: HisabiSymbol,
    contentDescription: String?,
    tint: Color = Ink,
    modifier: Modifier = Modifier,
    size: Dp = HisabiMetrics.IconSize
) {
    val semanticsModifier = if (contentDescription == null) modifier else {
        modifier.semantics { this.contentDescription = contentDescription }
    }
    Canvas(modifier = semanticsModifier.size(size)) {
        val scale = min(this.size.width, this.size.height) / 24f
        fun u(value: Float) = value * scale
        fun point(x: Float, y: Float) = Offset(u(x), u(y))
        val pen = Stroke(u(1.45f), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val fine = Stroke(u(1.15f), cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (symbol) {
            HisabiSymbol.Back -> {
                val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl
                val xStart = if (isRtl) 7f else 16.5f
                val xPoint = if (isRtl) 16.5f else 7f
                drawLine(tint, point(xStart, 4.5f), point(xPoint, 12f), u(1.45f), StrokeCap.Round)
                drawLine(tint, point(xPoint, 12f), point(xStart, 19.5f), u(1.45f), StrokeCap.Round)
            }
            HisabiSymbol.More -> {
                drawCircle(tint, u(1.15f), point(12f, 5f))
                drawCircle(tint, u(1.15f), point(12f, 12f))
                drawCircle(tint, u(1.15f), point(12f, 19f))
            }
            HisabiSymbol.Plus -> {
                drawLine(tint, point(12f, 4.5f), point(12f, 19.5f), u(1.45f), StrokeCap.Round)
                drawLine(tint, point(4.5f, 12f), point(19.5f, 12f), u(1.45f), StrokeCap.Round)
            }
            HisabiSymbol.Trash -> {
                // Bin body
                val bin = Path().apply {
                    moveTo(u(6f), u(8.5f))
                    lineTo(u(7.2f), u(20f))
                    lineTo(u(16.8f), u(20f))
                    lineTo(u(18f), u(8.5f))
                }
                drawPath(bin, tint, style = fine)
                // Lid line
                drawLine(tint, point(4.5f, 8.5f), point(19.5f, 8.5f), u(1.35f), StrokeCap.Round)
                // Lid handle
                val lid = Path().apply {
                    moveTo(u(9f), u(8.5f))
                    lineTo(u(9f), u(5f))
                    lineTo(u(15f), u(5f))
                    lineTo(u(15f), u(8.5f))
                }
                drawPath(lid, tint, style = fine)
                // Inner vertical slits
                drawLine(tint, point(10f, 11.5f), point(10f, 17f), u(1.1f), StrokeCap.Round)
                drawLine(tint, point(14f, 11.5f), point(14f, 17f), u(1.1f), StrokeCap.Round)
            }
            HisabiSymbol.ChevronUp -> {
                val chevron = Path().apply {
                    moveTo(u(4.5f), u(15f))
                    lineTo(u(12f), u(8.5f))
                    lineTo(u(19.5f), u(15f))
                }
                drawPath(chevron, tint, style = pen)
            }
            HisabiSymbol.ChevronDown -> {
                val chevron = Path().apply {
                    moveTo(u(4.5f), u(9f))
                    lineTo(u(12f), u(15.5f))
                    lineTo(u(19.5f), u(9f))
                }
                drawPath(chevron, tint, style = pen)
            }
            HisabiSymbol.Backspace -> {
                val tag = Path().apply {
                    moveTo(u(3.8f), u(12f))
                    lineTo(u(8.8f), u(5.5f))
                    lineTo(u(20.5f), u(5.5f))
                    lineTo(u(20.5f), u(18.5f))
                    lineTo(u(8.8f), u(18.5f))
                    close()
                }
                drawPath(tag, tint, style = pen)
                // Cross inside
                drawLine(tint, point(11.5f, 9f), point(17.5f, 15f), u(1.35f), StrokeCap.Round)
                drawLine(tint, point(17.5f, 9f), point(11.5f, 15f), u(1.35f), StrokeCap.Round)
            }
            HisabiSymbol.Exclamation -> {
                drawLine(tint, point(12f, 4.5f), point(12f, 14.5f), u(1.6f), StrokeCap.Round)
                drawCircle(tint, u(1.15f), point(12f, 18.5f))
            }
            HisabiSymbol.Close -> {
                drawLine(tint, point(5.5f, 5.5f), point(18.5f, 18.5f), u(1.45f), StrokeCap.Round)
                drawLine(tint, point(18.5f, 5.5f), point(5.5f, 18.5f), u(1.45f), StrokeCap.Round)
            }
            HisabiSymbol.Check -> {
                drawLine(tint, point(4f, 13f), point(9f, 18f), u(1.5f), StrokeCap.Round)
                drawLine(tint, point(9f, 18f), point(20f, 5f), u(1.5f), StrokeCap.Round)
            }
            HisabiSymbol.Wallet -> {
                drawRoundRect(tint, topLeft = point(3.5f, 6f), size = Size(u(17f), u(13f)), cornerRadius = CornerRadius(u(2.2f)), style = pen)
                drawRoundRect(tint, topLeft = point(12.8f, 10f), size = Size(u(8.2f), u(5.5f)), cornerRadius = CornerRadius(u(1.5f)), style = pen)
                drawCircle(tint, u(.9f), point(16.2f, 12.8f))
                drawLine(tint, point(5f, 6f), point(16f, 4f), u(1.2f), StrokeCap.Round)
            }
            HisabiSymbol.Page -> {
                val page = Path().apply {
                    moveTo(u(5f), u(3f)); lineTo(u(15f), u(3f)); lineTo(u(19f), u(7f))
                    lineTo(u(19f), u(21f)); lineTo(u(5f), u(21f)); close()
                }
                drawPath(page, tint, style = pen)
                drawLine(tint, point(15f, 3f), point(15f, 7f), u(1.2f), StrokeCap.Round)
                drawLine(tint, point(15f, 7f), point(19f, 7f), u(1.2f), StrokeCap.Round)
                drawLine(tint, point(8f, 11f), point(16f, 11f), u(1.1f), StrokeCap.Round)
                drawLine(tint, point(8f, 15f), point(16f, 15f), u(1.1f), StrokeCap.Round)
                drawLine(tint, point(8f, 19f), point(13f, 19f), u(1.1f), StrokeCap.Round)
            }
            HisabiSymbol.Home -> {
                val house = Path().apply {
                    moveTo(u(3.5f), u(11f))
                    lineTo(u(12f), u(4f))
                    lineTo(u(20.5f), u(11f))
                    lineTo(u(19f), u(11f))
                    lineTo(u(19f), u(20f))
                    lineTo(u(14.5f), u(20f))
                    lineTo(u(14.5f), u(14f))
                    lineTo(u(9.5f), u(14f))
                    lineTo(u(9.5f), u(20f))
                    lineTo(u(5f), u(20f))
                    lineTo(u(5f), u(11f))
                    close()
                }
                drawPath(house, tint, style = pen)
            }
            HisabiSymbol.Search -> {
                drawCircle(tint, u(6.5f), point(10f, 10f), style = pen)
                drawLine(tint, point(14.8f, 14.8f), point(20f, 20f), u(1.55f), StrokeCap.Round)
            }
            HisabiSymbol.Calculator -> {
                drawRoundRect(
                    tint,
                    topLeft = point(4.5f, 3f),
                    size = Size(u(15f), u(18f)),
                    cornerRadius = CornerRadius(u(2.5f)),
                    style = pen
                )
                drawRoundRect(
                    tint,
                    topLeft = point(7f, 5.5f),
                    size = Size(u(10f), u(3.5f)),
                    cornerRadius = CornerRadius(u(1f)),
                    style = fine
                )
                drawCircle(tint, u(0.75f), point(8f, 12f))
                drawCircle(tint, u(0.75f), point(12f, 12f))
                drawCircle(tint, u(0.75f), point(16f, 12f))
                drawCircle(tint, u(0.75f), point(8f, 15.5f))
                drawCircle(tint, u(0.75f), point(12f, 15.5f))
                drawCircle(tint, u(0.75f), point(16f, 15.5f))
            }
            HisabiSymbol.Clock -> {
                drawCircle(tint, u(8.5f), point(12f, 12f), style = pen)
                drawLine(tint, point(12f, 12f), point(12f, 7f), u(1.5f), StrokeCap.Round)
                drawLine(tint, point(12f, 12f), point(15.5f, 12f), u(1.4f), StrokeCap.Round)
                drawCircle(tint, u(1.1f), point(12f, 12f))
            }
            HisabiSymbol.Gear -> {
                val outerRadius = 8f
                val innerRadius = 3.5f
                drawCircle(tint, u(innerRadius), point(12f, 12f), style = pen)
                drawCircle(tint, u(outerRadius), point(12f, 12f), style = fine)
                for (i in 0 until 6) {
                    val angle = (i * 60.0) * Math.PI / 180.0
                    val cx = 12f + (outerRadius * kotlin.math.cos(angle)).toFloat()
                    val cy = 12f + (outerRadius * kotlin.math.sin(angle)).toFloat()
                    drawCircle(tint, u(1.4f), point(cx, cy))
                }
            }
            HisabiSymbol.Lightbulb -> {
                val bulb = Path().apply {
                    moveTo(u(8.5f), u(13.5f))
                    cubicTo(u(5.5f), u(10.5f), u(5.5f), u(6f), u(12f), u(4.5f))
                    cubicTo(u(18.5f), u(6f), u(18.5f), u(10.5f), u(15.5f), u(13.5f))
                    lineTo(u(14.5f), u(16.5f))
                    lineTo(u(9.5f), u(16.5f))
                    close()
                }
                drawPath(bulb, tint, style = pen)
                drawLine(tint, point(10f, 18.5f), point(14f, 18.5f), u(1.4f), StrokeCap.Round)
                // Small filament rays
                drawLine(tint, point(3.5f, 10f), point(5f, 10f), u(1.2f), StrokeCap.Round)
                drawLine(tint, point(19f, 10f), point(20.5f, 10f), u(1.2f), StrokeCap.Round)
                drawLine(tint, point(12f, 2f), point(12f, 3.5f), u(1.2f), StrokeCap.Round)
            }
            HisabiSymbol.Pencil -> {
                val pencil = Path().apply {
                    moveTo(u(5f), u(19f))
                    lineTo(u(8f), u(19f))
                    lineTo(u(19f), u(8f))
                    lineTo(u(16f), u(5f))
                    lineTo(u(5f), u(16f))
                    close()
                }
                drawPath(pencil, tint, style = fine)
                drawLine(tint, point(14f, 7f), point(17f, 10f), u(1.1f), StrokeCap.Round)
                drawLine(tint, point(4f, 20f), point(5f, 19f), u(1.3f), StrokeCap.Round)
            }
            HisabiSymbol.Copy -> {
                val back = Path().apply {
                    moveTo(u(5f), u(15f))
                    lineTo(u(5f), u(5f))
                    lineTo(u(15f), u(5f))
                }
                drawPath(back, tint, style = pen)
                drawRoundRect(
                    tint,
                    topLeft = point(8f, 8f),
                    size = Size(u(11f), u(11f)),
                    cornerRadius = CornerRadius(u(2f)),
                    style = pen
                )
            }
            HisabiSymbol.Calendar -> {
                // Calendar body outline
                drawRoundRect(
                    tint,
                    topLeft = point(4f, 5.5f),
                    size = Size(u(16f), u(14.5f)),
                    cornerRadius = CornerRadius(u(2.5f)),
                    style = pen
                )
                // Header divider line
                drawLine(tint, point(4f, 9.5f), point(20f, 9.5f), u(1.2f))
                // Rings / binder loops
                drawLine(tint, point(8f, 3.5f), point(8f, 6.5f), u(1.5f), StrokeCap.Round)
                drawLine(tint, point(16f, 3.5f), point(16f, 6.5f), u(1.5f), StrokeCap.Round)
                // Small day dots
                drawCircle(tint, u(0.85f), point(8f, 13f))
                drawCircle(tint, u(0.85f), point(12f, 13f))
                drawCircle(tint, u(0.85f), point(16f, 13f))
                drawCircle(tint, u(0.85f), point(8f, 16.5f))
                drawCircle(tint, u(0.85f), point(12f, 16.5f))
            }
        }
    }
}
