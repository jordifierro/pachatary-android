package com.pachatary.presentation.register

import com.pachatary.presentation.common.view.LoaderView

interface RegisterView : LoaderView {

    fun getUsername(): String
    fun getEmail(): String
    fun finish()
    fun finishApplication()
    fun blockDoneButton(block: Boolean)
    fun showSuccessMessage()
    fun showErrorMessage(message: String)
}
