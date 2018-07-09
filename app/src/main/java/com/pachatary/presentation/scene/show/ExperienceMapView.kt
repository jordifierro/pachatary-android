package com.pachatary.presentation.scene.show

import com.pachatary.data.scene.Scene
import com.pachatary.presentation.common.view.LoaderView
import io.reactivex.Flowable

interface ExperienceMapView : LoaderView {

    fun showScenesOnMap(scenes: List<Scene>)
    fun mapLoadedFlowable(): Flowable<Any>
    fun navigateToCreateScene(experienceId: String)
}
