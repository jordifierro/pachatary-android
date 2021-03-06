package com.pachatary.presentation.login

import com.pachatary.presentation.common.view.LoaderView

interface AskLoginEmailView : LoaderView {

    fun finishApplication()
    fun showSuccessMessage()
    fun showErrorMessage()
    fun showEmptyEmailError()
    fun disableAskButton()
    fun enableAskButton()
}
