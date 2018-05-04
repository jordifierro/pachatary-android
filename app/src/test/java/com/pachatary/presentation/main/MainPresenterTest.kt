package com.pachatary.presentation.main

import com.pachatary.data.auth.AuthRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class MainPresenterTest {

    @Test
    fun test_if_has_credentials_selects_saved_tab() {
        given {
            an_auth_repo_has_credentials()
        } whenn {
            create_presenter()
        } then {
            should_call_auth_repo_has_person_credentials()
            should_select_tab(MainView.ExperiencesViewType.SAVED)
        }
    }

    @Test
    fun test_if_has_no_credentials_should_navigate_to_welcome() {
        given {
            an_auth_repo_has_no_credentials()
        } whenn {
            create_presenter()
        } then {
            should_navigate_to_welcome()
            should_finish_view()
        }
    }

    @Test
    fun test_on_tab_selected_should_show_view_of_that_tab() {
        for (type in MainView.ExperiencesViewType.values()) {
            given {
                an_auth_repo_has_credentials()
                a_type(type)
            } whenn {
                tab_selected()
            } then {
                should_show_view_of_that_tab()
            }
        }
    }

    @Test
    fun test_tab_click_shows_view() {
        given {
            an_auth_repo_has_credentials()
        } whenn {
            tab_click(MainView.ExperiencesViewType.SAVED)
        } then {
            should_show_view()
        }
    }

    @Test
    fun test_back_navigations_works_with_stack() {
        given {
            an_auth_repo_has_credentials()
        } whenn {
            tab_click(MainView.ExperiencesViewType.EXPLORE)
            tab_click(MainView.ExperiencesViewType.EXPLORE)
            tab_click(MainView.ExperiencesViewType.EXPLORE)
            tab_click(MainView.ExperiencesViewType.SAVED)
            tab_click(MainView.ExperiencesViewType.MY_EXPERIENCES)
            tab_click(MainView.ExperiencesViewType.SAVED)
            tab_click(MainView.ExperiencesViewType.EXPLORE)
        } then {
            should_show_view(MainView.ExperiencesViewType.EXPLORE, 4)
            should_show_view(MainView.ExperiencesViewType.SAVED, 2)
            should_show_view(MainView.ExperiencesViewType.MY_EXPERIENCES, 1)
        } whenn {
            on_back_pressed()
            on_back_pressed()
            on_back_pressed()
        } then {
            should_select_tab(MainView.ExperiencesViewType.SAVED)
            should_select_tab(MainView.ExperiencesViewType.MY_EXPERIENCES)
            should_finish_view()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: MainPresenter
        @Mock lateinit var mockView: MainView
        @Mock lateinit var mockAuthRepository: AuthRepository
        var type = MainView.ExperiencesViewType.SAVED

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = MainPresenter(mockAuthRepository)
            presenter.view = mockView

            return this
        }

        fun nothing() {}

        fun a_type(type: MainView.ExperiencesViewType) {
            this.type = type
        }

        fun an_auth_repo_has_no_credentials() {
            given(mockAuthRepository.hasPersonCredentials()).willReturn(false)
        }

        fun an_auth_repo_has_credentials() {
            given(mockAuthRepository.hasPersonCredentials()).willReturn(true)
        }

        fun create_presenter() {
            presenter.create()
        }

        fun tab_selected() {
            presenter.onTabClick(type)
        }

        fun tab_click(type: MainView.ExperiencesViewType) {
            presenter.onTabClick(type)
        }

        fun on_back_pressed() {
            presenter.onBackPressed()
        }

        fun should_call_auth_repo_has_person_credentials() {
            then(mockAuthRepository).should().hasPersonCredentials()
        }

        fun should_show_view(type: MainView.ExperiencesViewType, timesCalled: Int) {
            BDDMockito.then(mockView).should(BDDMockito.times(timesCalled)).showView(type)
        }

        fun should_show_view() {
            BDDMockito.then(mockView).should().showView(MainView.ExperiencesViewType.SAVED)
        }

        fun should_select_tab(type: MainView.ExperiencesViewType) {
            BDDMockito.then(mockView).should().selectTab(type)
        }

        fun should_show_view_of_that_tab() {
            BDDMockito.then(mockView).should().showView(type)
        }

        fun should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        fun should_navigate_to_welcome() {
            BDDMockito.then(mockView).should().navigateToWelcome()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
