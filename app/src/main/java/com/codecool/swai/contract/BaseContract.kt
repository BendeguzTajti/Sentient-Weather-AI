package com.codecool.swai.contract

interface BaseContract {

    interface BaseView {
        fun displayError()
    }

    interface BasePresenter {
        fun onDetach()
    }
}