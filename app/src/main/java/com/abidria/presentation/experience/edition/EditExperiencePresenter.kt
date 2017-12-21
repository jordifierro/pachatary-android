package com.abidria.presentation.experience.edition

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.abidria.data.experience.Experience
import com.abidria.data.experience.ExperienceRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import javax.inject.Inject

class EditExperiencePresenter @Inject constructor(private val experienceRepository: ExperienceRepository,
                                                  private val schedulerProvider: SchedulerProvider): LifecycleObserver {

    lateinit var view: EditExperienceView
    lateinit var experienceId: String
    lateinit var experience: Experience

    fun setView(view: EditExperienceView, experienceId: String) {
        this.view = view
        this.experienceId = experienceId
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        experienceRepository.experienceFlowable(experienceId)
                .observeOn(schedulerProvider.observer())
                .subscribeOn(schedulerProvider.subscriber())
                .take(1)
                .subscribe { result -> experience = result.data!!
                                       view.navigateToEditTitleAndDescription(experience.title, experience.description) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {}

    fun onTitleAndDescriptionEdited(title: String, description: String) {
        experience = Experience(experience.id, title, description, experience.picture)
        experienceRepository.editExperience(experience)
                .subscribeOn(schedulerProvider.subscriber())
                .observeOn(schedulerProvider.observer())
                .subscribe { onExperienceEditedCorrectly(it.data!!) }
    }

    fun onEditTitleAndDescriptionCanceled() {
        view.finish()
    }

    fun onExperienceEditedCorrectly(experience: Experience) {
        this.experience = experience
        view.askUserToEditPicture()
    }

    fun onAskUserEditPictureResponse(userWantsToEditPicture: Boolean) {
        if (userWantsToEditPicture) view.navigateToPickImage()
        else view.finish()
    }

    fun onImagePicked(selectedImageUriString: String) {
        view.navigateToCropImage(selectedImageUriString)
    }

    fun onPickImageCanceled() {
        view.finish()
    }

    fun onImageCropped(croppedImageUriString: String) {
        experienceRepository.uploadExperiencePicture(experience.id, croppedImageUriString)
        view.finish()
    }

    fun onCropImageCanceled() {
        view.navigateToPickImage()
    }
}