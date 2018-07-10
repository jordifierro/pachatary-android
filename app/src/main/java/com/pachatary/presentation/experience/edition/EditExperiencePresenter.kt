package com.pachatary.presentation.experience.edition

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class EditExperiencePresenter @Inject constructor(private val experienceRepository: ExperienceRepository,
                                                  private val schedulerProvider: SchedulerProvider): LifecycleObserver {

    lateinit var view: EditExperienceView
    lateinit var experienceId: String
    lateinit var experience: Experience
    var getDisposable: Disposable? = null
    var editDisposable: Disposable? = null

    fun setView(view: EditExperienceView, experienceId: String) {
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
                                       view.navigateToEditTitleAndDescription(experience.title, experience.description) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        getDisposable?.dispose()
        editDisposable?.dispose()
    }

    fun onTitleAndDescriptionEdited(title: String, description: String) {
        experience = Experience(experience.id, title, description, experience.picture)
        editDisposable = experienceRepository.editExperience(experience)
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
        if (userWantsToEditPicture) view.navigateToSelectImage()
        else view.finish()
    }

    fun onSelectImageSuccess(selectedImageUriString: String) {
        experienceRepository.uploadExperiencePicture(experience.id, selectedImageUriString)
        view.finish()
    }

    fun onSelectImageCancel() {
        view.finish()
    }
}