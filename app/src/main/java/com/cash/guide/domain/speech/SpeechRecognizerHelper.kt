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
        _state.value = SpeechRecognitionState.LISTENING
        initRecognizer()
        startListeningSafe()
    }

    private fun initRecognizer() {
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error destroying speech recognizer", e)
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createListener())
        }
    }

    private fun startListeningSafe() {
        if (!isUserRecording) return
        try {
            if (speechRecognizer == null) {
                initRecognizer()
            }
            speechRecognizer?.startListening(buildIntent())
        } catch (e: Exception) {
            Log.w(TAG, "Exception starting listening, re-initializing", e)
            initRecognizer()
            try {
                speechRecognizer?.startListening(buildIntent())
            } catch (e2: Exception) {
                Log.e(TAG, "Failed again to start listening", e2)
            }
        }
    }

    private fun buildIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-MA")
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ar", "fr-FR", Locale.getDefault().toLanguageTag()))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 60000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3500L)
        }
    }

    private fun createListener() = object : RecognitionListener {
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

            // Errors 6 (TIMEOUT), 7 (NO_MATCH), 8 (BUSY), 11 (SERVER_DISCONNECTED)
            // are temporary during continuous recording and should NOT cancel the user's session!
            if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || 
                error == SpeechRecognizer.ERROR_NO_MATCH ||
                error == 11 || 
                error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                
                if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || error == SpeechRecognizer.ERROR_NO_MATCH) {
                    consecutiveSilenceCount++
                }

                mainHandler.postDelayed({
                    if (isUserRecording) {
                        startListeningSafe()
                    }
                }, 300)
                return
            }

            if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                _state.value = SpeechRecognitionState.ERROR
                _errorMessage.value = "يجب السماح بصلاحية الميكروفون"
                isUserRecording = false
                return
            }

            // For any other transient errors, keep recording alive
            mainHandler.postDelayed({
                if (isUserRecording) {
                    startListeningSafe()
                }
            }, 400)
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
                } else if (!current.contains(spokenText)) {
                    "$current $spokenText"
                } else {
                    current
                }
                _accumulatedText.value = updated
                _partialText.value = updated
                latestPartial = ""
            }

            if (isUserRecording) {
                mainHandler.postDelayed({
                    if (isUserRecording) {
                        startListeningSafe()
                    }
                }, 250)
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
    }

    fun stopAndDeliver() {
        isUserRecording = false
        mainHandler.removeCallbacksAndMessages(null)
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
