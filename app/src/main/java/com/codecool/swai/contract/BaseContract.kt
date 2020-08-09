package com.codecool.swai.contract

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog

interface BaseContract {

    interface BaseView {
        fun displayError(exception: Throwable)
        fun requestPermission(permission: String, requestCode: Int)
    }

    interface BasePresenter {
        fun onDetach()
        fun buildPermissionDialog(inflater: LayoutInflater, message: String, permission: String, requestCode: Int): AlertDialog
    }
}