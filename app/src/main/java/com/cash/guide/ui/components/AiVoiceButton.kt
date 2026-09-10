package com.cash.guide.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class AiVoiceButtonState {
    IDLE,
    RECORDING,
    ANALYZING
}

@Composable
fun AiVoiceButton(
    state: AiVoiceButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_voice_button")

    // Radar pulse scale when recording
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Equalizer animated heights for the 3 bars inside the red circle
    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 7f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 9f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(52.dp)
    ) {
        // Outer pulsing ring when recording
        if (state == AiVoiceButtonState.RECORDING) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935).copy(alpha = 0.25f))
            )
        }

        // Main button
        Surface(
            shape = CircleShape,
            color = when (state) {
                AiVoiceButtonState.RECORDING -> Color(0xFFE53935)
                AiVoiceButtonState.ANALYZING -> Color(0xFF1B7A4B)
                AiVoiceButtonState.IDLE -> Color(0xFF1B7A4B)
            },
            shadowElevation = if (state == AiVoiceButtonState.RECORDING) 4.dp else 2.dp,
            modifier = Modifier
                .size(44.dp)
                .clickable(enabled = state != AiVoiceButtonState.ANALYZING) { onClick() }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp)
            ) {
                when (state) {
                    AiVoiceButtonState.RECORDING -> {
                        // 3 animated vertical white audio waveform bars (Exact match to reference image!)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.2.dp)
                                    .height(bar1Height.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White)
                            )
                            Box(
                                modifier = Modifier
                                    .width(3.2.dp)
                                    .height(bar2Height.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White)
                            )
                            Box(
                                modifier = Modifier
                                    .width(3.2.dp)
                                    .height(bar3Height.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White)
                            )
                        }
                    }

                    AiVoiceButtonState.ANALYZING -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    AiVoiceButtonState.IDLE -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "تسجيل بالذكاء الاصطناعي",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
