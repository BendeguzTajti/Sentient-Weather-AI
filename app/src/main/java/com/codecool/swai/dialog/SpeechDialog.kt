package com.codecool.swai.dialog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView
import com.codecool.swai.BaseApp
import com.codecool.swai.R

class SpeechDialog : DialogFragment() {

    interface OnSpeechDialogCanceledListener {
        fun cancelRecognition()
    }

    private lateinit var onDialogCanceledListener: OnSpeechDialogCanceledListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.speech_dialog, container, false)
        val background = view.findViewById<LottieAnimationView>(R.id.robot)
        val speechCancelButton = view.findViewById<ImageButton>(R.id.speechCancelButton)
        background.setComposition(BaseApp.speechDialogBackground)
        speechCancelButton.setOnClickListener {
            dismiss()
            onDialogCanceledListener.cancelRecognition()
        }
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        setStyle(STYLE_NO_TITLE, R.style.SpeechDialogStyle)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onDialogCanceledListener = activity as OnSpeechDialogCanceledListener
        }catch (e: ClassCastException) {
            Log.d(".SpeechDialog", "onAttach: ClassCastException: ${e.message}")
        }
    }
}