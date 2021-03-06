package com.pachatary.presentation.login

import com.pachatary.presentation.common.view.LoaderView

interface LoginView : LoaderView {

    fun loginToken(): String
    fun showSuccessMessage()
    fun showErrorMessage()
    fun showRetry()
    fun navigateToMain()
    fun navigateToAskLoginEmailWithDelay()
    fun finish()
    fun finishWithDelay()
}
