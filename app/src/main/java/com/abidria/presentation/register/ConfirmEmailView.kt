package com.abidria.presentation.register

import com.abidria.presentation.common.view.LoaderView

interface ConfirmEmailView : LoaderView {

    fun confirmationToken(): String
    fun showMessage(message: String)
    fun finish()
    fun navigateToMain()
}
