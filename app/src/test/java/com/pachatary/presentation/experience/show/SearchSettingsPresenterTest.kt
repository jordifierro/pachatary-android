package com.pachatary.presentation.experience.show

import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SearchSettingsPresenterTest {

    @Test
    fun test_when_created_shows_search_text_and_option_button() {
        given {
            nothing()
        } whenn {
            create_presenter()
        } then {
            should_call_view_set_search_text()
            should_call_view_set_option_button_selected()
        }
    }

    @Test
    fun test_on_go_click_finishes_with_model() {
        given {
            nothing()
        } whenn {
            go_click()
        } then {
            should_call_view_finish_with_result_initial_model()
        }
    }

    @Test
    fun test_on_text_search_changed_finishes_with_new_search_text() {
        given {
            a_search_text()
        } whenn {
            search_text_changed()
            go_click()
        } then {
            should_call_view_finish_with_result_model_modified_with_search_text()
        }
    }

    @Test
    fun test_on_current_location_click_selects_button_and_finishes_with_corresponding_model() {
        given {
            nothing()
        } whenn {
            current_location_click()
            go_click()
        } then {
            should_call_view_finish_with_location_option_current_location()
        }
    }

    @Test
    fun test_on_select_location_click_navigate_to_select_location_with_last_selected_location() {
        given {
            nothing()
        } whenn {
            select_location_click()
        } then {
            should_call_view_navigate_select_location_with_model_selected_latitude_and_longitude()
        }
    }

    @Test
    fun test_after_location_selection_selects_button_and_changes_selected_location() {
        given {
            a_new_latitude_and_longitude()
        } whenn {
            current_location_click()
            location_selected()
            go_click()
        } then {
            should_call_view_set_option_button_selected()
            should_call_view_finish_with_location_option_selected_location_and_new_position()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: SearchSettingsPresenter
        @Mock lateinit var mockView: SearchSettingsView
        val initialModel = SearchSettingsModel("test", SearchSettingsModel.LocationOption.SELECTED,
                1.1, 2.2, 3.3, 4.4)
        var searchText = ""
        var newLatitude = 0.0
        var newLongitude = 0.0

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = SearchSettingsPresenter()
            presenter.setViewAndModel(mockView, initialModel)

            return this
        }

        fun nothing() {}

        fun a_search_text() {
            searchText = "culture"
        }

        fun a_new_latitude_and_longitude() {
            newLatitude = 9.9
            newLongitude = -0.1
        }

        fun create_presenter() {
            presenter.create()
        }

        fun go_click() {
            presenter.onGoClick()
        }

        fun search_text_changed() {
            presenter.onSearchTextChanged(searchText)
        }

        fun current_location_click() {
            presenter.onLocationOptionClick(SearchSettingsModel.LocationOption.CURRENT)
        }

        fun select_location_click() {
            presenter.onLocationOptionClick(SearchSettingsModel.LocationOption.SELECTED)
        }

        fun location_selected() {
            presenter.onLocationSelected(newLatitude, newLongitude)
        }

        fun should_call_view_set_search_text() {
            BDDMockito.then(mockView).should().setSearchText(initialModel.searchText)
        }

        fun should_call_view_set_option_button_selected() {
            BDDMockito.then(mockView).should()
                    .setLocationOptionSelected(initialModel.locationOption)
        }

        fun should_call_view_finish_with_result_initial_model() {
            BDDMockito.then(mockView).should().finishViewWithResult(initialModel)
        }

        fun should_call_view_finish_with_result_model_modified_with_search_text() {
            BDDMockito.then(mockView).should().finishViewWithResult(
                    SearchSettingsModel(searchText, initialModel.locationOption,
                            initialModel.currentLatitude, initialModel.currentLongitude,
                            initialModel.selectedLatitude, initialModel.selectedLongitude))
        }

        fun should_call_view_finish_with_location_option_current_location() {
            BDDMockito.then(mockView).should().finishViewWithResult(
                    SearchSettingsModel(initialModel.searchText,
                            SearchSettingsModel.LocationOption.CURRENT,
                            initialModel.currentLatitude, initialModel.currentLongitude,
                            initialModel.selectedLatitude, initialModel.selectedLongitude))
        }

        fun should_call_view_navigate_select_location_with_model_selected_latitude_and_longitude() {
            BDDMockito.then(mockView).should().navigateToSelectLocation(
                    initialModel.selectedLatitude, initialModel.selectedLongitude)
        }

        fun should_call_view_finish_with_location_option_selected_location_and_new_position() {
            BDDMockito.then(mockView).should().finishViewWithResult(
                    SearchSettingsModel(initialModel.searchText,
                            SearchSettingsModel.LocationOption.SELECTED,
                            initialModel.currentLatitude, initialModel.currentLongitude,
                            newLatitude, newLongitude))

        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
