package com.pachatary.presentation.register

import com.pachatary.presentation.common.view.LoaderView

interface ConfirmEmailView : LoaderView {

    fun confirmationToken(): String
    fun showSuccessMessage()
    fun showRetry()
    fun showInvalidTokenMessage()
    fun navigateToMain()
    fun navigateToRegisterWithDelay()
}
