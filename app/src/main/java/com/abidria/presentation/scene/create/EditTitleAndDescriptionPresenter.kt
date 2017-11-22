package com.abidria.presentation.scene.create

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import javax.inject.Inject

class EditTitleAndDescriptionPresenter @Inject constructor(): LifecycleObserver {

    val MIN_TITLE_LENGTH = 1
    val MAX_TITLE_LENGTH = 30

    lateinit var view: EditTitleAndDescriptionView
    var initialTitle = ""
    var initialDescription = ""

    fun setViewAndInitialTitleAndDescription(view: EditTitleAndDescriptionView,
                                             initialTitle: String, initialDescription: String) {
        this.view = view
        this.initialTitle = initialTitle
        this.initialDescription = initialDescription
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        view.setTitleAndDescription(initialTitle, initialDescription)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {}

    fun doneButtonClick() {
        val title = view.title()
        val description = view.description()

        if (title.length >= MIN_TITLE_LENGTH && title.length <= MAX_TITLE_LENGTH)
            view.finishWith(title = title, description = description)
        else view.showTitleLengthError()
    }
}
