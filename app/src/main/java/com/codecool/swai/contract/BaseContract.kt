package com.codecool.swai.contract

interface BaseContract {

    interface BaseView {
        fun displayError(exception: Throwable)
    }

    interface BasePresenter {
        fun onDetach()
    }
}