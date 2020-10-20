package com.codecool.swai.listener

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer

object LocationSpeechListener : RecognitionListener {

    private var callback: LocationListenerCallback? = null

    override fun onReadyForSpeech(params: Bundle?) {
        callback?.onRecognitionStarted()
    }

    override fun onRmsChanged(rmsdB: Float) {
    }

    override fun onBufferReceived(buffer: ByteArray?) {
    }

    override fun onPartialResults(partialResults: Bundle?) {
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
    }

    override fun onBeginningOfSpeech() {
    }

    override fun onEndOfSpeech() {
    }

    override fun onError(error: Int) {
        callback?.onRecognitionError(error)
    }

    override fun onResults(results: Bundle?) {
        results?.let {
            val matches = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val speechInput = matches?.first() ?: ""
            callback?.onRecognitionResult(speechInput)
        }
    }

    fun onAttach(callback: LocationListenerCallback) {
        this.callback = callback
    }

    fun onDetach() {
        this.callback = null
    }
}