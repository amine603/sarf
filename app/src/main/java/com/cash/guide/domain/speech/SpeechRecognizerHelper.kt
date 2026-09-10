package com.cash.guide.domain.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class SpeechRecognitionState {
    IDLE,
    LISTENING,
    PROCESSING,
    ERROR
}

class SpeechRecognizerHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow(SpeechRecognitionState.IDLE)
    val state: StateFlow<SpeechRecognitionState> = _state.asStateFlow()

    private val _accumulatedText = MutableStateFlow("")
    val accumulatedText: StateFlow<String> = _accumulatedText.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var latestPartial: String = ""
    private var isUserRecording = false
    private var consecutiveSilenceCount = 0

    var onSpeechResult: ((String) -> Unit)? = null

    fun reset() {
        isUserRecording = false
        stopListening()
        _accumulatedText.value = ""
        _partialText.value = ""
        latestPartial = ""
        consecutiveSilenceCount = 0
        _errorMessage.value = null
        _state.value = SpeechRecognitionState.IDLE
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = SpeechRecognitionState.ERROR
            _errorMessage.value = "التعرف على الصوت غير متاح في هذا الهاتف"
            return
        }

        reset()
        isUserRecording = true
        startListeningInternal()
    }

    private fun startListeningInternal() {
        if (!isUserRecording) return

        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error destroying previous recognizer instance", e)
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (isUserRecording) {
                        _state.value = SpeechRecognitionState.LISTENING
                        _errorMessage.value = null
                    }
                }

                override fun onBeginningOfSpeech() {
                    if (isUserRecording) {
                        _state.value = SpeechRecognitionState.LISTENING
                        consecutiveSilenceCount = 0
                    }
                }

                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    Log.w(TAG, "SpeechRecognizer error: $error, isUserRecording=$isUserRecording")
                    if (!isUserRecording) return

                    if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || error == SpeechRecognizer.ERROR_NO_MATCH) {
                        consecutiveSilenceCount++
                        // If user stays completely silent for multiple intervals and has text, we can stop
                        if (consecutiveSilenceCount >= 5 && getBestTranscript().isNotBlank()) {
                            stopAndDeliver()
                            return
                        }
                        // Otherwise, automatically restart listening so user can pause to think!
                        mainHandler.postDelayed({
                            if (isUserRecording) {
                                startListeningInternal()
                            }
                        }, 200)
                        return
                    }

                    val msg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "خطأ في الميكروفون"
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "تحقق من اتصال الإنترنت"
                        else -> "حدث خطأ في التسجيل"
                    }
                    _state.value = SpeechRecognitionState.ERROR
                    _errorMessage.value = msg
                    isUserRecording = false
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull()?.trim() ?: ""
                    Log.d(TAG, "Speech recognition onResults chunk: '$spokenText', isUserRecording=$isUserRecording")

                    if (spokenText.isNotBlank()) {
                        consecutiveSilenceCount = 0
                        val current = _accumulatedText.value.trim()
                        val updated = if (current.isBlank()) {
                            spokenText
                        } else if (!current.endsWith(spokenText)) {
                            "$current $spokenText"
                        } else {
                            current
                        }
                        _accumulatedText.value = updated
                        _partialText.value = updated
                        latestPartial = ""
                    }

                    // If user is still recording, seamlessly continue listening for the next phrase!
                    if (isUserRecording) {
                        mainHandler.postDelayed({
                            if (isUserRecording) {
                                startListeningInternal()
                            }
                        }, 150)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    if (!isUserRecording) return
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim() ?: ""
                    if (text.isNotBlank()) {
                        latestPartial = text
                        val current = _accumulatedText.value.trim()
                        val combined = if (current.isBlank()) {
                            text
                        } else {
                            "$current $text"
                        }
                        _partialText.value = combined
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-MA")
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ar", "fr-FR", Locale.getDefault().toLanguageTag()))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 30000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognition", e)
            _state.value = SpeechRecognitionState.ERROR
            _errorMessage.value = "تعذر تشغيل الميكروفون"
            isUserRecording = false
        }
    }

    fun stopAndDeliver() {
        isUserRecording = false
        val transcript = getBestTranscript()
        stopListening()
        _state.value = SpeechRecognitionState.IDLE
        if (transcript.isNotBlank()) {
            onSpeechResult?.invoke(transcript)
        }
    }

    fun stopListening() {
        isUserRecording = false
        mainHandler.removeCallbacksAndMessages(null)
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying speech recognizer", e)
        } finally {
            speechRecognizer = null
            if (_state.value != SpeechRecognitionState.PROCESSING) {
                _state.value = SpeechRecognitionState.IDLE
            }
        }
    }

    fun getBestTranscript(): String {
        val acc = _accumulatedText.value.trim()
        val partial = latestPartial.trim()
        return when {
            acc.isNotBlank() && partial.isNotBlank() && !acc.endsWith(partial) -> "$acc $partial".trim()
            acc.isNotBlank() -> acc
            else -> partial
        }
    }

    companion object {
        private const val TAG = "SpeechRecognizerHelper"
    }
}
