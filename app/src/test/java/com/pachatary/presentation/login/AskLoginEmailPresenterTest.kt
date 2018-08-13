package com.pachatary.presentation.login

import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.common.ResultError
import com.pachatary.data.common.ResultInProgress
import com.pachatary.data.common.ResultSuccess
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class AskLoginEmailPresenterTest {

    @Test
    fun test_on_ask_click_receives_in_progress() {
        given {
            an_email()
            an_auth_repo_that_returns_loading_result_when_ask_login_email()
        } whenn {
            on_ask_click()
        } then {
            should_call_repo_ask_login_email_with_email()
            should_show_loader()
            should_disable_ask_button()
        }
    }

    @Test
    fun test_on_ask_click_receives_success() {
        given {
            an_email()
            an_auth_repo_that_returns_success_result_when_ask_login_email()
        } whenn {
            on_ask_click()
        } then {
            should_call_repo_ask_login_email_with_email()
            should_hide_loader()
            should_show_success_message()
            should_finish_app()
        }
    }

    @Test
    fun test_on_ask_click_receives_error() {
        given {
            an_email()
            an_auth_repo_that_returns_error_result_when_ask_login_email()
        } whenn {
            on_ask_click()
        } then {
            should_call_repo_ask_login_email_with_email()
            should_hide_loader()
            should_enable_ask_button()
            should_show_error_message()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: AskLoginEmailPresenter
        @Mock lateinit var mockView: AskLoginEmailView
        @Mock lateinit var mockAuthRepository: AuthRepository
        var email = ""

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = AskLoginEmailPresenter(mockAuthRepository, Schedulers.trampoline())
            presenter.view = mockView

            return this
        }

        fun an_email() {
            email = "e@c.m"
        }

        fun an_auth_repo_that_returns_loading_result_when_ask_login_email() {
            BDDMockito.given(mockAuthRepository.askLoginEmail(email))
                    .willReturn(Flowable.just(ResultInProgress()))
        }

        fun an_auth_repo_that_returns_success_result_when_ask_login_email() {
            BDDMockito.given(mockAuthRepository.askLoginEmail(email))
                    .willReturn(Flowable.just(ResultSuccess()))
        }

        fun an_auth_repo_that_returns_error_result_when_ask_login_email() {
            BDDMockito.given(mockAuthRepository.askLoginEmail(email))
                    .willReturn(Flowable.just(ResultError(Exception())))
        }

        fun on_ask_click() {
            presenter.onAskClick(email)
        }

        fun should_call_repo_ask_login_email_with_email() {
            BDDMockito.then(mockAuthRepository).should().askLoginEmail(email)
        }

        fun should_show_loader() {
            BDDMockito.then(mockView).should().showLoader()
        }

        fun should_disable_ask_button() {
            BDDMockito.then(mockView).should().disableAskButton()
        }

        fun should_hide_loader() {
            BDDMockito.then(mockView).should().hideLoader()
        }

        fun should_finish_app() {
            BDDMockito.then(mockView).should().finishApplication()
        }

        fun should_enable_ask_button() {
            BDDMockito.then(mockView).should().enableAskButton()
        }

        fun should_show_error_message() {
            BDDMockito.then(mockView).should().showErrorMessage()
        }

        fun should_show_success_message() {
            BDDMockito.then(mockView).should().showSuccessMessage()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
