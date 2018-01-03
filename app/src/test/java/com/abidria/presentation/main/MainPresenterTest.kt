package com.abidria.presentation.main

import com.abidria.data.auth.AuthRepository
import com.abidria.data.auth.AuthToken
import com.abidria.data.common.Result
import com.abidria.data.experience.Experience
import com.abidria.data.experience.ExperienceRepository
import com.abidria.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class MainPresenterTest {

    @Test
    fun test_if_has_credentials_go_to_initial_tab_on_create() {
        given {
            an_auth_repo_has_credentials()
        } whenn {
            create_presenter()
        } then {
            should_call_auth_repo_has_person_credentials()
            should_hide_view_loader()
            should_show_view_tabs()
            should_show_saved_view()
        }
    }

    @Test
    fun test_if_has_no_credentials_should_ask_person_invitation_and_go_to_initial_tab_on_create() {
        given {
            an_auth_repo_has_no_credentials()
            an_auth_repo_returns_auth_token_on_get_person_invitation()
        } whenn {
            create_presenter()
        } then {
            should_call_auth_repo_has_person_credentials()
            should_call_auth_repo_get_person_invitation()
            should_show_view_loader()
            should_hide_view_tabs()

            should_hide_view_loader()
            should_show_view_tabs()
            should_show_saved_view()
        }
    }

    @Test
    fun test_tab_click_while_no_credentials() {
        given {
            an_auth_repo_has_no_credentials()
        } whenn {
            tab_click()
        } then {
            should_not_call_show_view()
        }
    }

    @Test
    fun test_tab_click_when_credentials() {
        given {
            an_auth_repo_has_credentials()
        } whenn {
            tab_click()
        } then {
            should_show_view()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: MainPresenter
        @Mock lateinit var mockView: MainView
        @Mock lateinit var mockAuthRepository: AuthRepository

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = MainPresenter(mockAuthRepository, testSchedulerProvider)
            presenter.view = mockView

            return this
        }

        fun nothing() {}

        fun an_auth_repo_has_no_credentials() {
            given(mockAuthRepository.hasPersonCredentials()).willReturn(false)
        }

        fun an_auth_repo_has_credentials() {
            given(mockAuthRepository.hasPersonCredentials()).willReturn(true)
        }

        fun an_auth_repo_returns_auth_token_on_get_person_invitation() {
            given(mockAuthRepository.getPersonInvitation()).willReturn(
                    Flowable.just(Result(AuthToken("A", "R"), null)))
        }

        fun create_presenter() {
            presenter.create()
        }

        fun tab_click() {
            presenter.onTabClick(MainView.ExperiencesViewType.SAVED)
        }

        fun should_call_auth_repo_has_person_credentials() {
            then(mockAuthRepository).should().hasPersonCredentials()
        }

        fun should_call_auth_repo_get_person_invitation() {
            then(mockAuthRepository).should().getPersonInvitation()
        }

        fun should_show_view_loader() {
            then(mockView).should().showLoader()
        }

        fun should_hide_view_loader() {
            then(mockView).should().hideLoader()
        }

        fun should_show_view_tabs() {
            BDDMockito.then(mockView).should().showTabs(true)
        }

        fun should_show_saved_view() {
            BDDMockito.then(mockView).should().showView(MainView.ExperiencesViewType.SAVED)
        }

        fun should_hide_view_tabs() {
            BDDMockito.then(mockView).should().showTabs(false)
        }

        fun should_not_call_show_view() {
            BDDMockito.then(mockView).shouldHaveZeroInteractions()
        }

        fun should_show_view() {
            BDDMockito.then(mockView).should().showView(MainView.ExperiencesViewType.SAVED)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
