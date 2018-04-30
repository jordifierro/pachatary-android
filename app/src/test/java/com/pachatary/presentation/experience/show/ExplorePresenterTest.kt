package com.pachatary.presentation.experience.show

import com.pachatary.data.common.Request
import com.pachatary.data.common.Result
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
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

class ExplorePresenterTest {

    @Test
    fun test_create_asks_firsts_experiences() {
        given {
            an_experience_repo_that_returns_in_progress(Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
        } then {
            should_call_repo_get_firsts_experiences()
        }
    }

    @Test
    fun test_when_result_in_progress_last_event_get_firsts_shows_loader() {
        given {
            an_experience_repo_that_returns_in_progress(action = Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
        } then {
            should_show_view_loader()
            should_hide_view_pagination_loader()
            should_hide_view_retry()
        }
    }


    @Test
    fun test_when_result_in_progress_last_event_pagination_shows_pagination_loader() {
        given {
            an_experience_repo_that_returns_in_progress(action = Request.Action.PAGINATE)
        } whenn {
            create_presenter()
        } then {
            should_hide_view_loader()
            should_show_view_pagination_loader()
            should_hide_view_retry()
        }
    }

    @Test
    fun test_when_result_success_shows_data() {
        given {
            an_experience()
            another_experience()
            an_experience_repo_that_returns_both_on_my_experiences_flowable()
        } whenn {
            create_presenter()
        } then {
            should_hide_view_loader()
            should_hide_view_retry()
            should_hide_view_pagination_loader()
            should_show_received_experiences()
        }

    }


    @Test
    fun test_create_when_response_error_shows_retry_if_last_event_get_firsts() {
        given {
            an_experience_repo_that_returns_exception(action = Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
        } then {
            should_hide_view_loader()
            should_hide_view_pagination_loader()
            should_show_view_retry()
        }
    }

    @Test
    fun test_create_when_response_error_does_nothing_if_last_event_pagination() {
        given {
            an_experience_repo_that_returns_exception(action = Request.Action.PAGINATE)
        } whenn {
            create_presenter()
        } then {
            should_hide_view_loader()
            should_hide_view_retry()
            should_hide_view_pagination_loader()
        }
    }

    @Test
    fun test_on_retry_click_calls_get_firsts_experiences_again() {
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

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: ExplorePresenter
        @Mock lateinit var mockView: ExploreView
        @Mock lateinit var mockRepository: ExperienceRepository
        lateinit var experienceA: Experience
        lateinit var experienceB: Experience
        lateinit var testObservable: PublishSubject<Result<List<Experience>>>

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider =
                    SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = ExplorePresenter(mockRepository, testSchedulerProvider)
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
            BDDMockito.given(
                    mockRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.EXPLORE))
                    .willReturn(Flowable.just(Result<List<Experience>>(
                            arrayListOf(experienceA, experienceB))))
        }

        fun an_experience_repo_that_returns_in_progress(action: Request.Action) {
            BDDMockito.given(mockRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.EXPLORE))
                    .willReturn(Flowable.just(Result(listOf(), inProgress = true, action = action)))
        }

        fun an_experience_repo_that_returns_exception(action: Request.Action) {
            BDDMockito.given(mockRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.EXPLORE))
                .willReturn(Flowable.just(Result(listOf(), error = Exception(), action = action)))
        }

        fun create_presenter() {
            presenter.create()
        }

        fun retry_clicked() {
            presenter.onRetryClick()
        }

        fun experience_click(experienceId: String) {
            presenter.onExperienceClick(experienceId)
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

        fun should_show_view_pagination_loader() {
            then(mockView).should().showPaginationLoader()
        }

        fun should_hide_view_pagination_loader() {
            then(mockView).should().hidePaginationLoader()
        }

        fun should_show_view_retry() {
            then(mockView).should().showRetry()
        }

        fun should_hide_view_retry() {
            then(mockView).should().hideRetry()
        }

        fun should_call_repo_get_firsts_experiences() {
            then(mockRepository).should().getFirstExperiences(ExperienceRepoSwitch.Kind.EXPLORE)
        }

        fun should_navigate_to_experience(experienceId: String) {
            then(mockView).should().navigateToExperience(experienceId)
        }

        fun a_test_observable() {
            testObservable = PublishSubject.create<Result<List<Experience>>>()
            assertFalse(testObservable.hasObservers())
        }

        fun an_experience_repo_that_returns_test_observable() {
            BDDMockito.given(mockRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.EXPLORE))
                    .willReturn(testObservable.toFlowable(BackpressureStrategy.LATEST))
        }

        fun destroy_presenter() {
            presenter.destroy()
        }

        fun should_unsubscribe_observable() {
            assertFalse(testObservable.hasObservers())
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
