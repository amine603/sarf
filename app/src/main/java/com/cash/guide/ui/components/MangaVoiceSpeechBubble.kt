package com.cash.guide.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.resolveJournalFont

/**
 * Comic / Manga speech bubble shape with a pointer arrow at the bottom right.
 */
class MangaSpeechBubbleShape(
    private val cornerRadius: Dp = 12.dp,
    private val arrowWidth: Dp = 14.dp,
    private val arrowHeight: Dp = 8.dp,
    private val arrowCenterOffset: Dp = 26.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cr = with(density) { cornerRadius.toPx() }
        val aw = with(density) { arrowWidth.toPx() }
        val ah = with(density) { arrowHeight.toPx() }
        val centerOffset = with(density) { arrowCenterOffset.toPx() }

        val bodyBottom = size.height - ah
        val arrowTipX = if (layoutDirection == LayoutDirection.Rtl) {
            centerOffset.coerceIn(cr + aw / 2f, size.width - cr - aw / 2f)
        } else {
            (size.width - centerOffset).coerceIn(cr + aw / 2f, size.width - cr - aw / 2f)
        }
        val arrowLeft = arrowTipX - aw / 2f
        val arrowRight = arrowTipX + aw / 2f

        val path = Path().apply {
            moveTo(cr, 0f)
            lineTo(size.width - cr, 0f)
            arcTo(Rect(size.width - 2 * cr, 0f, size.width, 2 * cr), -90f, 90f, false)
            lineTo(size.width, bodyBottom - cr)
            arcTo(Rect(size.width - 2 * cr, bodyBottom - 2 * cr, size.width, bodyBottom), 0f, 90f, false)
            lineTo(arrowRight, bodyBottom)
            lineTo(arrowTipX, size.height)
            lineTo(arrowLeft, bodyBottom)
            lineTo(cr, bodyBottom)
            arcTo(Rect(0f, bodyBottom - 2 * cr, 2 * cr, bodyBottom), 90f, 90f, false)
            lineTo(0f, cr)
            arcTo(Rect(0f, 0f, 2 * cr, 2 * cr), 180f, 90f, false)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Manga-style live voice transcription bubble.
 * Stretches horizontally above the input bar, with a speech tail pointing down to the mic button.
 */
@Composable
fun MangaVoiceSpeechBubble(
    liveTranscript: String,
    isAnalyzing: Boolean,
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val shape = MangaSpeechBubbleShape(cornerRadius = 12.dp, arrowHeight = 8.dp)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    LaunchedEffect(liveTranscript) {
        if (liveTranscript.isNotBlank()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = shape)
            .clip(shape)
            .background(Color(0xFFFFFDF5))
            .border(1.3.dp, Color(0xFF2C3E50).copy(alpha = 0.85f), shape)
            .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 15.dp) // extra bottom padding for arrow
    ) {
        if (isAnalyzing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 46.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF1B7A4B),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "جاري التحليل واستخراج السلعة بالذكاء الاصطناعي 🪄...",
                    fontFamily = resolveJournalFont("جاري التحليل واستخراج السلعة", true),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B7A4B)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top micro-bar: Pulsing record dot + Header + Cancel 'X'
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إلغاء",
                        tint = JournalMutedInk.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onCancel() }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "(برك على الزر الأحمر ملي تسالي)",
                            fontFamily = resolveJournalFont("برك على الزر ملي تسالي", true),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFD32F2F)
                        )
                        Text(
                            text = "كنسمع ليك دابا... هضر براحتك",
                            fontFamily = resolveJournalFont("كنسمع ليك دابا", true),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935).copy(alpha = pulseAlpha))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Transcription area (compact with scroll)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 28.dp, max = 75.dp)
                ) {
                    if (liveTranscript.isBlank()) {
                        Text(
                            text = "تكلم بالدارجة... كاع داكشي لي كتقولو غادي يبان كيتكتب هنا فالحين ✍️",
                            fontFamily = resolveJournalFont("تكلم بالدارجة", true),
                            fontSize = 13.sp,
                            color = JournalMutedInk.copy(alpha = 0.7f),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                        ) {
                            Text(
                                text = liveTranscript,
                                fontFamily = resolveJournalFont(liveTranscript, true),
                                fontSize = 14.5.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = JournalInk,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
