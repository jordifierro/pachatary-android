package com.abidria.presentation.experience.show

import com.abidria.data.auth.AuthRepository
import com.abidria.data.auth.AuthToken
import com.abidria.data.common.Result
import com.abidria.data.experience.Experience
import com.abidria.data.experience.ExperienceRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class MyExperiencesPresenterTest {

    @Test
    fun test_create_asks_experiences_and_ask_invitation_if_not_has_credentials() {
        given {
            an_auth_repo_has_no_credentials()
            an_auth_repo_returns_auth_token_on_get_person_invitation()
            an_experience()
            another_experience()
            an_experience_repo_that_returns_both_on_my_experiences_flowable()
        } whenn {
            create_presenter()
        } then {
            should_call_auth_repo_has_person_credentials()
            should_call_auth_repo_get_person_invitation()
            should_show_view_loader()
            should_show_received_experiences()
            should_hide_view_loader()
        }
    }

    @Test
    fun test_create_asks_experiences_and_shows_if_already_has_credentials() {
        given {
            an_auth_repo_has_credentials()
            an_experience()
            another_experience()
            an_experience_repo_that_returns_both_on_my_experiences_flowable()
        } whenn {
            create_presenter()
        } then {
            should_show_view_loader()
            should_show_received_experiences()
            should_hide_view_loader()
        }
    }

    @Test
    fun test_create_when_response_error_shows_retry() {
        given {
            an_auth_repo_has_credentials()
            an_experience_repo_that_returns_exception()
        } whenn {
            create_presenter()
        } then {
            should_hide_view_loader()
            should_show_view_retry()
        }
    }

    @Test
    fun test_on_retry_click_retrive_experiences_and_shows_them() {
        given {
            nothing()
        } whenn {
            retry_clicked()
        } then {
            should_hide_view_retry()
            should_show_view_loader()
            should_call_repo_refresh_experiences()
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
    fun test_create_new_experience_button_click() {
        given {
            nothing()
        } whenn {
            on_create_experience_click()
        } then {
            should_navigate_to_create_experience()
        }
    }

    @Test
    fun test_unsubscribe_on_destroy() {
        given {
            an_auth_repo_has_credentials()
            a_test_observable()
            an_experience_repo_that_returns_test_observable()
        } whenn {
            create_presenter()
            destroy_presenter()
        } then {
            should_unsubscribe_observable()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: MyExperiencesPresenter
        @Mock lateinit var mockView: MyExperiencesView
        @Mock lateinit var mockRepository: ExperienceRepository
        @Mock lateinit var mockAuthRepository: AuthRepository
        lateinit var experienceA: Experience
        lateinit var experienceB: Experience
        lateinit var testObservable: PublishSubject<Result<List<Experience>>>

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = MyExperiencesPresenter(mockRepository, mockAuthRepository, testSchedulerProvider)
            presenter.view = mockView

            return this
        }

        fun nothing() {}

        fun an_auth_repo_has_no_credentials() {
            given(mockAuthRepository.hasPersonCredentials()).willReturn(false)
        }

        fun an_auth_repo_has_credentials() {
            given(mockAuthRepository.hasPersonCredentials()).willReturn(true)
        }

        fun an_auth_repo_returns_auth_token_on_get_person_invitation() {
            given(mockAuthRepository.getPersonInvitation()).willReturn(
                    Flowable.just(Result(AuthToken("A", "R"), null)))
        }

        fun an_experience() {
            experienceA = Experience(id = "1", title = "A", description = "", picture = null)
        }

        fun another_experience() {
            experienceB = Experience(id = "2", title = "B", description = "", picture = null)
        }

        fun an_experience_repo_that_returns_both_on_my_experiences_flowable() {
            given(mockRepository.myExperiencesFlowable())
                    .willReturn(Flowable.just(Result<List<Experience>>(arrayListOf(experienceA, experienceB), null)))
        }

        fun an_experience_repo_that_returns_exception() {
            given(mockRepository.myExperiencesFlowable())
                    .willReturn(Flowable.just(Result<List<Experience>>(null, Exception())))
        }

        fun create_presenter() {
            presenter.create()
        }

        fun retry_clicked() {
            presenter.onRetryClick()
        }

        fun on_create_experience_click() {
            presenter.onCreateExperienceClick()
        }

        fun experience_click(experienceId: String) {
            presenter.onExperienceClick(experienceId)
        }

        fun should_call_auth_repo_has_person_credentials() {
            then(mockAuthRepository).should().hasPersonCredentials()
        }

        fun should_call_auth_repo_get_person_invitation() {
            then(mockAuthRepository).should().getPersonInvitation()
        }

        fun should_show_view_loader() {
            then(mockView).should().showLoader()
        }

        fun should_show_received_experiences() {
            then(mockView).should().showExperienceList(arrayListOf(experienceA, experienceB))
        }

        fun should_hide_view_loader() {
            then(mockView).should().hideLoader()
        }

        fun should_show_view_retry() {
            then(mockView).should().showRetry()
        }

        fun should_hide_view_retry() {
            then(mockView).should().hideRetry()
        }

        fun should_call_repo_refresh_experiences() {
            then(mockRepository).should().refreshExperiences()
        }

        fun should_navigate_to_experience(experienceId: String) {
            then(mockView).should().navigateToExperience(experienceId)
        }

        fun should_navigate_to_create_experience() {
            then(mockView).should().navigateToCreateExperience()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
        fun a_test_observable() {
            testObservable = PublishSubject.create<Result<List<Experience>>>()
            assertFalse(testObservable.hasObservers())
        }

        fun an_experience_repo_that_returns_test_observable() {
            given(mockRepository.myExperiencesFlowable()).willReturn(testObservable.toFlowable(BackpressureStrategy.LATEST))
        }

        fun destroy_presenter() {
            presenter.destroy()
        }

        fun should_unsubscribe_observable() {
            assertFalse(testObservable.hasObservers())
        }
    }
}
