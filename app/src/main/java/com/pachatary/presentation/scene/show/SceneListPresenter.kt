package com.pachatary.presentation.scene.show

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.common.Result
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.data.scene.Scene
import com.pachatary.data.scene.SceneRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class SceneListPresenter @Inject constructor(private val repository: SceneRepository,
                                             private val experienceRepository: ExperienceRepository,
                                             private val schedulerProvider: SchedulerProvider) : LifecycleObserver {

    lateinit var view: SceneListView
    lateinit var experienceId: String
    lateinit var selectedSceneId: String
    var isMine = false

    private var scenesDisposable: Disposable? = null
    private var experienceDisposable: Disposable? = null

    fun setView(view: SceneListView, experienceId: String, selectedSceneId: String, isMine: Boolean) {
        this.view = view
        this.experienceId = experienceId
        this.selectedSceneId = selectedSceneId
        this.isMine = isMine
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        val experienceFlowable = experienceRepository.experienceFlowable(experienceId = this.experienceId)
        val scenesFlowable = repository.scenesFlowable(experienceId = experienceId)

        experienceDisposable = Flowable.combineLatest(experienceFlowable, scenesFlowable,
                BiFunction<Result<Experience>, Result<List<Scene>>, Pair<Experience, List<Scene>>>
                                                                            { rE, rS -> Pair(rE.data!!, rS.data!!)})
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe({ view.showExperienceScenesAndScrollToSelectedIfFirstTime(
                                            it.first, it.second, this.selectedSceneId) })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        experienceDisposable?.dispose()
        scenesDisposable?.dispose()
    }

    fun onEditExperienceClick(experienceId: String) {
        view.navigateToEditExperience(experienceId)
    }

    fun onEditSceneClick(sceneId: String) {
        view.navigateToEditScene(sceneId, experienceId)
    }
}
