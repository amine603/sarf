package com.cash.guide.ui.components

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.cash.guide.domain.ads.AdMobManager
import com.cash.guide.domain.ads.AiCreditManager
import com.cash.guide.domain.ai.AiVoiceOnboardingManager
import com.cash.guide.domain.ai.CalculationAiResult
import com.cash.guide.domain.ai.ChecklistAiResult
import com.cash.guide.domain.ai.GeminiDarijaService
import com.cash.guide.domain.speech.SpeechRecognitionState
import com.cash.guide.domain.speech.SpeechRecognizerHelper
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import kotlinx.coroutines.launch

@Composable
fun AiVoiceAssistantButton(
    target: AiVoiceInputTarget,
    onChecklistResult: (ChecklistAiResult) -> Unit = {},
    onCalculationResult: (CalculationAiResult) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val creditManager = remember { AiCreditManager.getInstance(context) }
    val adMobManager = remember { AdMobManager.getInstance(context) }
    val onboardingManager = remember { AiVoiceOnboardingManager.getInstance(context) }

    val credits by creditManager.credits.collectAsState()
    val hasSeenOnboarding by onboardingManager.hasSeenOnboarding.collectAsState()

    var showOnboardingDialog by remember { mutableStateOf(false) }
    var showRewardedAdDialog by remember { mutableStateOf(false) }

    var buttonState by remember { mutableStateOf(AiVoiceButtonState.IDLE) }
    var recordedTranscript by remember { mutableStateOf("") }
    var pendingChecklistResult by remember { mutableStateOf<ChecklistAiResult?>(null) }
    var pendingCalculationResult by remember { mutableStateOf<CalculationAiResult?>(null) }

    val speechHelper = remember {
        SpeechRecognizerHelper(context).apply {
            onSpeechResult = { text ->
                if (text.isNotBlank()) {
                    recordedTranscript = text
                    buttonState = AiVoiceButtonState.ANALYZING
                    scope.launch {
                        try {
                            if (target == AiVoiceInputTarget.CHECKLIST) {
                                val result = GeminiDarijaService.parseChecklistFromDarija(text)
                                if (result != null && result.items.isNotEmpty()) {
                                    pendingChecklistResult = result
                                } else {
                                    Toast.makeText(context, "تعذر استخراج العناصر، عاود جرب وتحدث بوضوح", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val result = GeminiDarijaService.parseCalculationFromDarija(text)
                                if (result != null && result.entries.isNotEmpty()) {
                                    pendingCalculationResult = result
                                } else {
                                    Toast.makeText(context, "تعذر استخراج الحسابات، عاود جرب وتحدث بوضوح", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "حدث خطأ في الاتصال بالذكاء الاصطناعي", Toast.LENGTH_SHORT).show()
                        } finally {
                            buttonState = AiVoiceButtonState.IDLE
                        }
                    }
                } else {
                    buttonState = AiVoiceButtonState.IDLE
                }
            }
        }
    }

    val partialText by speechHelper.partialText.collectAsState()

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
            buttonState = AiVoiceButtonState.RECORDING
            speechHelper.startListening()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechHelper.stopListening()
        }
    }

    fun handleMicClick() {
        when (buttonState) {
            AiVoiceButtonState.IDLE -> {
                if (!hasSeenOnboarding) {
                    showOnboardingDialog = true
                    return
                }
                if (credits <= 0) {
                    showRewardedAdDialog = true
                    return
                }
                if (!hasAudioPermission) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    return
                }
                buttonState = AiVoiceButtonState.RECORDING
                speechHelper.startListening()
            }

            AiVoiceButtonState.RECORDING -> {
                // User clicked to stop and analyze!
                buttonState = AiVoiceButtonState.ANALYZING
                speechHelper.stopAndDeliver()
            }

            AiVoiceButtonState.ANALYZING -> {
                // Already analyzing, do nothing
            }
        }
    }

    AiVoiceButton(
        state = buttonState,
        onClick = { handleMicClick() },
        modifier = modifier
    )

    // Live Voice Recording Window (Real-time transcription & equalizer)
    if (buttonState == AiVoiceButtonState.RECORDING || buttonState == AiVoiceButtonState.ANALYZING) {
        AiLiveRecordingSheet(
            liveTranscript = partialText,
            isAnalyzing = buttonState == AiVoiceButtonState.ANALYZING,
            onFinishRecording = {
                if (buttonState == AiVoiceButtonState.RECORDING) {
                    buttonState = AiVoiceButtonState.ANALYZING
                    speechHelper.stopAndDeliver()
                }
            },
            onCancel = {
                buttonState = AiVoiceButtonState.IDLE
                speechHelper.stopListening()
            }
        )
    }

    // 1. One-Time Educational Onboarding Dialog
    if (showOnboardingDialog) {
        AiVoiceOnboardingDialog(
            onDismiss = {
                onboardingManager.markSeen()
                showOnboardingDialog = false
                if (credits > 0) {
                    if (!hasAudioPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        buttonState = AiVoiceButtonState.RECORDING
                        speechHelper.startListening()
                    }
                } else {
                    showRewardedAdDialog = true
                }
            }
        )
    }

    // 2. Rewarded Ad Dialog (when credits == 0)
    if (showRewardedAdDialog) {
        Dialog(onDismissRequest = { showRewardedAdDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showRewardedAdDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = JournalMutedInk)
                        }
                    }

                    Text(
                        text = "سلاو ليك المحاولات اليومية (5/5) ⏳",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFF57F17),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "تفرج فإشهار فيديو قصير (15-30 ثانية) وربح 5 محاولات إضافية فوراً لتسجيل السلعة والحسابات!",
                        fontSize = 13.5.sp,
                        color = JournalInk,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1B7A4B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (activity != null) {
                                    if (!adMobManager.isReady.value) {
                                        Toast.makeText(context, "جاري تجهيز الإعلان، يرجى المحاولة بعد لحظات...", Toast.LENGTH_SHORT).show()
                                        adMobManager.loadRewardedAd()
                                    } else {
                                        adMobManager.showRewardedAd(
                                            activity = activity,
                                            onRewardEarned = {
                                                creditManager.addRewardCredits(5)
                                                showRewardedAdDialog = false
                                                buttonState = AiVoiceButtonState.RECORDING
                                                speechHelper.startListening()
                                            }
                                        )
                                    }
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مشاهدة إعلان وربح 5 محاولات 🎁",
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

    // 3. Review & Confirmation Dialog for Checklist
    val checklistRes = pendingChecklistResult
    if (checklistRes != null) {
        AiChecklistReviewDialog(
            originalSpeech = recordedTranscript,
            initialItems = checklistRes.items,
            onDismiss = { pendingChecklistResult = null },
            onConfirm = { confirmedItems ->
                creditManager.consumeCredit()
                onChecklistResult(checklistRes.copy(items = confirmedItems))
                pendingChecklistResult = null
            }
        )
    }

    // 4. Review & Confirmation Dialog for Calculations
    val calcRes = pendingCalculationResult
    if (calcRes != null) {
        AiCalculationReviewDialog(
            originalSpeech = recordedTranscript,
            initialEntries = calcRes.entries,
            onDismiss = { pendingCalculationResult = null },
            onConfirm = { confirmedEntries ->
                creditManager.consumeCredit()
                onCalculationResult(calcRes.copy(entries = confirmedEntries))
                pendingCalculationResult = null
            }
        )
    }
}
