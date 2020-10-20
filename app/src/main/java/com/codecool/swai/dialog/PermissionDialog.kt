package com.codecool.swai.dialog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.codecool.swai.R

class PermissionDialog : DialogFragment() {

    companion object {
        const val DIALOG_MESSAGE_KEY = ".DIALOG_MESSAGE_KEY"
        const val PERMISSION_KEY = ".PERMISSION_KEY"
        const val REQUEST_CODE_KEY = ".REQUEST_CODE_KEY"
    }

    interface OnPermissionDialogClosedListener {
        fun requestPermission(permission: String, requestCode: Int)
    }

    private lateinit var onDialogClosedListener: OnPermissionDialogClosedListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.permission_dialog, container, false)
        val message = arguments?.getString(DIALOG_MESSAGE_KEY)
        val dialogMessage = view.findViewById<TextView>(R.id.permissionDialogMessage)
        val positiveButton = view.findViewById<Button>(R.id.positiveButton)
        dialogMessage.text = message
        positiveButton.setOnClickListener {
            dismiss()
            val permission = arguments?.getString(PERMISSION_KEY) ?: "Invalid Permission"
            val requestCode = arguments?.getInt(REQUEST_CODE_KEY) ?: -1
            onDialogClosedListener.requestPermission(permission, requestCode)
        }
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        setStyle(STYLE_NO_TITLE, R.style.PermissionDialogStyle)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onDialogClosedListener = activity as OnPermissionDialogClosedListener
        }catch (e: ClassCastException){
            Log.d(".PermissionDialog", "onAttach: ClassCastException: ${e.message}")
        }
    }
}