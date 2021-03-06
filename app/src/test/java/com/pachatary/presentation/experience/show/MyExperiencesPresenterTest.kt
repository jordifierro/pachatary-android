package com.pachatary.presentation.experience.show

import com.pachatary.data.*
import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.common.*
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.data.picture.LittlePicture
import com.pachatary.data.profile.Profile
import com.pachatary.data.profile.ProfileRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class MyExperiencesPresenterTest {

    @Test
    fun test_if_cannot_create_content_shows_register_dialog() {
        given {
            an_auth_repo_that_returns_on_can_create_content(false)
        } whenn {
            create_presenter()
        } then {
            should_navigate_to_register()
        }
    }

    @Test
    fun test_create_asks_experiences_and_shows_if_can_create_content() {
        given {
            an_auth_repo_that_returns_on_can_create_content(true)
            an_experience_repo_that_returns(DummyExperiencesResultSuccess(listOf("4", "5")))
            a_profile_repo_that_returns(DummyProfileResult("usr"))
        } whenn {
            create_presenter()
        } then {
            should_call_repo_get_firsts_experiences()
            should_show_experiences(listOf(DummyExperience("4"), DummyExperience("5")))
            should_hide_experiences_loader()

            should_show_profile(DummyProfile("usr"))
            should_hide_profile_loader()
        }
    }

    @Test
    fun test_create_when_no_experiences_shows_no_experience_info() {
        given {
            an_auth_repo_that_returns_on_can_create_content(true)
            an_experience_repo_that_returns(DummyExperiencesResultSuccess(listOf()))
            a_profile_repo_that_returns(DummyProfileResult("usr"))
        } whenn {
            create_presenter()
        } then {
            should_call_repo_get_firsts_experiences()
            should_show_no_experiences_info()
            should_hide_experiences_loader()

            should_show_profile(DummyProfile("usr"))
            should_hide_profile_loader()
        }
    }

    @Test
    fun test_when_result_in_progress_last_event_get_firsts_shows_loader_if_can_create_content() {
        given {
            an_auth_repo_that_returns_on_can_create_content(true)
            an_experience_repo_that_returns(ResultInProgress(action = Request.Action.GET_FIRSTS))
            a_profile_repo_that_returns(ResultInProgress())
        } whenn {
            create_presenter()
        } then {
            should_hide_view_pagination_loader()
            should_show_experiences_loader()
            should_show_experiences(listOf())

            should_show_profile_loader()
        }
    }

    @Test
    fun test_when_result_in_progress_last_event_paginate_shows_loader_if_can_create_content() {
        given {
            an_auth_repo_that_returns_on_can_create_content(true)
            an_experience_repo_that_returns(ResultInProgress(action = Request.Action.PAGINATE))
            a_profile_repo_that_returns(ResultInProgress())
        } whenn {
            create_presenter()
        } then {
            should_show_view_pagination_loader()
            should_hide_experiences_loader()

            should_show_profile_loader()
        }
    }

    @Test
    fun test_resume_checks_if_can_create_content_has_changed_and_connects_to_experiences() {
        given {
            an_auth_repo_that_returns_on_can_create_content(true)
            an_experience_repo_that_returns(DummyExperiencesResultSuccess(listOf("4", "5")))
            a_profile_repo_that_returns(DummyProfileResult("usr"))
        } whenn {
            resume_presenter()
        } then {
            should_call_repo_get_firsts_experiences()
            should_show_experiences(listOf(DummyExperience("4"), DummyExperience("5")))
            should_hide_experiences_loader()

            should_show_profile(DummyProfile("usr"))
            should_hide_profile_loader()
        }
    }

    @Test
    fun test_resume_does_nothing_if_cannot_create_content() {
        given {
            an_auth_repo_that_returns_on_can_create_content(false)
        } whenn {
            resume_presenter()
        } then {
            should_do_nothing()
        }
    }

    @Test
    fun test_resume_does_nothing_if_already_connected_to_experiences_flowable() {
        given {
            an_auth_repo_that_returns_on_can_create_content(true)
            an_experience_repo_that_returns(DummyExperiencesResultSuccess(listOf("4", "5")))
            a_profile_repo_that_returns(DummyProfileResult("usr"))
        } whenn {
            create_presenter()
        } then {
            should_call_repo_get_firsts_experiences()
            should_call_repo_my_experience_flowable()
            should_show_experiences(listOf(DummyExperience("4"), DummyExperience("5")))
            should_hide_experiences_loader()
            should_hide_view_pagination_loader()

            should_call_profile_repo_self()
            should_show_profile(DummyProfile("usr"))
            should_hide_profile_loader()
        } whenn {
            resume_presenter()
        } then {
            should_do_nothing()
        }
    }

    @Test
    fun test_create_when_response_error_shows_retry_if_last_event_is_get_firsts() {
        given {
            an_auth_repo_that_returns_on_can_create_content(true)
            an_experience_repo_that_returns(
                    ResultError(Exception(), action = Request.Action.GET_FIRSTS))
            a_profile_repo_that_returns(DummyResultError())
        } whenn {
            create_presenter()
        } then {
            should_hide_experiences_loader()
            should_hide_view_pagination_loader()
            should_show_experiences_retry()

            should_hide_profile_loader()
            should_show_profile_retry()
        }
    }

    @Test
    fun test_create_when_response_error_hides_loaders_if_last_event_is_paginate() {
        given {
            an_auth_repo_that_returns_on_can_create_content(true)
            an_experience_repo_that_returns(
                    ResultError(Exception(), action = Request.Action.PAGINATE))
            a_profile_repo_that_returns(DummyResultError())
        } whenn {
            create_presenter()
        } then {
            should_hide_experiences_loader()
            should_hide_view_pagination_loader()

            should_hide_profile_loader()
            should_show_profile_retry()
        }
    }

    @Test
    fun test_on_retry_click_retrive_experiences_and_shows_them() {
        given {
            an_experience_repo_that_returns(DummyExperiencesResultSuccess(listOf()))
            a_profile_repo_that_returns(DummyProfileResult(""))
        } whenn {
            retry_clicked()
        } then {
            should_call_profile_repo_self()
            should_call_repo_get_firsts_experiences()
        }
    }

    @Test
    fun test_on_refresh_gets_firsts_experiences() {
        given {
            an_experience_repo_that_returns(DummyExperiencesResultSuccess(listOf()))
            a_profile_repo_that_returns(DummyProfileResult(""))
        } whenn {
            refresh()
        } then {
            should_call_repo_get_firsts_experiences()
        }
    }

    @Test
    fun test_experience_tapped() {
        given {
            nothing()
        } whenn {
            experience_click("2")
        } then {
            should_navigate_to_experience("2")
        }
    }

    @Test
    fun test_create_new_experience_button_click_when_can_create_content() {
        given {
            an_auth_repo_that_returns_on_can_create_content(true)
        } whenn {
            on_create_experience_click()
        } then {
            should_navigate_to_create_experience()
        }
    }

    @Test
    fun test_create_new_experience_button_click_if_cannot_create_content_shows_register_dialog() {
        given {
            an_auth_repo_that_returns_on_can_create_content(false)
        } whenn {
            on_create_experience_click()
        } then {
            should_navigate_to_register()
        }
    }

    @Test
    fun test_unsubscribe_on_destroy() {
        given {
            repos_that_returns_test_observables()
        } whenn {
            create_presenter()
            destroy_presenter()
        } then {
            should_unsubscribe_observables()
        }
    }

    @Test
    fun test_on_proceed_to_register_navigates_to_register() {
        given {
            nothing()
        } whenn {
            proceed_to_register()
        } then {
            should_navigate_to_register()
        }
    }

    @Test
    fun test_on_dont_proceed_to_register_must_do_nothing() {
        given {
            nothing()
        } whenn {
            dont_proceed_to_register()
        } then {
            should_do_nothing()
        }
    }

    @Test
    fun test_last_experience_shown_calls_api_paginate() {
        given {
            nothing()
        } whenn {
            last_experience_shown()
        } then {
            should_call_repo_get_more_experiences()
        }
    }

    @Test
    fun test_on_profile_picture_click_navigates_to_select_image() {
        given {
            nothing()
        } whenn {
            profile_picture_click()
        } then {
            should_navigate_to_select_image()
        }
    }

    @Test
    fun test_on_image_selected_uploads_profile_picture() {
        given {
            nothing()
        } whenn {
            image_selected("path")
        } then {
            should_call_profile_repo_upload_picture("path")
        }
    }

    @Test
    fun test_edit_bio() {
        given {
            a_profile_repo_that_returns_test_observable_on_edit("new bio")
        } whenn {
            bio_edited("new bio")
        } then {
            should_call_profile_repo_edit_with("new bio")
            should_subscribe_to_edit_test_observable()
        }
    }

    @Test
    fun test_when_no_experiences_and_no_profile_does_nothing() {
        given {
            nothing()
        } whenn {
            share_click()
        } then {
            should_do_nothing()
        }
    }

    @Test
    fun test_when_no_profile_picture_shows_forbidden_info() {
        given {
            an_auth_repo_that_returns_on_can_create_content(true)
            an_experience_repo_that_returns(DummyExperiencesResultSuccess(listOf("2")))
            a_profile_repo_that_returns(DummyProfileResult("a"))
        } whenn {
            create_presenter()
            share_click()
        } then {
            should_show_forbidden_share_dialog()
        }
    }

    @Test
    fun test_when_zero_experiences_shows_forbidden_info() {
        given {
            an_auth_repo_that_returns_on_can_create_content(true)
            an_experience_repo_that_returns(DummyExperiencesResultSuccess(listOf()))
            a_profile_repo_that_returns(
                    ResultSuccess(Profile("", "", LittlePicture("", "", ""), false)))
        } whenn {
            create_presenter()
            share_click()
        } then {
            should_show_forbidden_share_dialog()
        }
    }

    @Test
    fun test_when_profile_pic_and_experiences_shows_share_dialog() {
        given {
            an_auth_repo_that_returns_on_can_create_content(true)
            an_experience_repo_that_returns(DummyExperiencesResultSuccess(listOf("2")))
            a_profile_repo_that_returns(
                    ResultSuccess(Profile("usr", "", LittlePicture("", "", ""), false)))
        } whenn {
            create_presenter()
            share_click()
        } then {
            should_show_share_dialog("usr")
        }
    }

    @Test
    fun test_on_settings_click_navigates_to_settings() {
        given {
            nothing()
        } whenn {
            settings_click()
        } then {
            should_navigate_to_settings()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: MyExperiencesPresenter
        @Mock lateinit var mockView: MyExperiencesView
        @Mock private lateinit var mockExperiencesRepository: ExperienceRepository
        @Mock private lateinit var mockProfileRepository: ProfileRepository
        @Mock private lateinit var mockAuthRepository: AuthRepository
        private val testExperiencesObservable =
                PublishSubject.create<Result<List<Experience>>>()
        private val testProfileObservable = PublishSubject.create<Result<Profile>>()
        private val testEditProfileObservable = PublishSubject.create<Result<Profile>>()

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = MyExperiencesPresenter(mockExperiencesRepository, mockProfileRepository,
                                               mockAuthRepository, Schedulers.trampoline())
            presenter.view = mockView

            return this
        }

        fun nothing() {}

        fun an_experience_repo_that_returns(vararg results: Result<List<Experience>>) {
            given(mockExperiencesRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.MINE))
                    .willReturn(Flowable.fromArray(*results))
        }

        fun a_profile_repo_that_returns(vararg results: Result<Profile>) {
            BDDMockito.given(mockProfileRepository.selfProfile())
                    .willReturn(Flowable.fromArray(*results))
        }

        fun a_profile_repo_that_returns_test_observable_on_edit(bio: String) {
            BDDMockito.given(mockProfileRepository.editProfile(bio))
                    .willReturn(testEditProfileObservable.toFlowable(BackpressureStrategy.LATEST))
        }

        fun an_auth_repo_that_returns_on_can_create_content(can: Boolean) {
            BDDMockito.given(mockAuthRepository.canPersonCreateContent()).willReturn(can)
        }

        fun create_presenter() {
            presenter.create()
        }

        fun resume_presenter() {
            presenter.resume()
        }

        fun proceed_to_register() {
            presenter.onProceedToRegister()
        }

        fun retry_clicked() {
            presenter.onRetryClick()
        }

        fun refresh() {
            presenter.onRefresh()
        }

        fun on_create_experience_click() {
            presenter.onCreateExperienceClick()
        }

        fun experience_click(experienceId: String) {
            presenter.onExperienceClick(experienceId)
        }

        fun dont_proceed_to_register() {
            presenter.onDontProceedToRegister()
        }

        fun last_experience_shown() {
            presenter.lastExperienceShown()
        }

        fun profile_picture_click() {
            presenter.onProfilePictureClick()
        }

        fun image_selected(imageUriString: String) {
            presenter.onImageSelected(imageUriString)
        }

        fun bio_edited(bio: String) {
            presenter.onBioEdited(bio)
        }

        fun share_click() {
            presenter.onShareClick()
        }

        fun settings_click() {
            presenter.onSettingsClick()
        }

        fun should_show_experiences(experiences: List<Experience>) {
            BDDMockito.then(mockView).should().showExperienceList(experiences)
        }

        fun should_call_repo_get_more_experiences() {
            then(mockExperiencesRepository).should()
                    .getMoreExperiences(ExperienceRepoSwitch.Kind.MINE)
        }

        fun should_show_experiences_loader() {
            then(mockView).should().showExperiencesLoader()
        }

        fun should_hide_experiences_loader() {
            then(mockView).should().hideExperiencesLoader()
        }

        fun should_show_view_pagination_loader() {
            then(mockView).should().showPaginationLoader()
        }

        fun should_hide_view_pagination_loader() {
            then(mockView).should().hidePaginationLoader()
        }

        fun should_show_experiences_retry() {
            then(mockView).should().showExperiencesRetry()
        }

        fun should_call_repo_get_firsts_experiences() {
            then(mockExperiencesRepository).should()
                    .getFirstExperiences(ExperienceRepoSwitch.Kind.MINE)
        }

        fun should_navigate_to_experience(experienceId: String) {
            then(mockView).should().navigateToExperience(experienceId)
        }

        fun should_navigate_to_create_experience() {
            then(mockView).should().navigateToCreateExperience()
        }

        fun repos_that_returns_test_observables() {
            given(mockExperiencesRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.MINE))
                    .willReturn(testExperiencesObservable.toFlowable(BackpressureStrategy.LATEST))
            given(mockProfileRepository.selfProfile())
                    .willReturn(testProfileObservable.toFlowable(BackpressureStrategy.LATEST))
        }

        fun destroy_presenter() {
            presenter.destroy()
        }

        fun should_unsubscribe_observables() {
            assertFalse(testExperiencesObservable.hasObservers())
            assertFalse(testProfileObservable.hasObservers())
        }

        fun should_do_nothing() {
            BDDMockito.then(mockView).shouldHaveNoMoreInteractions()
            BDDMockito.then(mockExperiencesRepository).shouldHaveNoMoreInteractions()
            BDDMockito.then(mockProfileRepository).shouldHaveNoMoreInteractions()
        }

        fun should_call_repo_my_experience_flowable() {
            BDDMockito.then(mockExperiencesRepository).should()
                    .experiencesFlowable(ExperienceRepoSwitch.Kind.MINE)
        }

        fun should_navigate_to_register() {
            BDDMockito.then(mockView).should().navigateToRegister()
        }

        fun should_show_profile(profile: Profile) {
            BDDMockito.then(mockView).should().showProfile(profile)
        }

        fun should_hide_profile_loader() {
            BDDMockito.then(mockView).should().hideProfileLoader()
        }

        fun should_show_profile_loader() {
            BDDMockito.then(mockView).should().showProfileLoader()
        }

        fun should_show_profile_retry() {
            BDDMockito.then(mockView).should().showProfileRetry()
        }

        fun should_call_profile_repo_self() {
            BDDMockito.then(mockProfileRepository).should().selfProfile()
        }

        fun should_navigate_to_select_image() {
            BDDMockito.then(mockView).should().navigateToPickAndCropImage()
        }

        fun should_call_profile_repo_upload_picture(imageUriString: String) {
            BDDMockito.then(mockProfileRepository).should().uploadProfilePicture(imageUriString)
        }

        fun should_call_profile_repo_edit_with(bio: String) {
            BDDMockito.then(mockProfileRepository).should().editProfile(bio)
        }

        fun should_subscribe_to_edit_test_observable() {
            assert(testEditProfileObservable.hasObservers())
        }

        fun should_show_no_experiences_info() {
            BDDMockito.then(mockView).should().showNoExperiencesInfo()
        }

        fun should_show_forbidden_share_dialog() {
            BDDMockito.then(mockView).should().showNotEnoughInfoToShareDialog()
        }

        fun should_show_share_dialog(username: String) {
            BDDMockito.then(mockView).should().showShareDialog(username)
        }

        fun should_navigate_to_settings() {
            BDDMockito.then(mockView).should().navigateToSettings()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
