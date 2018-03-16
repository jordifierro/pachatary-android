package com.pachatary.presentation.common.edition

import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SelectLocationPresenterTest {

    @Test
    fun on_unknown_initial_location_presenter_should_set_far_zoom_level() {
        given {
            an_unknown_initial_location()
            presenter_initialized_with_this_location()
        } whenn {
            presenter_is_created()
        } then {
            presenter_should_set_view_initial_location_with_far_zoom_level()
        }
    }

    @Test
    fun on_aprox_initial_location_presenter_should_set_mid_zoom_level() {
        given {
            an_aprox_initial_location()
            presenter_initialized_with_this_location()
        } whenn {
            presenter_is_created()
        } then {
            presenter_should_set_view_initial_location_with_mid_zoom_level()
        }
    }

    @Test
    fun on_specific_initial_location_presenter_should_set_near_zoom_level() {
        given {
            an_specific_initial_location()
            presenter_initialized_with_this_location()
        } whenn {
            presenter_is_created()
        } then {
            presenter_should_set_view_initial_location_with_near_zoom_level()
        }
    }

    @Test
    fun done_button_calls_finish_with_latitude_and_longitude() {
        given {
            an_unknown_initial_location()
            presenter_initialized_with_this_location()
            a_latitude()
            a_longitude()
        } whenn {
            done_button_is_clicked()
        } then {
            finish_should_be_called_with_latitude_and_longitude()
        }
    }

    @Test
    fun test_when_search_location_button_clicked_subscribe_to_geocoder() {
        given {
            an_unknown_initial_location()
            presenter_initialized_with_this_location()
            a_latitude()
            a_longitude()
            an_address()
            an_observable_that_returns_that_latitude_and_longitude_when_geocoder_called_with_that_address()
        } whenn {
            search_button_is_clicked_with_that_address()
        } then {
            geocoder_should_be_called_with_that_address()
            view_should_move_map_to_that_latitude_and_longitude_point()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var presenter: SelectLocationPresenter
        @Mock lateinit var mockView: SelectLocationView
        var initialLatitude = 0.0
        var initialLongitude = 0.0
        var initialLocationType = SelectLocationPresenter.LocationType.UNKNWON
        var latitude = 0.0
        var longitude = 0.0
        var address = ""
        lateinit var geocoderFlowable: Flowable<Pair<Double, Double>>

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = SelectLocationPresenter(SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline()))

            return this
        }

        fun a_latitude() {
            latitude = 2.3
        }

        fun a_longitude() {
            longitude = -6.7
        }

        fun an_unknown_initial_location() {
            initialLocationType = SelectLocationPresenter.LocationType.UNKNWON
        }

        fun an_aprox_initial_location() {
            initialLocationType = SelectLocationPresenter.LocationType.APROX
            initialLatitude = 4.2
            initialLongitude -4.2
        }

        fun an_specific_initial_location() {
            initialLocationType = SelectLocationPresenter.LocationType.SPECIFIC
            initialLatitude = 3.2
            initialLongitude -3.2
        }

        fun an_observable_that_returns_that_latitude_and_longitude_when_geocoder_called_with_that_address() {
            geocoderFlowable = Flowable.just(Pair(latitude, longitude))
            BDDMockito.given(mockView.geocodeAddress(address)).willReturn(geocoderFlowable)
        }

        fun an_address() {
            address = "My point address, 123"
        }

        fun presenter_initialized_with_this_location() {
            presenter = SelectLocationPresenter(SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline()))
            presenter.setViewAndInitialLocation(mockView, initialLatitude, initialLongitude, initialLocationType)
        }

        fun presenter_is_created() {
            presenter.create()
        }

        fun done_button_is_clicked() {
            BDDMockito.given(mockView.latitude()).willReturn(latitude)
            BDDMockito.given(mockView.longitude()).willReturn(longitude)
            presenter.doneButtonClick()
        }

        fun search_button_is_clicked_with_that_address() {
            presenter.searchButtonClick(address)
        }

        fun finish_should_be_called_with_latitude_and_longitude() {
            BDDMockito.then(mockView).should().finishWith(latitude, longitude)
        }

        fun presenter_should_set_view_initial_location_with_far_zoom_level() {
            BDDMockito.then(mockView).should().setInitialLocation(initialLatitude, initialLongitude,
                                                                  SelectLocationView.ZoomLevel.FAR)
        }

        fun presenter_should_set_view_initial_location_with_mid_zoom_level() {
            BDDMockito.then(mockView).should().setInitialLocation(initialLatitude, initialLongitude,
                                                                  SelectLocationView.ZoomLevel.MID)
        }

        fun presenter_should_set_view_initial_location_with_near_zoom_level() {
            BDDMockito.then(mockView).should().setInitialLocation(initialLatitude, initialLongitude,
                                                                  SelectLocationView.ZoomLevel.NEAR)
        }

        fun geocoder_should_be_called_with_that_address() {
            BDDMockito.then(mockView).should().geocodeAddress(address)
        }

        fun view_should_move_map_to_that_latitude_and_longitude_point() {
            BDDMockito.then(mockView).should().moveMapToPoint(latitude, longitude)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}