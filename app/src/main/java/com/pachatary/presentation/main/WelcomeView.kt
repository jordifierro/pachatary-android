package com.pachatary.presentation.main

import com.pachatary.presentation.common.view.LoaderView

interface WelcomeView : LoaderView {

    fun finish()
    fun navigateToMain()
    fun showErrorMessage()
    fun disableStartButton()
    fun enableStartButton()
}
