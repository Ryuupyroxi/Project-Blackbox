package com.blackbox.ai.agent.voice

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Kai-style voice round-trip for the agent chat: text-to-speech (TTS) replies
 * and speech-to-text (STT) input using Android's built-in engines. No extra
 * dependencies; requires RECORD_AUDIO permission for STT (already declared).
 */
class BlackboxVoice(context: Context) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var recognizer: SpeechRecognizer? = null

    fun init(onReady: (Boolean) -> Unit) {
        if (tts != null) return
        tts = TextToSpeech(appContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            tts?.language = Locale.getDefault()
            onReady(ttsReady)
        }
    }

    fun isTtsReady(): Boolean = ttsReady

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        val engine = tts ?: return
        if (!ttsReady) return
        val utteranceId = "blackbox_tts_${System.currentTimeMillis()}"
        engine.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                onDone?.invoke()
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onDone?.invoke()
            }
        })
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    /**
     * Starts STT and delivers the recognized text via [onResult].
     * Returns false if speech recognition is unavailable.
     */
    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit): Boolean {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            onError("Speech recognition not available")
            return false
        }
        stopListening()
        val sr = SpeechRecognizer.createSpeechRecognizer(appContext)
        recognizer = sr
        sr.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                onError("STT error $error")
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull().orEmpty()
                if (text.isNotBlank()) onResult(text) else onError("No speech recognized")
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        sr.startListening(intent)
        return true
    }

    fun stopListening() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }

    fun shutdown() {
        stopListening()
        tts?.stop()
        tts?.shutdown()
        tts = null
        ttsReady = false
    }
}
