package com.pachatary.presentation.common.edition

import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class EditTitleAndDescriptionPresenterTest {

    @Test
    fun presenter_sets_to_the_view_initial_values() {
        given {
            an_initial_title()
            an_initial_description()
            a_presenter_instantiated_with_view_and_initial_values()
        } whenn {
            presenter_is_created()
        } then {
            presenter_should_set_initial_title_and_description_to_the_view()
        }
    }

    @Test
    fun no_title_should_show_error() {
        given {
            no_title()
        } whenn {
            done_button_is_clicked()
        } then {
            error_should_be_shown()
        }
    }

    @Test
    fun title_too_large_should_show_error() {
        given {
            too_large_title()
        } whenn {
            done_button_is_clicked()
        } then {
            error_should_be_shown()
        }
    }

    @Test
    fun correct_title_sends_title_and_description() {
        given {
            correct_length_title()
            some_description()
        } whenn {
            done_button_is_clicked()
        } then {
            finish_should_be_called_with_title_and_description()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: EditTitleAndDescriptionPresenter
        @Mock private lateinit var mockView: EditTitleAndDescriptionView
        private var initialTitle = ""
        private var initialDescription = ""
        private var title = ""
        private var description = ""

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = EditTitleAndDescriptionPresenter()
            presenter.view = mockView

            return this
        }

        fun an_initial_title() {
            initialTitle = "some"
        }

        fun an_initial_description() {
            initialDescription = "other"
        }

        fun no_title() {
            title = ""
        }

        fun too_large_title() {
            title = "*".repeat(31)
        }

        fun correct_length_title() {
            title = "correct length title"
        }

        fun some_description() {
            description = "some description"
        }

        fun a_presenter_instantiated_with_view_and_initial_values() {
            presenter.setViewAndInitialTitleAndDescription(mockView, initialTitle, initialDescription)
        }

        fun done_button_is_clicked() {
            given(mockView.title()).willReturn(title)
            given(mockView.description()).willReturn(description)
            presenter.doneButtonClick()
        }

        fun presenter_is_created() {
            presenter.create()
        }

        fun finish_should_be_called_with_title_and_description() {
            then(mockView).should().finishWith(title, description)
        }

        fun error_should_be_shown() {
            then(mockView).should().showTitleLengthError()
        }

        fun presenter_should_set_initial_title_and_description_to_the_view() {
            then(mockView).should().setTitleAndDescription(initialTitle, initialDescription)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
