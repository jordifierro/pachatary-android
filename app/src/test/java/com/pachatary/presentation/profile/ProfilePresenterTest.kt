package com.pachatary.presentation.profile

import com.pachatary.data.*
import com.pachatary.data.common.*
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.data.profile.Profile
import com.pachatary.data.profile.ProfileRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ProfilePresenterTest {

    @Test
    fun test_create_asks_firsts_experiences_and_profile() {
        given {
            a_presenter("username")
            an_experience_repo_that_returns(ResultInProgress(action = Request.Action.GET_FIRSTS))
            a_profile_repo_that_returns("username", ResultInProgress())
        } whenn {
            create_presenter()
        } then {
            should_call_repo_get_firsts_experiences("username")
            should_call_profile("username")
        }
    }

    @Test
    fun test_when_result_in_progress_last_event_get_firsts_shows_loader() {
        given {
            a_presenter("username")
            an_experience_repo_that_returns(ResultInProgress(action = Request.Action.GET_FIRSTS))
            a_profile_repo_that_returns("username", ResultInProgress())
        } whenn {
            create_presenter()
        } then {
            should_show_experiences_loader()
            should_hide_view_pagination_loader()
            should_hide_experiences_retry()
            should_hide_profile_retry()
            should_show_profile_loader()
        }
    }

    @Test
    fun test_when_result_in_progress_last_event_pagination_shows_pagination_loader() {
        given {
            a_presenter("username")
            an_experience_repo_that_returns(ResultInProgress(action = Request.Action.PAGINATE))
            a_profile_repo_that_returns("username", ResultInProgress())
        } whenn {
            create_presenter()
        } then {
            should_hide_experiences_loader()
            should_show_view_pagination_loader()
            should_hide_experiences_retry()
            should_hide_profile_retry()
            should_show_profile_loader()
        }
    }

    @Test
    fun test_when_result_success_shows_data() {
        given {
            a_presenter("username")
            an_experience_repo_that_returns(DummyExperiencesResultSuccess(listOf("3", "4")))
            a_profile_repo_that_returns("username", DummyProfileResult("other"))
        } whenn {
            create_presenter()
        } then {
            should_hide_experiences_loader()
            should_hide_experiences_retry()
            should_hide_view_pagination_loader()
            should_show_received_experiences(listOf(DummyExperience("3"), DummyExperience("4")))

            should_show_profile(DummyProfile("other"))
            should_hide_profile_retry()
            should_hide_profile_loader()
        }
    }

    @Test
    fun test_create_when_response_error_shows_retry_if_last_event_get_firsts() {
        given {
            a_presenter("username")
            an_experience_repo_that_returns(
                    ResultError(Exception(), action = Request.Action.GET_FIRSTS))
            a_profile_repo_that_returns("username", DummyResultError())
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
    fun test_create_when_response_error_does_nothing_if_last_event_pagination() {
        given {
            a_presenter("username")
            an_experience_repo_that_returns(
                    ResultError(Exception(), action = Request.Action.PAGINATE))
            a_profile_repo_that_returns("username", DummyResultError())
        } whenn {
            create_presenter()
        } then {
            should_hide_experiences_loader()
            should_hide_experiences_retry()
            should_hide_view_pagination_loader()
            should_hide_profile_loader()
            should_show_profile_retry()
        }
    }

    @Test
    fun test_on_retry_click_calls_get_firsts_experiences_again() {
        given {
            a_presenter("username")
            a_profile_repo_that_returns("username", DummyProfileResult("other"))
        } whenn {
            retry_clicked()
        } then {
            should_call_repo_get_firsts_experiences("username")
            should_show_profile(DummyProfile("other"))
        }
    }

    @Test
    fun test_experience_tapped() {
        given {
            a_presenter("username")
        } whenn {
            experience_click("2")
        } then {
            should_navigate_to_experience("2")
        }
    }

    @Test
    fun test_unsubscribe_on_destroy() {
        given {
            a_presenter("username")
            repos_that_return_test_observables("username")
        } whenn {
            create_presenter()
            destroy_presenter()
        } then {
            should_unsubscribe_observables()
        }
    }

    @Test
    fun test_last_experience_shown_calls_repo_get_more_with_persons_kind() {
        given {
            a_presenter("username")
        } whenn {
            last_experience_shown()
        } then {
            should_call_repo_get_more_experiences()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: ProfilePresenter
        @Mock lateinit var mockView: ProfileView
        @Mock lateinit var mockRepository: ExperienceRepository
        @Mock lateinit var mockProfileRepo: ProfileRepository
        lateinit var experienceTestObservable: PublishSubject<Result<List<Experience>>>
        lateinit var profileTestObservable: PublishSubject<Result<Profile>>

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = ProfilePresenter(mockRepository, mockProfileRepo,
                    Schedulers.trampoline())

            return this
        }

        fun a_presenter(username: String) {
            presenter.setViewAndUsername(mockView, username)
        }

        fun an_experience_repo_that_returns(result: Result<List<Experience>>) {
            BDDMockito.given(mockRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.PERSONS))
                    .willReturn(Flowable.just(result))
        }

        fun a_profile_repo_that_returns(username: String, result: Result<Profile>) {
            BDDMockito.given(mockProfileRepo.profile(username))
                    .willReturn(Flowable.just(result))
        }

        fun create_presenter() {
            presenter.create()
        }

        fun retry_clicked() {
            presenter.onRetryClick()
        }

        fun last_experience_shown() {
            presenter.lastExperienceShown()
        }

        fun experience_click(experienceId: String) {
            presenter.onExperienceClick(experienceId)
        }

        fun should_show_received_experiences(experiences: List<Experience>) {
            then(mockView).should().showExperienceList(experiences)
        }

        fun should_show_profile(profile: Profile) {
            BDDMockito.then(mockView).should().showProfile(profile)
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

        fun should_hide_experiences_retry() {
            then(mockView).should().hideExperiencesRetry()
        }

        fun should_call_repo_get_firsts_experiences(username: String) {
            then(mockRepository).should()
                    .getFirstExperiences(ExperienceRepoSwitch.Kind.PERSONS,
                                         Request.Params(username = username))
        }

        fun should_call_profile(username: String) {
            BDDMockito.then(mockProfileRepo).should().profile(username)
        }

        fun should_navigate_to_experience(experienceId: String) {
            then(mockView).should().navigateToExperienceWithFinishOnProfileClick(experienceId)
        }

        fun repos_that_return_test_observables(username: String) {
            experienceTestObservable = PublishSubject.create<Result<List<Experience>>>()
            assertFalse(experienceTestObservable.hasObservers())
            profileTestObservable = PublishSubject.create<Result<Profile>>()
            assertFalse(profileTestObservable.hasObservers())

            BDDMockito.given(mockRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.PERSONS))
                    .willReturn(experienceTestObservable.toFlowable(BackpressureStrategy.LATEST))
            BDDMockito.given(mockProfileRepo.profile(username))
                    .willReturn(profileTestObservable.toFlowable(BackpressureStrategy.LATEST))
        }

        fun destroy_presenter() {
            presenter.destroy()
        }

        fun should_unsubscribe_observables() {
            assertFalse(experienceTestObservable.hasObservers())
            assertFalse(profileTestObservable.hasObservers())
        }

        fun should_call_repo_get_more_experiences() {
            then(mockRepository).should().getMoreExperiences(ExperienceRepoSwitch.Kind.PERSONS)
        }

        fun should_hide_profile_retry() {
            BDDMockito.then(mockView).should().hideProfileRetry()
        }

        fun should_show_profile_loader() {
            BDDMockito.then(mockView).should().showProfileLoader()
        }

        fun should_hide_profile_loader() {
            BDDMockito.then(mockView).should().hideProfileLoader()
        }

        fun should_show_profile_retry() {
            BDDMockito.then(mockView).should().showProfileRetry()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
