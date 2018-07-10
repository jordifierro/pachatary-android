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

    var title = ""
    var description = ""
    private var selectedImageUriString: String? = null

    var disposable: Disposable? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        view.navigateToEditTitleAndDescription()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        disposable?.dispose()
    }

    fun onTitleAndDescriptionEdited(title: String, description: String) {
        this.title = title
        this.description = description
        view.navigateToSelectImage()
    }

    fun onEditTitleAndDescriptionCanceled() {
        view.finish()
    }

    fun onImageSelectSuccess(selectedImageUriString: String) {
        this.selectedImageUriString = selectedImageUriString
        createExperience()
    }

    fun onImageSelectCancel() {
        view.navigateToEditTitleAndDescription(title, description)
    }

    private fun createExperience() {
        val newExperience = Experience(id = "", title = this.title,
                                       description = this.description, picture = null)
        disposable = experienceRepository.createExperience(newExperience)
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe({ onExperienceCreatedCorrectly(it.data!!) }, { throw it })
    }

    private fun onExperienceCreatedCorrectly(experience: Experience) {
        uploadExperiencePicture(experience.id)
        view.finish()
    }

    private fun uploadExperiencePicture(experienceId: String) {
        experienceRepository.uploadExperiencePicture(experienceId, selectedImageUriString!!)
    }
}