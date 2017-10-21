package com.abidria.presentation.scene.create

interface SelectLocationView {
    fun latitude(): Double
    fun longitude(): Double
    fun finishWith(latitude: Double, longitude: Double)
}