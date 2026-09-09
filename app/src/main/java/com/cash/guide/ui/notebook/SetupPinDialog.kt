package com.cash.guide.ui.notebook

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cash.guide.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SetupPinDialog(
    onPinConfirmed: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(1) } // 1: Enter, 2: Confirm
    var firstPin by remember { mutableStateOf("") }
    var currentPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current

    fun triggerShake() {
        coroutineScope.launch {
            try {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {}
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 350
                    0f at 0
                    (-14f) at 50
                    14f at 100
                    (-10f) at 150
                    10f at 200
                    (-6f) at 250
                    6f at 300
                    0f at 350
                }
            )
        }
    }

    fun onDigit(d: String) {
        if (currentPin.length < 4) {
            val newPin = currentPin + d
            currentPin = newPin
            errorMessage = null
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}

            if (newPin.length == 4) {
                if (step == 1) {
                    firstPin = newPin
                    currentPin = ""
                    step = 2
                } else {
                    if (newPin == firstPin) {
                        onPinConfirmed(newPin)
                    } else {
                        errorMessage = context.getString(R.string.pin_mismatch_error)
                        triggerShake()
                        currentPin = ""
                        step = 1
                        firstPin = ""
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (currentPin.isNotEmpty()) {
            currentPin = currentPin.dropLast(1)
            errorMessage = null
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(JournalPaper)
                    .border(1.dp, JournalRule.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .drawBehind {
                        val stepPx = 29.dp.toPx()
                        var y = stepPx
                        while (y < size.height) {
                            drawLine(
                                color = JournalRule.copy(alpha = 0.35f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 0.6.dp.toPx()
                            )
                            y += stepPx
                        }
                    }
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Lock Icon and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Lock,
                            contentDescription = null,
                            tint = JournalInk,
                            size = 22.dp
                        )
                        Text(
                            text = if (step == 1)
                                stringResource(R.string.pin_setup_title)
                            else
                                stringResource(R.string.pin_confirm_title),
                            style = TextStyle(
                                fontFamily = JournalHandFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalInk
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            style = TextStyle(
                                fontFamily = JournalHandFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalMutedInk
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (step == 1)
                        stringResource(R.string.pin_setup_hint)
                    else
                        stringResource(R.string.pin_confirm_hint),
                    style = TextStyle(
                        fontFamily = JournalHandFamily,
                        fontSize = 14.sp,
                        color = JournalMutedInk,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        style = TextStyle(
                            fontFamily = JournalHandFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalCreditRed,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4 PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < currentPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) HighlighterPink.copy(alpha = 0.90f) else JournalPaper
                                )
                                .border(
                                    width = 1.4.dp,
                                    color = if (isFilled) JournalInk else JournalRule,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Mini Notebook Numeric Keypad (1..9, 0, ⌫)
                val keypadRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "⌫")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    keypadRows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            row.forEach { key ->
                                if (key.isEmpty()) {
                                    Spacer(modifier = Modifier.size(52.dp))
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(HighlighterYellow.copy(alpha = 0.20f))
                                            .border(1.dp, JournalRule.copy(alpha = 0.50f), CircleShape)
                                            .clickable(
                                                role = Role.Button,
                                                onClick = {
                                                    if (key == "⌫") onBackspace() else onDigit(key)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            style = TextStyle(
                                                fontFamily = JournalHandFamily,
                                                fontSize = if (key == "⌫") 18.sp else 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JournalInk
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
