package com.pachatary.presentation.common.edition

import io.reactivex.Flowable

interface SelectLocationView {
    enum class ZoomLevel(val zoom: Double) {
        FAR(0.0), MID(13.0), NEAR(18.0)
    }
    fun setInitialLocation(latitude: Double, longitude: Double, zoomLevel: ZoomLevel)
    fun latitude(): Double
    fun longitude(): Double
    fun finishWith(latitude: Double, longitude: Double)
    fun geocodeAddress(address: String) : Flowable<Pair<Double, Double>>
    fun moveMapToPoint(latitude: Double, longitude: Double)
    fun hasLocationPermission(): Boolean
    fun askLocation()
    fun askLocationPermission()
}