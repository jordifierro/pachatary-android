package com.abidria.presentation.experience

import com.abidria.data.scene.Scene
import io.reactivex.Flowable

interface ExperienceMapView {
    fun showScenesOnMap(scenes: List<Scene>)
    fun mapLoadedFlowable(): Flowable<Boolean>
    fun setTitle(title: String)
}
