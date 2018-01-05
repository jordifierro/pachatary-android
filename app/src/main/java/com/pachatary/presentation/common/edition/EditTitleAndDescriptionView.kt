package com.pachatary.presentation.common.edition

interface EditTitleAndDescriptionView {
    fun title(): String
    fun description(): String
    fun setTitleAndDescription(title: String, description: String)
    fun finishWith(title: String, description: String)
    fun showTitleLengthError()
}