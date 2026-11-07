package digital.vmstudio.myassistant.presentation.assistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Explicit, foreground push-to-talk speech input. The Android recognizer is used;
 * no audio is continuously captured or uploaded to the AI gateway.
 */
class SpeechInputController(
    context: Context,
    private val onText: (String) -> Unit,
    private val onListeningChanged: (Boolean) -> Unit,
    private val onError: (String) -> Unit
) : RecognitionListener {
    private val appContext = context.applicationContext
    private val recognizer: SpeechRecognizer? =
        if (SpeechRecognizer.isRecognitionAvailable(appContext)) {
            SpeechRecognizer.createSpeechRecognizer(appContext)
        } else null

    fun start() {
        val service = recognizer
        if (service == null) {
            onError("Speech recognition is not available on this device.")
            return
        }

        service.setRecognitionListener(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            service.startListening(intent)
            onListeningChanged(true)
        } catch (e: Exception) {
            onListeningChanged(false)
            onError("Could not start speech recognition. Please try again.")
        }
    }

    fun stop() {
        recognizer?.stopListening()
        onListeningChanged(false)
    }

    fun destroy() {
        recognizer?.destroy()
        onListeningChanged(false)
    }

    override fun onReadyForSpeech(params: Bundle?) {
        onListeningChanged(true)
    }

    override fun onBeginningOfSpeech() {
        onListeningChanged(true)
    }

    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        onListeningChanged(false)
    }

    override fun onError(error: Int) {
        onListeningChanged(false)
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Microphone audio error."
            SpeechRecognizer.ERROR_CLIENT -> "Speech recognition was interrupted."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "Speech recognition network is unavailable."
            SpeechRecognizer.ERROR_NO_MATCH -> "I couldn't recognise that. Please try again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognition is busy. Try again in a moment."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything. Tap the microphone and speak."
            else -> "Speech recognition failed. Please try again."
        }
        onError(message)
    }

    override fun onResults(results: Bundle?) {
        onListeningChanged(false)
        val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()?.trim().orEmpty()
        if (spoken.isNotBlank()) onText(spoken)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val spoken = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()?.trim().orEmpty()
        if (spoken.isNotBlank()) onText(spoken)
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}
