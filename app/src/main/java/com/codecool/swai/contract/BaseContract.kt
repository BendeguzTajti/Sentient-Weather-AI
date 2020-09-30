package com.codecool.swai.contract

interface BaseContract {

    interface BaseView {
        fun showLoading()
        fun hideLoading()
        fun hideError()
        fun displayError(exception: Throwable)
    }

    interface BasePresenter<in V:BaseView> {
        fun onAttach(view: V)
        fun onDetach()
    }
}