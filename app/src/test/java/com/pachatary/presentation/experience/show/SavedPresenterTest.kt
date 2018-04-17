package com.pachatary.presentation.experience.show

import com.pachatary.data.common.Result
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
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

class SavedPresenterTest {

    @Test
    fun test_create_asks_firsts_experiences() {
        given {
            an_experience_repo_that_returns_in_progress(Result.Event.GET_FIRSTS)
        } whenn {
            create_presenter()
        } then {
            should_call_repo_get_firsts_experiences()
        }
    }

    @Test
    fun test_when_result_in_progress_last_event_get_firsts_shows_loader() {
        given {
            an_experience_repo_that_returns_in_progress(lastEvent = Result.Event.GET_FIRSTS)
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
            an_experience_repo_that_returns_in_progress(lastEvent = Result.Event.PAGINATE)
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
            an_experience_repo_that_returns_exception(lastEvent = Result.Event.GET_FIRSTS)
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
            an_experience_repo_that_returns_exception(lastEvent = Result.Event.PAGINATE)
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

    @Test
    fun test_last_experience_shown_calls_repo_get_more_with_saved_kind() {
        given {
            nothing()
        } whenn {
            last_experience_shown()
        } then {
            should_call_repo_get_more_experiences()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: SavedPresenter
        @Mock lateinit var mockView: SavedView
        @Mock lateinit var mockRepository: ExperienceRepository
        lateinit var experienceA: Experience
        lateinit var experienceB: Experience
        lateinit var testObservable: PublishSubject<Result<List<Experience>>>

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = SavedPresenter(mockRepository, Schedulers.trampoline())
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

        fun an_experience_repo_that_returns_in_progress(lastEvent: Result.Event) {
            BDDMockito.given(mockRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.SAVED))
                    .willReturn(Flowable.just(Result<List<Experience>>(null, inProgress = true,
                            lastEvent = lastEvent)))
        }

        fun an_experience_repo_that_returns_both_on_my_experiences_flowable() {
            BDDMockito.given(mockRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.SAVED))
                    .willReturn(Flowable.just(Result<List<Experience>>(
                            arrayListOf(experienceA, experienceB))))
        }

        fun an_experience_repo_that_returns_exception(lastEvent: Result.Event) {
            BDDMockito.given(mockRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.SAVED))
                    .willReturn(Flowable.just(Result<List<Experience>>(null, error = Exception(),
                            lastEvent = lastEvent)))
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

        fun should_show_received_experiences() {
            then(mockView).should().showExperienceList(arrayListOf(experienceA, experienceB))
        }

        fun should_show_view_loader() {
            then(mockView).should().showLoader()
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
            then(mockRepository).should().getFirstExperiences(ExperienceRepoSwitch.Kind.SAVED)
        }

        fun should_navigate_to_experience(experienceId: String) {
            then(mockView).should().navigateToExperience(experienceId)
        }

        fun a_test_observable() {
            testObservable = PublishSubject.create<Result<List<Experience>>>()
            assertFalse(testObservable.hasObservers())
        }

        fun an_experience_repo_that_returns_test_observable() {
            BDDMockito.given(mockRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.SAVED))
                    .willReturn(testObservable.toFlowable(BackpressureStrategy.LATEST))
        }

        fun destroy_presenter() {
            presenter.destroy()
        }

        fun should_unsubscribe_observable() {
            assertFalse(testObservable.hasObservers())
        }

        fun should_call_repo_get_more_experiences() {
            then(mockRepository).should().getMoreExperiences(ExperienceRepoSwitch.Kind.SAVED)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
