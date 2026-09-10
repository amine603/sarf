package com.cash.guide.domain.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
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

    private val _state = MutableStateFlow(SpeechRecognitionState.IDLE)
    val state: StateFlow<SpeechRecognitionState> = _state.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var onSpeechResult: ((String) -> Unit)? = null

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = SpeechRecognitionState.ERROR
            _errorMessage.value = "التعرف على الصوت غير متاح في هذا الهاتف"
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = SpeechRecognitionState.LISTENING
                    _partialText.value = ""
                    _errorMessage.value = null
                }

                override fun onBeginningOfSpeech() {
                    _state.value = SpeechRecognitionState.LISTENING
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _state.value = SpeechRecognitionState.PROCESSING
                }

                override fun onError(error: Int) {
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "لم يتم التعرف على الصوت، حاول مرة أخرى"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "انتهى الوقت، اضغط وتحدث مجدداً"
                        SpeechRecognizer.ERROR_AUDIO -> "خطأ في الميكروفون"
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "تحقق من اتصال الإنترنت"
                        else -> "حدث خطأ، يرجى إعادة المحاولة"
                    }
                    Log.w(TAG, "SpeechRecognizer error: $error ($msg)")
                    _state.value = SpeechRecognitionState.ERROR
                    _errorMessage.value = msg
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull() ?: ""
                    Log.d(TAG, "Speech recognition result: $spokenText")
                    _state.value = SpeechRecognitionState.IDLE
                    _partialText.value = spokenText
                    if (spokenText.isNotBlank()) {
                        onSpeechResult?.invoke(spokenText)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        _partialText.value = text
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
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognition", e)
            _state.value = SpeechRecognitionState.ERROR
            _errorMessage.value = "تعذر تشغيل الميكروفون"
        }
    }

    fun stopListening() {
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

    companion object {
        private const val TAG = "SpeechRecognizerHelper"
    }
}
