package com.pachatary.presentation.main

import com.pachatary.presentation.common.view.LoaderView

interface WelcomeView : LoaderView {

    fun finish()
    fun navigateToMain()
    fun showErrorMessage()
    fun disableButtons()
    fun enableButtons()
    fun navigateToAskLogin()
    fun navigateToPrivacyPolicy()
    fun navigateToTermsAndConditions()
}
