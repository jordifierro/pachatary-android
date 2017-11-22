package com.abidria.presentation.scene.create

interface EditTitleAndDescriptionView {
    fun title(): String
    fun description(): String
    fun setTitleAndDescription(title: String, description: String)
    fun finishWith(title: String, description: String)
    fun showTitleLengthError()
}