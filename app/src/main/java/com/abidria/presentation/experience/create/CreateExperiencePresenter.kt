package com.abidria.presentation.experience.create

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.abidria.data.experience.Experience
import com.abidria.data.experience.ExperienceRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import javax.inject.Inject

class CreateExperiencePresenter @Inject constructor(private val experienceRepository: ExperienceRepository,
                                                    private val schedulerProvider: SchedulerProvider): LifecycleObserver {

    lateinit var view: CreateExperienceView
    var title = ""
    var description = ""
    var createdExperience: Experience? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        view.navigateToEditTitleAndDescription()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {}

    fun onTitleAndDescriptionEdited(title: String, description: String) {
        this.title = title
        this.description = description
        val newExperience = Experience(id = "", title = this.title, description = this.description, picture = null)
        experienceRepository.createExperience(newExperience)
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe({ onExperienceCreatedCorrectly(it.data!!) })
    }

    fun onEditTitleAndDescriptionCanceled() {
        view.finish()
    }

    fun onExperienceCreatedCorrectly(experience: Experience) {
        this.createdExperience = experience
        view.navigateToPickImage()
    }

    fun onImagePicked(selectedImageUriString: String) {
        view.navigateToCropImage(selectedImageUriString)
    }

    fun onPickImageCanceled() {
        view.finish()
    }

    fun onImageCropped(croppedImageUriString: String) {
        experienceRepository.uploadExperiencePicture(createdExperience!!.id, croppedImageUriString)
        view.finish()
    }

    fun onCropImageCanceled() {
        view.navigateToPickImage()
    }
}