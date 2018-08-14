package com.pachatary.presentation.login

import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.common.ClientException
import com.pachatary.data.common.ResultError
import com.pachatary.data.common.ResultInProgress
import com.pachatary.data.common.ResultSuccess
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LoginPresenterTest {

    enum class Action { CREATE, RETRY }

    @Test
    fun test_on_login_retry_receives_in_progress() {
        for (action in Action.values()) {
            given {
                a_login_token()
                an_auth_repo_that_returns_loading_result_when_login()
            } whenn {
                do_action(action)
            } then {
                should_call_repo_login_with_login_token()
                should_show_loader()
            }
        }
    }

    @Test
    fun test_on_login_retry_receives_success() {
        for (action in Action.values()) {
            given {
                a_login_token()
                an_auth_repo_that_returns_success_result_when_login()
            } whenn {
                do_action(action)
            } then {
                should_call_repo_login_with_login_token()
                should_hide_loader()
                should_show_success_message()
                should_navigate_to_main()
                should_finish_view()
            }
        }
    }

    @Test
    fun test_on_login_retry_receives_internet_error() {
        for (action in Action.values()) {
            given {
                a_login_token()
                an_auth_repo_that_returns_error_result_when_login(Exception())
            } whenn {
                do_action(action)
            } then {
                should_call_repo_login_with_login_token()
                should_hide_loader()
                should_show_retry()
            }
        }
    }

    @Test
    fun test_on_login_retry_receives_client_error() {
        for (action in Action.values()) {
            given {
                a_login_token()
                an_auth_repo_that_returns_error_result_when_login(ClientException("s", "c", "m"))
            } whenn {
                do_action(action)
            } then {
                should_call_repo_login_with_login_token()
                should_hide_loader()
                should_show_error_message()
                should_navigate_to_ask_login_email()
            }
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        lateinit var presenter: LoginPresenter
        @Mock lateinit var mockView: LoginView
        @Mock lateinit var mockAuthRepository: AuthRepository
        var loginToken = ""

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = LoginPresenter(mockAuthRepository, Schedulers.trampoline())
            presenter.view = mockView

            return this
        }

        fun a_login_token() {
            loginToken = "ABC"
            BDDMockito.given(mockView.loginToken()).willReturn(loginToken)
        }

        fun an_auth_repo_that_returns_loading_result_when_login() {
            BDDMockito.given(mockAuthRepository.login(loginToken))
                .willReturn(Flowable.just(ResultInProgress()))
        }

        fun an_auth_repo_that_returns_success_result_when_login() {
            BDDMockito.given(mockAuthRepository.login(loginToken))
                    .willReturn(Flowable.just(ResultSuccess()))
        }

        fun an_auth_repo_that_returns_error_result_when_login(error: Exception) {
            BDDMockito.given(mockAuthRepository.login(loginToken)).willReturn(
                    Flowable.just(ResultError(error)))
        }

        fun do_action(action: Action) {
            when (action) {
                Action.CREATE -> presenter.create()
                Action.RETRY -> presenter.retryClick()
            }
        }

        fun should_call_repo_login_with_login_token() {
            BDDMockito.then(mockAuthRepository).should().login(loginToken)
        }

        fun should_show_loader() {
            BDDMockito.then(mockView).should().showLoader()
        }

        fun should_hide_loader() {
            BDDMockito.then(mockView).should().hideLoader()
        }

        fun should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        fun should_show_error_message() {
            BDDMockito.then(mockView).should().showErrorMessage()
        }

        fun should_show_success_message() {
            BDDMockito.then(mockView).should().showSuccessMessage()
        }

        fun should_navigate_to_main() {
            BDDMockito.then(mockView).should().navigateToMain()
        }

        fun should_show_retry() {
            BDDMockito.then(mockView).should().showRetry()
        }

        fun should_navigate_to_ask_login_email() {
            BDDMockito.then(mockView).should().navigateToAskLoginEmailWithDelay()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
