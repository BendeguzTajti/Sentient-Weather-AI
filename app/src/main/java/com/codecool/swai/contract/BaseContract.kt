package com.codecool.swai.contract

interface BaseContract {

    interface BaseView {
        fun hideLoading()
        fun hideError()
        fun displayError(exception: Throwable)
    }

    interface BasePresenter {
        fun onAttach(view: BaseView)
        fun onDetach()
    }
}