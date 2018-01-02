package com.abidria.presentation.register

import com.abidria.presentation.common.view.LoaderView

interface RegisterView : LoaderView {

    fun getUsername(): String
    fun getEmail(): String
    fun showMessage(message: String)
    fun finish()
    fun blockDoneButton(block: Boolean)
}
