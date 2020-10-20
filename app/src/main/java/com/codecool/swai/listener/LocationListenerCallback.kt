package com.codecool.swai.listener

interface LocationListenerCallback {
    fun onRecognitionStarted()
    fun onRecognitionResult(speechInput: String)
    fun onRecognitionError(error: Int)
}