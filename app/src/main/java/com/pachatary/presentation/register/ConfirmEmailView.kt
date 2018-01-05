package com.pachatary.presentation.register

import com.pachatary.presentation.common.view.LoaderView

interface ConfirmEmailView : LoaderView {

    fun confirmationToken(): String
    fun showMessage(message: String)
    fun finish()
    fun navigateToMain()
}
