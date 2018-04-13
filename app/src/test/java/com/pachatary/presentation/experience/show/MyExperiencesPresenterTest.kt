package com.pachatary.presentation.experience.show

import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.common.Result
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.NewExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
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
            an_auth_repo_that_returns_false_on_can_create_content()
        } whenn {
            create_presenter()
        } then {
            should_show_view_register_dialog()
        }
    }

    @Test
    fun test_create_asks_experiences_and_shows_if_can_create_content() {
        given {
            an_auth_repo_that_returns_true_on_can_create_content()
            an_experience()
            another_experience()
            an_experience_repo_that_returns_both_on_my_experiences_flowable()
        } whenn {
            create_presenter()
        } then {
            should_call_repo_get_firsts_experiences()
            should_show_received_experiences()
            should_hide_view_loader()
        }
    }

    @Test
    fun test_when_result_in_progress_shows_loader_if_can_create_content() {
        given {
            an_auth_repo_that_returns_true_on_can_create_content()
            an_experience_repo_that_returns_in_progress()
        } whenn {
            create_presenter()
        } then {
            should_show_view_loader()
            should_hide_view_retry()
        }
    }

    @Test
    fun test_resume_checks_if_can_create_content_has_changed_and_connects_to_experiences() {
        given {
            an_auth_repo_that_returns_true_on_can_create_content()
            an_experience()
            another_experience()
            an_experience_repo_that_returns_both_on_my_experiences_flowable()
        } whenn {
            resume_presenter()
        } then {
            should_call_repo_get_firsts_experiences()
            should_show_received_experiences()
            should_hide_view_loader()
        }
    }

    @Test
    fun test_resume_does_nothing_if_cannot_create_content() {
        given {
            an_auth_repo_that_returns_false_on_can_create_content()
        } whenn {
            resume_presenter()
        } then {
            should_do_nothing()
        }
    }

    @Test
    fun test_resume_does_nothing_if_already_connected_to_experiences_flowable() {
        given {
            an_auth_repo_that_returns_true_on_can_create_content()
            an_experience()
            another_experience()
            an_experience_repo_that_returns_both_on_my_experiences_flowable()
        } whenn {
            create_presenter()
        } then {
            should_call_repo_get_firsts_experiences()
            should_call_repo_my_experience_flowable()
            should_show_received_experiences()
            should_hide_view_loader()
            should_hide_view_retry()
        } whenn {
            resume_presenter()
        } then {
            should_do_nothing()
        }
    }

    @Test
    fun test_create_when_response_error_shows_retry() {
        given {
            an_auth_repo_that_returns_true_on_can_create_content()
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
            an_auth_repo_that_returns_true_on_can_create_content()
        } whenn {
            on_create_experience_click()
        } then {
            should_navigate_to_create_experience()
        }
    }

    @Test
    fun test_create_new_experience_button_click_if_cannot_create_content_shows_register_dialog() {
        given {
            an_auth_repo_that_returns_false_on_can_create_content()
        } whenn {
            on_create_experience_click()
        } then {
            should_show_view_register_dialog()
        }
    }

    @Test
    fun test_unsubscribe_on_destroy() {
        given {
            a_test_observable()
            an_experience_repo_that_returns_test_observable()
        } whenn {
            create_presenter()
            destroy_presenter()
        } then {
            should_unsubscribe_observable()
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

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: MyExperiencesPresenter
        @Mock lateinit var mockView: MyExperiencesView
        @Mock lateinit var mockExperiencesRepository: NewExperienceRepository
        @Mock lateinit var mockAuthRepository: AuthRepository
        lateinit var experienceA: Experience
        lateinit var experienceB: Experience
        lateinit var testObservable: PublishSubject<Result<List<Experience>>>

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = MyExperiencesPresenter(mockExperiencesRepository, mockAuthRepository, testSchedulerProvider)
            presenter.view = mockView

            return this
        }

        fun nothing() {}

        fun an_experience() {
            experienceA = Experience(id = "1", title = "A", description = "", picture = null)
        }

        fun another_experience() {
            experienceB = Experience(id = "2", title = "B", description = "", picture = null)
        }

        fun an_experience_repo_that_returns_both_on_my_experiences_flowable() {
            given(mockExperiencesRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.MINE))
                    .willReturn(Flowable.just(
                            Result<List<Experience>>(arrayListOf(experienceA, experienceB))))
        }

        fun an_experience_repo_that_returns_exception() {
            given(mockExperiencesRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.MINE))
                    .willReturn(Flowable.just(Result<List<Experience>>(null, error = Exception())))
        }

        fun an_experience_repo_that_returns_in_progress() {
            given(mockExperiencesRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.MINE))
                    .willReturn(Flowable.just(Result<List<Experience>>(null, inProgress = true)))
        }

        fun an_auth_repo_that_returns_true_on_can_create_content() {
            BDDMockito.given(mockAuthRepository.canPersonCreateContent()).willReturn(true)
        }

        fun an_auth_repo_that_returns_false_on_can_create_content() {
            BDDMockito.given(mockAuthRepository.canPersonCreateContent()).willReturn(false)
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

        fun on_create_experience_click() {
            presenter.onCreateExperienceClick()
        }

        fun experience_click(experienceId: String) {
            presenter.onExperienceClick(experienceId)
        }

        fun dont_proceed_to_register() {
            presenter.onDontProceedToRegister()
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

        fun a_test_observable() {
            testObservable = PublishSubject.create<Result<List<Experience>>>()
            assertFalse(testObservable.hasObservers())
        }

        fun an_experience_repo_that_returns_test_observable() {
            given(mockExperiencesRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.MINE))
                    .willReturn(testObservable.toFlowable(BackpressureStrategy.LATEST))
        }

        fun destroy_presenter() {
            presenter.destroy()
        }

        fun should_unsubscribe_observable() {
            assertFalse(testObservable.hasObservers())
        }

        fun should_show_view_register_dialog() {
            BDDMockito.then(mockView).should().showRegisterDialog()
        }

        fun should_do_nothing() {
            BDDMockito.then(mockView).shouldHaveNoMoreInteractions()
            BDDMockito.then(mockExperiencesRepository).shouldHaveNoMoreInteractions()
        }

        fun should_call_repo_my_experience_flowable() {
            BDDMockito.then(mockExperiencesRepository).should()
                    .experiencesFlowable(ExperienceRepoSwitch.Kind.MINE)
        }

        fun should_navigate_to_register() {
            BDDMockito.then(mockView).should().navigateToRegister()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
