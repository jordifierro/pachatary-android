package com.pachatary.presentation.experience.edition

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class EditExperiencePresenter @Inject constructor(
        private val experienceRepository: ExperienceRepository,
        private val schedulerProvider: SchedulerProvider): LifecycleObserver {

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
                .observeOn(schedulerProvider.observer())
                .subscribeOn(schedulerProvider.subscriber())
                .take(1)
                .subscribe { result -> experience = result.data!!
                                       view.showExperience(experience) }
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
                .observeOn(schedulerProvider.observer())
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