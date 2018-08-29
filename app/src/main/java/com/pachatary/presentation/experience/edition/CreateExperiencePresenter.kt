package com.pachatary.presentation.experience.edition

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class CreateExperiencePresenter @Inject constructor(
        private val experienceRepository: ExperienceRepository,
        private val schedulerProvider: SchedulerProvider): LifecycleObserver {

    lateinit var view: CreateExperienceView

    var disposable: Disposable? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        disposable?.dispose()
    }

    fun onCreateButtonClick() {
        if (view.title().isEmpty() || view.title().length > 80) view.showTitleError()
        else if (view.description().isEmpty()) view.showDescriptionError()
        else if (view.picture() == null) view.showPictureError()
        else createExperience()
    }

    private fun createExperience() {
        val newExperience = Experience(id = "", title = view.title(),
                description = view.description(), picture = null)
        disposable = experienceRepository.createExperience(newExperience)
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe({ when {
                    it.isInProgress() -> {
                        view.showLoader()
                        view.disableCreateButton()
                    }
                    it.isSuccess() -> {
                        view.hideLoader()
                        onExperienceCreatedCorrectly(it.data!!)
                    }
                    it.isError() -> {
                        view.hideLoader()
                        view.enableCreateButton()
                        view.showError()
                    }
                } }, { throw it })
    }

    private fun onExperienceCreatedCorrectly(experience: Experience) {
        experienceRepository.uploadExperiencePicture(experience.id, view.picture()!!)
        view.finish()
    }
}