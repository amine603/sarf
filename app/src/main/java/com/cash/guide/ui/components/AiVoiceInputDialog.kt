package com.cash.guide.ui.components

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.cash.guide.domain.ads.AdMobManager
import com.cash.guide.domain.ads.AiCreditManager
import com.cash.guide.domain.ai.CalculationAiResult
import com.cash.guide.domain.ai.ChecklistAiResult
import com.cash.guide.domain.ai.GeminiDarijaService
import com.cash.guide.domain.speech.SpeechRecognitionState
import com.cash.guide.domain.speech.SpeechRecognizerHelper
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import kotlinx.coroutines.launch

enum class AiVoiceInputTarget {
    CHECKLIST,
    CALCULATION
}

@Composable
fun AiVoiceInputDialog(
    target: AiVoiceInputTarget,
    onDismiss: () -> Unit,
    onChecklistResult: (ChecklistAiResult) -> Unit = {},
    onCalculationResult: (CalculationAiResult) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val creditManager = remember { AiCreditManager.getInstance(context) }
    val adMobManager = remember { AdMobManager.getInstance(context) }
    val credits by creditManager.credits.collectAsState()

    var isProcessingGemini by remember { mutableStateOf(false) }
    var geminiErrorMessage by remember { mutableStateOf<String?>(null) }

    val speechHelper = remember {
        SpeechRecognizerHelper(context).apply {
            onSpeechResult = { text ->
                if (text.isNotBlank()) {
                    isProcessingGemini = true
                    geminiErrorMessage = null
                    scope.launch {
                        try {
                            if (target == AiVoiceInputTarget.CHECKLIST) {
                                val result = GeminiDarijaService.parseChecklistFromDarija(text)
                                if (result != null && result.items.isNotEmpty()) {
                                    creditManager.consumeCredit()
                                    onChecklistResult(result)
                                    onDismiss()
                                } else {
                                    geminiErrorMessage = "تعذر استخراج العناصر، يرجى التحدث بوضوح أكثر"
                                }
                            } else {
                                val result = GeminiDarijaService.parseCalculationFromDarija(text)
                                if (result != null && result.entries.isNotEmpty()) {
                                    creditManager.consumeCredit()
                                    onCalculationResult(result)
                                    onDismiss()
                                } else {
                                    geminiErrorMessage = "تعذر استخراج الحسابات، يرجى ذكر المبالغ بوضوح"
                                }
                            }
                        } catch (e: Exception) {
                            geminiErrorMessage = "حدث خطأ في الاتصال بالذكاء الاصطناعي"
                        } finally {
                            isProcessingGemini = false
                        }
                    }
                }
            }
        }
    }

    val speechState by speechHelper.state.collectAsState()
    val partialText by speechHelper.partialText.collectAsState()
    val speechError by speechHelper.errorMessage.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted && credits > 0) {
            speechHelper.startListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechHelper.stopListening()
        }
    }

    // Mic Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = JournalPaper),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Title & Close & Credits Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Credits Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (credits > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (credits > 0) Color(0xFF81C784) else Color(0xFFE57373)
                        )
                    ) {
                        Text(
                            text = if (credits > 0) "⚡ $credits محاولات متبقية" else "⚡ 0 محاولات",
                            color = if (credits > 0) Color(0xFF1B5E20) else Color(0xFFC62828),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = JournalMutedInk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (target == AiVoiceInputTarget.CHECKLIST) "إملاء قائمة التسوق بالذكاء الاصطناعي 🎙️" else "إملاء الحسابات بالذكاء الاصطناعي 🎙️",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                val exampleText = if (target == AiVoiceInputTarget.CHECKLIST) {
                    "مثال: \"خاصني 2 كيلو مطيشة، وبكية أتاي السبع، و 3 خبزات وبيض\""
                } else {
                    "مثال: \"قيد 150 درهم سلعة، و 500 ريال كرا، و 40 درهم ترانسبور\""
                }
                Text(
                    text = exampleText,
                    fontSize = 12.5.sp,
                    color = JournalMutedInk,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // If user has 0 credits -> Show Rewarded Ad CTA
                if (credits <= 0) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF8E1),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB300))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "سلاو ليك المحاولات اليومية (5/5) ⏳",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFFF57F17),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "تفرج فإشهار فيديو قصير (15-30 ثانية) وربح 5 محاولات إضافية فوراً!",
                                fontSize = 13.sp,
                                color = JournalInk,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF1B7A4B),
                                modifier = Modifier
                                    .clickable {
                                        if (activity != null) {
                                            adMobManager.showRewardedAd(
                                                activity = activity,
                                                onRewardEarned = {
                                                    creditManager.addRewardCredits(5)
                                                }
                                            )
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مشاهدة إعلان وربح 5 محاولات 🎁",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Big Microphone button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(100.dp)
                    ) {
                        val isListening = speechState == SpeechRecognitionState.LISTENING

                        if (isListening) {
                            Box(
                                modifier = Modifier
                                    .size(95.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(Color(0x331B7A4B))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(if (isListening) Color(0xFFD32F2F) else Color(0xFF1B7A4B))
                                .clickable {
                                    if (!hasAudioPermission) {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        if (isListening) {
                                            speechHelper.stopListening()
                                        } else {
                                            speechHelper.startListening()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isProcessingGemini) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(32.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Microphone",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Text
                    val statusMessage = when {
                        isProcessingGemini -> "جاري التحليل بالذكاء الاصطناعي (Gemini)... 🤖"
                        speechState == SpeechRecognitionState.LISTENING -> "كنسمع ليك دابا... تكلم بالدارجة 🎙️"
                        speechState == SpeechRecognitionState.PROCESSING -> "جاري معالجة الصوت..."
                        else -> "اضغط على الميكروفون وتحدث بالدارجة"
                    }

                    Text(
                        text = statusMessage,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (speechState == SpeechRecognitionState.LISTENING) Color(0xFFD32F2F) else JournalInk,
                        textAlign = TextAlign.Center
                    )

                    // Spoken text display in ruled ink style
                    if (partialText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF48FB1).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF48FB1).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "💬 \"$partialText\"",
                                fontSize = 14.sp,
                                color = JournalInk,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Error display if any
                    val err = geminiErrorMessage ?: speechError
                    if (err != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "⚠️ $err",
                            fontSize = 12.5.sp,
                            color = Color(0xFFC62828),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
