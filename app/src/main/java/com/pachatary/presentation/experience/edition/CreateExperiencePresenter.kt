package com.pachatary.presentation.experience.edition

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Named

class CreateExperiencePresenter @Inject constructor(
        private val experienceRepository: ExperienceRepository,
        @Named("main") private val mainScheduler: Scheduler): LifecycleObserver {

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
                .observeOn(mainScheduler)
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