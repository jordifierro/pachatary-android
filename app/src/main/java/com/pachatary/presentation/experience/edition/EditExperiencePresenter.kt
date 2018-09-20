package com.pachatary.presentation.experience.edition

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepository
import io.reactivex.disposables.Disposable
import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Named

class EditExperiencePresenter @Inject constructor(
        private val experienceRepository: ExperienceRepository,
        @Named("main") private val mainScheduler: Scheduler): LifecycleObserver {

    lateinit var view: EditExperienceView
    lateinit var experienceId: String

    lateinit var experience: Experience

    private var getDisposable: Disposable? = null
    private var editDisposable: Disposable? = null

    fun setViewAndExperienceId(view: EditExperienceView, experienceId: String) {
        this.view = view
        this.experienceId = experienceId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        getDisposable = experienceRepository.experienceFlowable(experienceId)
                .observeOn(mainScheduler)
                .take(1)
                .subscribe({ result -> experience = result.data!!
                                       view.showExperience(experience) },
                           { throw it })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        getDisposable?.dispose()
        editDisposable?.dispose()
    }

    fun onUpdateButtonClick() {
        if (view.title().isEmpty() || view.title().length > 80) view.showTitleError()
        else if (view.description().isEmpty()) view.showDescriptionError()
        else updateExperience()
    }

    private fun updateExperience() {
        experience = Experience(experience.id, view.title(), view.description())
        editDisposable = experienceRepository.editExperience(experience)
                .observeOn(mainScheduler)
                .subscribe({ when {
                    it.isInProgress() -> {
                        view.showLoader()
                        view.disableUpdateButton()
                    }
                    it.isSuccess() -> {
                        view.hideLoader()
                        onExperienceUpdatedCorrectly(it.data!!)
                    }
                    it.isError() -> {
                        view.hideLoader()
                        view.enableUpdateButton()
                        view.showError()
                    }
                } }, { throw it })
    }

    private fun onExperienceUpdatedCorrectly(experience: Experience) {
        if (view.picture() != null)
            experienceRepository.uploadExperiencePicture(experience.id, view.picture()!!)
        view.finish()
    }
}