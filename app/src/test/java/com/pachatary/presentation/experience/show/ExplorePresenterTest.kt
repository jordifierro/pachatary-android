package com.pachatary.presentation.experience.show

import com.pachatary.data.common.*
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
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class ExplorePresenterTest {

    @Test
    fun test_create_when_no_permissions_asks_permissions() {
        given {
            no_permissions()
            an_experience_repo_that_returns_in_progress(Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
        } then {
            should_ask_permissions()
        }
    }

    @Test
    fun test_create_with_permissions_asks_last_known_location() {
        given {
            permissions()
            an_experience_repo_that_returns_in_progress(Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
        } then {
            should_ask_last_known_location()
        }
    }

    @Test
    fun test_when_permisions_accepted_shows_views_and_asks_last_known_location() {
        given {
            nothing()
        } whenn {
            permissions_accepted()
        } then {
            should_ask_last_known_location()
        }
    }

    @Test
    fun test_create_dont_ask_firsts_experiences() {
        given {
            an_experience_repo_that_returns_in_progress(Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
        } then {
            should_call_repo_explore_experiences()
            should_not_call_repo_get_firsts_experiences()
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
    fun test_create_with_last_location_known_ask_firsts_experiences() {
        given {
            an_experience_repo_that_returns_in_progress(Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
            last_location_known(latitude = 1.3, longitude = -4.5)
        } then {
            should_call_repo_get_firsts_experiences(text = null, latitude = 1.3, longitude = -4.5)
        }
    }

    @Test
    fun test_create_with_last_location_not_found_ask_firsts_experiences_without_location() {
        given {
            an_experience_repo_that_returns_in_progress(Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
            last_location_not_found()
        } then {
            should_call_repo_get_firsts_experiences()
        }
    }

    @Test
    fun test_on_retry_click_with_last_location_calls_get_firsts_experiences_again() {
        given {
            nothing()
        } whenn {
            retry_clicked()
            last_location_known(latitude = 1.3, longitude = -4.5)
        } then {
            should_call_repo_get_firsts_experiences(text = null, latitude = 1.3, longitude = -4.5)
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
            should_show_empty_experiences()
            should_hide_view_pagination_loader()
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
            should_hide_view_pagination_loader()
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
    fun test_on_search_button_click_get_firsts_experience_with_search_text() {
        given {
            nothing()
        } whenn {
            search_button_click_with_text("museums")
        } then {
            should_call_repo_get_firsts_experiences(text = "museums")
        }
    }

    @Test
    fun test_on_location_click_navigates_to_select_location_with_previous_latitude_and_longitude() {
        given {
            last_location_known(5.4, -9.0)
        } whenn {
            location_click()
        } then {
            should_navigate_to_select_location_with(5.4, -9.0)
        }
    }

    @Test
    fun test_on_location_selected_get_firsts_experiences_with_that_location() {
        given {
            last_location_known(5.4, -9.0)
        } whenn {
            location_selected(0.1, 3.3)
        } then {
            should_call_repo_get_firsts_experiences(latitude = 0.1, longitude = 3.3)
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: ExplorePresenter
        @Mock lateinit var mockView: ExploreView
        @Mock
        private lateinit var mockRepository: ExperienceRepository
        private lateinit var experienceA: Experience
        private lateinit var experienceB: Experience
        private lateinit var testObservable: PublishSubject<Result<List<Experience>>>

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
                    .willReturn(Flowable.just(ResultSuccess(listOf(experienceA, experienceB))))
        }

        fun an_experience_repo_that_returns_in_progress(action: Request.Action) {
            BDDMockito.given(mockRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.EXPLORE))
                    .willReturn(Flowable.just(ResultInProgress(listOf(), action = action)))
        }

        fun an_experience_repo_that_returns_exception(action: Request.Action) {
            BDDMockito.given(mockRepository.experiencesFlowable(ExperienceRepoSwitch.Kind.EXPLORE))
                .willReturn(Flowable.just(
                        ResultError(Exception(), data = listOf(), action = action)))
        }

        fun permissions() {
            BDDMockito.given(mockView.hasLocationPermission()).willReturn(true)
        }

        fun no_permissions() {
            BDDMockito.given(mockView.hasLocationPermission()).willReturn(false)
        }

        fun create_presenter() {
            presenter.create()
        }

        fun retry_clicked() {
            presenter.onRetryClick()
        }

        fun last_location_known(latitude: Double, longitude: Double) {
            presenter.onLastLocationFound(latitude, longitude)
        }

        fun last_location_not_found() {
            presenter.onLastLocationNotFound()
        }

        fun experience_click(experienceId: String) {
            presenter.onExperienceClick(experienceId)
        }

        fun permissions_accepted() {
            presenter.onPermissionsAccepted()
        }

        fun search_button_click_with_text(text: String) {
            presenter.searchClick(text)
        }

        fun location_click() {
            presenter.locationClick()
        }

        fun location_selected(latitude: Double, longitude: Double) {
            presenter.onLocationSelected(latitude, longitude)
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

        fun should_call_repo_get_firsts_experiences(text: String? = null,
                                                    latitude: Double? = null,
                                                    longitude: Double? = null) {
            then(mockRepository).should()
                    .getFirstExperiences(ExperienceRepoSwitch.Kind.EXPLORE,
                                         Request.Params(text, latitude, longitude))
        }

        fun should_show_empty_experiences() {
            BDDMockito.then(mockView).should().showExperienceList(listOf())
        }

        fun should_call_repo_explore_experiences() {
            BDDMockito.then(mockRepository).should()
                    .experiencesFlowable(ExperienceRepoSwitch.Kind.EXPLORE)
        }

        fun should_not_call_repo_get_firsts_experiences() {
            Mockito.verifyNoMoreInteractions(mockRepository)
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

        fun should_ask_permissions() {
            BDDMockito.then(mockView).should().askPermissions()
        }

        fun should_ask_last_known_location() {
            BDDMockito.then(mockView).should().askLastKnownLocation()
        }

        fun should_navigate_to_select_location_with(latitude: Double, longitude: Double) {
            BDDMockito.then(mockView).should().navigateToSelectLocation(latitude, longitude)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
