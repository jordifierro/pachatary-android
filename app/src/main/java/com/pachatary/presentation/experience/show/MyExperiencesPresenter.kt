package com.pachatary.presentation.experience.show

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.common.Request
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.data.profile.ProfileRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Named

class MyExperiencesPresenter @Inject constructor(
        private val experiencesRepository: ExperienceRepository,
        private val profileRepository: ProfileRepository,
        private val authRepository: AuthRepository,
        @Named("main") private val mainScheduler: Scheduler) : LifecycleObserver {

    lateinit var view: MyExperiencesView

    private var experiencesDisposable: Disposable? = null
    private var profileDisposable: Disposable? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        if (authRepository.canPersonCreateContent()) connectToExperiencesAndProfile()
        else view.navigateToRegister()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        if (authRepository.canPersonCreateContent() && experiencesDisposable == null)
            connectToExperiencesAndProfile()
    }

    fun onRetryClick() {
        experiencesRepository.getFirstExperiences(ExperienceRepoSwitch.Kind.MINE)
    }

    fun onExperienceClick(experienceId: String) {
        view.navigateToExperience(experienceId)
    }

    private fun connectToExperiencesAndProfile() {
        connectToExperiences()
        connectToProfile()
    }

    private fun connectToExperiences() {
        experiencesDisposable =
                experiencesRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.MINE)
                                        .observeOn(mainScheduler)
                                        .subscribe({
                                            if (it.isInProgress()) {
                                                if (it.action == Request.Action.GET_FIRSTS) {
                                                    view.showExperiencesLoader()
                                                    view.showExperienceList(listOf())
                                                    view.hidePaginationLoader()
                                                }
                                                else if (it.action == Request.Action.PAGINATE) {
                                                    view.hideExperiencesLoader()
                                                    view.showPaginationLoader()
                                                }
                                            }
                                            else {
                                                view.hideExperiencesLoader()
                                                view.hidePaginationLoader()
                                            }

                                            if (it.isError() &&
                                                    it.action == Request.Action.GET_FIRSTS)
                                                    view.showExperiencesRetry()
                                            else view.hideExperiencesRetry()

                                            if (it.isSuccess())
                                                view.showExperienceList(it.data!!)
                                        }, { throw it })
        experiencesRepository.getFirstExperiences(ExperienceRepoSwitch.Kind.MINE)
    }

    private fun connectToProfile() {
        profileDisposable = profileRepository.selfProfile()
                .observeOn(mainScheduler)
                .subscribe({
                    if (it.isSuccess()) {
                        view.showProfile(it.data!!)
                        view.hideProfileLoader()
                        view.hideProfileRetry()
                    }
                    else if (it.isInProgress()) {
                        view.showProfileLoader()
                        view.hideProfileRetry()
                    }
                    else {
                        view.showProfileRetry()
                        view.hideProfileLoader()
                    }
                }, { throw it })
    }

    fun lastExperienceShown() {
        experiencesRepository.getMoreExperiences(ExperienceRepoSwitch.Kind.MINE)
    }

    fun destroy() {
        experiencesDisposable?.dispose()
        profileDisposable?.dispose()
    }

    fun onCreateExperienceClick() {
        if (authRepository.canPersonCreateContent()) view.navigateToCreateExperience()
        else view.navigateToRegister()
    }

    fun onProceedToRegister() {
        view.navigateToRegister()
    }

    fun onDontProceedToRegister() {}

    fun onProfilePictureClick() {
        view.navigateToPickAndCropImage()
    }

    fun onImageSelected(imageUriString: String) {
        profileRepository.uploadProfilePicture(imageUriString)
    }

    fun onBioEdited(newBio: String) {
        profileRepository.editProfile(newBio)
                .observeOn(mainScheduler)
                .subscribe()
    }
}
