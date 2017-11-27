package com.abidria.presentation.common.view.edition

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

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var presenter: SelectLocationPresenter
        @Mock lateinit var mockView: SelectLocationView
        var initialLatitude = 0.0
        var initialLongitude = 0.0
        var initialLocationType = SelectLocationPresenter.LocationType.UNKNWON
        var latitude = 0.0
        var longitude = 0.0

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = SelectLocationPresenter()

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

        fun presenter_initialized_with_this_location() {
            presenter = SelectLocationPresenter()
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

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}