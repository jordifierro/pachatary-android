package com.abidria.presentation.scene.create

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import javax.inject.Inject

class SelectLocationPresenter @Inject constructor(): LifecycleObserver {

    lateinit var view: SelectLocationView

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {}

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {}

    fun doneButtonClick() {
        view.finishWith(latitude = view.latitude(), longitude = view.longitude())
    }
}