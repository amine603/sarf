package com.cash.guide.ui.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.resolveJournalFont

/**
 * Live Voice Recording Window showing real-time transcription as the user speaks.
 * Includes animated equalizer waves and an explicit "Finish & Analyze" action.
 */
@Composable
fun AiLiveRecordingSheet(
    liveTranscript: String,
    isAnalyzing: Boolean,
    onFinishRecording: () -> Unit,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(liveTranscript) {
        if (liveTranscript.isNotBlank()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = JournalPaper),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إلغاء",
                            tint = JournalMutedInk
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isAnalyzing) "جاري التحليل الذكي..." else "كنسمع ليك دابا...",
                            fontFamily = resolveJournalFont("كنسمع ليك دابا", true),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = JournalInk
                        )
                        Text(
                            text = "🎙️",
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Transcription Box (Simulates lined paper / writing area)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 240.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFFDF5))
                        .border(1.2.dp, JournalRule.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    if (liveTranscript.isBlank()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "تكلم براحتك بالدارجة...",
                                fontFamily = resolveJournalFont("تكلم براحتك بالدارجة", true),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B7A4B),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "كاع داكشي لي غادي تقولو غادي يبان كيتكتب هنا فالحين ✍️",
                                fontFamily = resolveJournalFont("كاع داكشي لي غادي تقولو", true),
                                fontSize = 13.5.sp,
                                color = JournalMutedInk,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                        ) {
                            Text(
                                text = liveTranscript,
                                fontFamily = resolveJournalFont(liveTranscript, true),
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Medium,
                                color = JournalInk,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Controls Area
                if (isAnalyzing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF1B7A4B),
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "جاري استخراج السلعة بالذكاء الاصطناعي 🪄...",
                            fontFamily = resolveJournalFont("جاري استخراج السلعة", true),
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1B7A4B)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // In-place Equalizer Button (Red recording state with waveform bars)
                        AiVoiceButton(
                            state = AiVoiceButtonState.RECORDING,
                            onClick = onFinishRecording
                        )

                        // Big Prominent Action Button: "سالي وتحليل بالذكاء الاصطناعي"
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1B7A4B),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clickable { onFinishRecording() }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "سالي وتحليل بالذكاء الاصطناعي ✨",
                                        fontFamily = resolveJournalFont("سالي وتحليل بالذكاء الاصطناعي", true),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
