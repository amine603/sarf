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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.domain.ai.AiOutputScript
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.isArabicScript
import com.cash.guide.ui.notebook.resolveJournalFont
import java.util.Locale

/**
 * Comic / Manga speech bubble shape with a pointer arrow at the bottom.
 * If [forceRightArrow] is true, the arrow is positioned at the right (width - 26dp) regardless of RTL.
 */
class MangaSpeechBubbleShape(
    private val cornerRadius: Dp = 12.dp,
    private val arrowWidth: Dp = 14.dp,
    private val arrowHeight: Dp = 8.dp,
    private val arrowCenterOffset: Dp = 26.dp,
    private val forceRightArrow: Boolean = false
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
        val isRight = forceRightArrow || layoutDirection == LayoutDirection.Ltr
        val arrowTipX = if (isRight) {
            (size.width - centerOffset).coerceIn(cr + aw / 2f, size.width - cr - aw / 2f)
        } else {
            centerOffset.coerceIn(cr + aw / 2f, size.width - cr - aw / 2f)
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
 * Stretches horizontally above the mic button, with a speech tail pointing down to it.
 */
@Composable
fun MangaVoiceSpeechBubble(
    liveTranscript: String,
    isAnalyzing: Boolean,
    currentScript: AiOutputScript = AiOutputScript.ARABIC,
    secondsRemaining: Int = 45,
    onScriptSelected: (AiOutputScript) -> Unit = {},
    forceRightArrow: Boolean = false,
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    val configuration = LocalConfiguration.current
    val currentLocale = configuration.locales.get(0) ?: Locale.getDefault()
    val langCode = currentLocale.language.lowercase()
    val isFrench = langCode == "fr"
    val isEnglish = langCode == "en"
    val isArabic = !isFrench && !isEnglish

    val shape = MangaSpeechBubbleShape(
        cornerRadius = 12.dp,
        arrowHeight = 8.dp,
        forceRightArrow = forceRightArrow
    )

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

    val analyzingText = when {
        isFrench -> "Analyse et extraction par l'IA 🪄..."
        isEnglish -> "Analyzing & extracting with AI 🪄..."
        else -> "جاري التحليل واستخراج البنود بالذكاء الاصطناعي 🪄..."
    }

    val statusTitle = when {
        isFrench -> "À votre écoute... Parlez librement"
        isEnglish -> "Listening now... Speak freely"
        else -> "كنسمع ليك دابا... هضر براحتك"
    }

    val statusSub = when {
        isFrench -> "(Appuyez sur le bouton rouge pour terminer)"
        isEnglish -> "(Tap red button when finished)"
        else -> "(برك على الزر الأحمر ملي تسالي)"
    }

    val scriptLabel = when {
        isFrench -> "Format :"
        isEnglish -> "Format:"
        else -> "شكل الكتابة:"
    }

    val closeDesc = when {
        isFrench -> "Fermer"
        isEnglish -> "Close"
        else -> "إلغاء"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = shape)
            .clip(shape)
            .background(Color(0xFFFFFDF5))
            .border(1.3.dp, Color(0xFF2C3E50).copy(alpha = 0.85f), shape)
            .padding(start = 14.dp, end = 14.dp, top = 7.dp, bottom = 15.dp) // extra bottom padding for arrow
    ) {
        if (isAnalyzing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
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
                    text = analyzingText,
                    fontFamily = resolveJournalFont(analyzingText, isArabic),
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
                    // Status and recording indicator (at logical start)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!isRtl) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE53935).copy(alpha = pulseAlpha))
                            )
                            Text(
                                text = statusTitle,
                                fontFamily = resolveJournalFont(statusTitle, false),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalInk
                            )
                            Text(
                                text = statusSub,
                                fontFamily = resolveJournalFont(statusSub, false),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFFD32F2F)
                            )
                        } else {
                            Text(
                                text = statusSub,
                                fontFamily = resolveJournalFont(statusSub, true),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = statusTitle,
                                fontFamily = resolveJournalFont(statusTitle, true),
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

                    // Right/End area: Countdown timer badge + Close icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val timerMinutes = secondsRemaining / 60
                        val timerSecs = secondsRemaining % 60
                        val timerText = String.format(java.util.Locale.US, "%02d:%02d", timerMinutes, timerSecs)
                        val isUrgent = secondsRemaining <= 10

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isUrgent) Color(0xFFFFEBEE) else Color(0xFFF0F4F8))
                                .border(
                                    1.dp,
                                    if (isUrgent) Color(0xFFE53935).copy(alpha = 0.6f) else Color(0xFFCFD8DC),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "⏱️",
                                    fontSize = 9.5.sp
                                )
                                Text(
                                    text = timerText,
                                    fontSize = 11.sp,
                                    fontWeight = if (isUrgent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isUrgent) Color(0xFFD32F2F) else Color(0xFF455A64)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = closeDesc,
                            tint = JournalMutedInk.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onCancel() }
                        )
                    }
                }

                // Output Script Selector Chips (العربية | العرنسية Franco | Français)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = scriptLabel,
                        fontFamily = resolveJournalFont(scriptLabel, isArabic),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = JournalMutedInk,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    AiOutputScript.entries.forEach { script ->
                        val isSelected = currentScript == script
                        val chipText = when (script) {
                            AiOutputScript.ARABIC -> if (isFrench) "Arabe" else if (isEnglish) "Arabic" else "العربية"
                            AiOutputScript.FRANCO -> if (isFrench) "Franco" else if (isEnglish) "Franco" else "العرنسية"
                            AiOutputScript.FRENCH -> if (isEnglish) "French" else "Français"
                        }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color(0xFF1B7A4B) else Color(0xFFEAE7D8).copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF1B7A4B) else JournalMutedInk.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onScriptSelected(script) }
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = chipText,
                                fontFamily = resolveJournalFont(chipText, isArabicScript(chipText)),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else JournalInk
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Transcription area (compact with scroll)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 28.dp, max = 75.dp)
                ) {
                    if (liveTranscript.isBlank()) {
                        val hint = when {
                            isFrench -> when (currentScript) {
                                AiOutputScript.FRENCH -> "Parlez en français ou en darija... tout s'écrira en français ✍️"
                                AiOutputScript.FRANCO -> "Parlez en darija ou en français... tout s'écrira en franco ✍️"
                                AiOutputScript.ARABIC -> "Parlez en darija ou en français... tout s'écrira en arabe ✍️"
                            }
                            isEnglish -> when (currentScript) {
                                AiOutputScript.FRENCH -> "Speak in French or Darija... result will be in French ✍️"
                                AiOutputScript.FRANCO -> "Speak in Darija or French... result will be in Franco ✍️"
                                AiOutputScript.ARABIC -> "Speak in Darija or English... result will be in Arabic ✍️"
                            }
                            else -> when (currentScript) {
                                AiOutputScript.FRENCH -> "تكلم بالفرنسية أو بالدارجة... غادي يفهمك ويكتبها بالفرنسية ✍️"
                                AiOutputScript.FRANCO -> "تكلم بالدارجة أو بالفرنسية... غادي يفهمك ويكتبها بالعرنسية ✍️"
                                AiOutputScript.ARABIC -> "تكلم بالدارجة أو بالفرنسية... كاع داكشي لي كتقولو كيتكتب هنا فالحين ✍️"
                            }
                        }
                        val isHintArabic = isArabicScript(hint)
                        Text(
                            text = hint,
                            fontFamily = resolveJournalFont(hint, isHintArabic),
                            fontSize = 12.5.sp,
                            color = JournalMutedInk.copy(alpha = 0.7f),
                            textAlign = if (isHintArabic) TextAlign.Right else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val isTranscriptArabic = isArabicScript(liveTranscript)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                        ) {
                            Text(
                                text = liveTranscript,
                                fontFamily = resolveJournalFont(liveTranscript, isTranscriptArabic),
                                fontSize = 14.5.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = JournalInk,
                                textAlign = if (isTranscriptArabic) TextAlign.Right else TextAlign.Left,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
