package com.pachatary.presentation.main

import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.auth.AuthToken
import com.pachatary.data.common.Result
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class WelcomePresenterTest {

    @Test
    fun test_on_start_click_receives_in_progress() {
        given {
            an_auth_repo_that_returns_loading_result_when_get_invitation_called()
        } whenn {
            on_start_click()
        } then {
            should_show_loader()
            should_disable_start_button()
        }
    }

    @Test
    fun test_on_start_click_receives_success() {
        given {
            an_auth_repo_that_returns_success_result_when_get_invitation_called()
        } whenn {
            on_start_click()
        } then {
            should_hide_loader()
            should_navigate_to_main()
            should_finish_view()
        }
    }

    @Test
    fun test_on_start_click_receives_error() {
        given {
            an_auth_repo_that_returns_error_result_when_get_invitation_called()
        } whenn {
            on_start_click()
        } then {
            should_hide_loader()
            should_enable_start_button()
            should_show_error_message()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: WelcomePresenter
        @Mock lateinit var mockView: WelcomeView
        @Mock lateinit var mockAuthRepository: AuthRepository

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = WelcomePresenter(mockAuthRepository, Schedulers.trampoline())
            presenter.view = mockView

            return this
        }

        fun an_auth_repo_that_returns_loading_result_when_get_invitation_called() {
            BDDMockito.given(mockAuthRepository.getPersonInvitation())
                    .willReturn(Flowable.just(Result<AuthToken>(null, inProgress = true)))
        }

        fun an_auth_repo_that_returns_success_result_when_get_invitation_called() {
            BDDMockito.given(mockAuthRepository.getPersonInvitation())
                    .willReturn(Flowable.just(Result(AuthToken("a", "r"))))
        }

        fun an_auth_repo_that_returns_error_result_when_get_invitation_called() {
            BDDMockito.given(mockAuthRepository.getPersonInvitation())
                    .willReturn(Flowable.just(Result<AuthToken>(null, error = Exception())))
        }

        fun on_start_click() {
            presenter.onStartClick()
        }

        fun should_show_loader() {
            BDDMockito.then(mockView).should().showLoader()
        }

        fun should_disable_start_button() {
            BDDMockito.then(mockView).should().disableStartButton()
        }

        fun should_hide_loader() {
            BDDMockito.then(mockView).should().hideLoader()
        }

        fun should_navigate_to_main() {
            BDDMockito.then(mockView).should().navigateToMain()
        }

        fun should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        fun should_enable_start_button() {
            BDDMockito.then(mockView).should().enableStartButton()
        }

        fun should_show_error_message() {
            BDDMockito.then(mockView).should().showErrorMessage()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
