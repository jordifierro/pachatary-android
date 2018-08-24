package com.pachatary.presentation.experience.show

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.common.Request
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.data.profile.Profile
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

    private var experiences: List<Experience>? = null
    private var profile: Profile? = null

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
        connectToExperiencesAndProfile()
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
                                            when {
                                                it.isInProgress() ->
                                                    if (it.action == Request.Action.GET_FIRSTS) {
                                                        view.showExperiencesLoader()
                                                        view.showExperienceList(listOf())
                                                        view.hidePaginationLoader()
                                                    } else if (it.action == Request.Action.PAGINATE) {
                                                        view.hideExperiencesLoader()
                                                        view.showPaginationLoader()
                                                    }
                                                else -> {
                                                    view.hideExperiencesLoader()
                                                    view.hidePaginationLoader()
                                                }
                                            }

                                            if (it.isError() &&
                                                    it.action == Request.Action.GET_FIRSTS)
                                                    view.showExperiencesRetry()

                                            if (it.isSuccess()) {
                                                experiences = it.data!!
                                                if (it.data.isEmpty()) view.showNoExperiencesInfo()
                                                else view.showExperienceList(it.data)
                                            }
                                        }, { throw it })
        experiencesRepository.getFirstExperiences(ExperienceRepoSwitch.Kind.MINE)
    }

    private fun connectToProfile() {
        profileDisposable = profileRepository.selfProfile()
                .observeOn(mainScheduler)
                .subscribe({
                    when {
                        it.isSuccess() -> {
                            profile = it.data!!
                            view.showProfile(it.data)
                            view.hideProfileLoader()
                        }
                        it.isInProgress() -> view.showProfileLoader()
                        else -> {
                            view.showProfileRetry()
                            view.hideProfileLoader()
                        }
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

    fun onShareClick() {
        if (experiences != null && profile != null) {
            if (!experiences!!.isEmpty() && profile!!.picture != null)
                view.showShareDialog(profile!!.username)
            else view.showNotEnoughInfoToShareDialog()
        }
    }

    fun onSettingsClick() {
        view.navigateToSettings()
    }
}
