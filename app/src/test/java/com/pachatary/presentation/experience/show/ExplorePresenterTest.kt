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
    fun test_create_with_permissions_show_views_and_ask_last_known_location() {
        given {
            permissions()
            an_experience_repo_that_returns_in_progress(Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
        } then {
            should_show_accepted_permissions_views()
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
            should_show_accepted_permissions_views()
            should_ask_last_known_location()
        }
    }

    @Test
    fun test_on_permissions_denied_shows_no_permissions_views() {
        given {
            nothing()
        } whenn {
            permissions_denied()
        } then {
            should_show_denied_permissions_views()
        }
    }

    @Test
    fun test_on_retry_permissions_click_shows_permissions_dialog() {
        given {
            nothing()
        } whenn {
            retry_permissions_clicked()
        } then {
            should_ask_permissions()
        }
    }

    @Test
    fun test_create_dont_ask_firsts_experiences() {
        given {
            an_experience_repo_that_returns_in_progress(Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
        } then {
            should_not_call_repo_get_firsts_experiences()
        }
    }

    @Test
    fun test_on_retry_click_dont_calls_get_firsts_experiences_again() {
        given {
            nothing()
        } whenn {
            retry_clicked()
        } then {
            should_not_call_repo_get_firsts_experiences()
        }
    }

    @Test
    fun test_create_with_last_location_known_ask_firsts_experiences() {
        given {
            an_experience_repo_that_returns_in_progress(Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
            last_location_known()
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
            last_location_known()
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
            should_show_empty_experiences()
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
    fun test_last_location_found_and_search_click_navigates_with_correct_params() {
        given {
            an_experience_repo_that_returns_in_progress(Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
            last_location_known()
            on_search_click()
        } then {
            should_call_repo_get_firsts_experiences()
            should_navigate_with_location_option_current_and_current_locations()
        }
    }

    @Test
    fun test_search_settings_result_uses_it_to_call_api_and_navigate_to_search_again_if_search() {
        given {
            an_experience_repo_that_returns_in_progress(Request.Action.GET_FIRSTS)
        } whenn {
            create_presenter()
            last_location_known()
            a_search_word()
            a_selected_latitude_and_longitude()
            on_search_result()
            on_search_click()
        } then {
            should_call_repo_get_firsts_experiences_and_again_with_new_search_settings()
            should_navigate_with_new_settings()
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
        val latitude = 4.8
        val longitude = -0.3
        lateinit var searchWord: String
        var selectedLatitude: Double = 0.0
        var selectedLongitude: Double = 0.0

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider =
                    SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = ExplorePresenter(mockRepository, testSchedulerProvider)
            presenter.view = mockView

            return this
        }

        fun nothing() {}

        fun a_search_word() {
            searchWord = "culture"
        }

        fun a_selected_latitude_and_longitude() {
            selectedLatitude = 0.12
            selectedLongitude = -1.99
        }

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

        fun last_location_known() {
            presenter.onLastLocationFound(latitude, longitude)
        }

        fun experience_click(experienceId: String) {
            presenter.onExperienceClick(experienceId)
        }

        fun on_search_click() {
            presenter.onSearchClick()
        }

        fun on_search_result() {
            presenter.onSearchSettingsResult(
                    SearchSettingsModel(searchWord, SearchSettingsModel.LocationOption.SELECTED,
                            latitude, longitude, selectedLatitude, selectedLongitude))
        }

        fun permissions_accepted() {
            presenter.onPermissionsAccepted()
        }

        fun permissions_denied() {
            presenter.onPermissionsDenied()
        }

        fun retry_permissions_clicked() {
            presenter.onRetryPermissions()
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
            then(mockRepository).should()
                    .getFirstExperiences(ExperienceRepoSwitch.Kind.EXPLORE,
                                         Request.Params(null, latitude, longitude))
        }

        fun should_show_empty_experiences() {
            BDDMockito.then(mockView).should().showExperienceList(listOf())
        }

        fun should_not_call_repo_get_firsts_experiences() {
            then(mockRepository).should(Mockito.never())
                    .getFirstExperiences(ExperienceRepoSwitch.Kind.EXPLORE,
                                         Request.Params(null, latitude, longitude))
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

        fun should_navigate_with_location_option_current_and_current_locations() {
            BDDMockito.then(mockView).should().navigateToSearchSettings(
                    SearchSettingsModel("", SearchSettingsModel.LocationOption.CURRENT,
                            latitude, longitude, latitude, longitude)
            )
        }

        fun should_call_repo_get_firsts_experiences_and_again_with_new_search_settings() {
            then(mockRepository).should()
                    .getFirstExperiences(ExperienceRepoSwitch.Kind.EXPLORE,
                            Request.Params(searchWord, selectedLatitude, selectedLongitude))
        }

        fun should_navigate_with_new_settings() {
            BDDMockito.then(mockView).should().navigateToSearchSettings(
                    SearchSettingsModel(searchWord, SearchSettingsModel.LocationOption.SELECTED,
                            latitude, longitude, selectedLatitude, selectedLongitude)
            )
        }

        fun should_ask_permissions() {
            BDDMockito.then(mockView).should().askPermissions()
        }

        fun should_show_accepted_permissions_views() {
            BDDMockito.then(mockView).should().showAcceptedPermissionsViews()
        }

        fun should_ask_last_known_location() {
            BDDMockito.then(mockView).should().askLastKnownLocation()
        }

        fun should_show_denied_permissions_views() {
            BDDMockito.then(mockView).should().showDeniedPermissionsViews()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
