package com.pachatary.presentation.register

import com.pachatary.presentation.common.view.LoaderView

interface RegisterView : LoaderView {

    fun getUsername(): String
    fun getEmail(): String
    fun showMessage(message: String)
    fun finish()
    fun blockDoneButton(block: Boolean)
}
