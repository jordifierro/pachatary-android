package com.pachatary.presentation.login

import com.pachatary.presentation.common.view.LoaderView

interface AskLoginEmailView : LoaderView {

    fun finish()
    fun showSuccessMessage()
    fun showErrorMessage()
    fun disableAskButton()
    fun enableAskButton()
}
