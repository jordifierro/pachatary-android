package com.pachatary.presentation.common.edition

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import javax.inject.Inject

class SelectLocationPresenter @Inject constructor(val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    enum class LocationType {
        UNKNWON, APROX, SPECIFIC
    }

    lateinit var view: SelectLocationView
    var initialLatitude = 0.0
    var initialLongitude = 0.0
    var initialLocationType = LocationType.UNKNWON

    fun setViewAndInitialLocation(view: SelectLocationView, initialLatitude: Double, initialLongitude: Double,
                                  initialLocationType: LocationType) {
        this.view = view
        this.initialLatitude = initialLatitude
        this.initialLongitude = initialLongitude
        this.initialLocationType = initialLocationType
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        var zoomLevel = SelectLocationView.ZoomLevel.FAR
        when (initialLocationType) {
            LocationType.APROX -> zoomLevel = SelectLocationView.ZoomLevel.MID
            LocationType.SPECIFIC -> zoomLevel = SelectLocationView.ZoomLevel.NEAR
            LocationType.UNKNWON -> zoomLevel = SelectLocationView.ZoomLevel.FAR
        }
        view.setInitialLocation(latitude = initialLatitude, longitude = initialLongitude, zoomLevel = zoomLevel)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {}

    fun doneButtonClick() {
        val latitude = view.latitude()
        val longitude = view.longitude()
        if (latitude != null && longitude != null)
            view.finishWith(latitude = latitude, longitude = longitude)
    }

    @SuppressLint("CheckResult")
    fun searchButtonClick(searchText: String) {
        view.geocodeAddress(searchText)
                .observeOn(schedulerProvider.observer())
                .subscribeOn(schedulerProvider.subscriber())
                .subscribe { view.moveMapToPoint(it.first, it.second) }
    }

    fun locateClick() {
        if (view.hasLocationPermission()) view.askLocation()
        else view.askLocationPermission()
    }

    fun onLocationPermissionAccepted() {
        view.askLocation()
    }

    fun onLocationFound(latitude: Double, longitude: Double) {
        view.moveMapToPoint(latitude, longitude)
    }

    fun onLocationPermissionDenied() { }
}