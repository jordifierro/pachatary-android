package com.abidria.presentation.scene.create

import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SelectLocationPresenterTest {

    @Test
    fun done_button_calls_finish_with_latitude_and_longitude() {
        given {
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
        private lateinit var presenter: SelectLocationPresenter
        @Mock private lateinit var mockView: SelectLocationView
        private var latitude = 0.0
        private var longitude = 0.0

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = SelectLocationPresenter()
            presenter.view = mockView

            return this
        }

        fun a_latitude() {
            latitude = 2.3
        }

        fun a_longitude() {
            longitude = -6.7
        }

        fun done_button_is_clicked() {
            BDDMockito.given(mockView.latitude()).willReturn(latitude)
            BDDMockito.given(mockView.longitude()).willReturn(longitude)
            presenter.doneButtonClick()
        }

        fun finish_should_be_called_with_latitude_and_longitude() {
            BDDMockito.then(mockView).should().finishWith(latitude, longitude)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}