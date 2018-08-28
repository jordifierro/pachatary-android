package com.pachatary.presentation.register

import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.common.ClientException
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultError
import com.pachatary.data.common.ResultSuccess
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.net.SocketTimeoutException

class ConfirmEmailPresenterTest {

    enum class Action { CREATE, RETRY }

    @Test
    fun test_register_ok() {
        for (action in Action.values()) {
            given {
                a_confirmation_token("TK")
                an_auth_repo_that_returns("TK", ResultSuccess())
            } whenn {
                do_action(action)
            } then {
                should_call_repo_confirm_email_with("TK")
                should_hide_view_loader()
                should_show_success_message()
                should_navigate_to_main_view()
            }
        }
    }

    @Test
    fun test_register_token_error() {
        for (action in Action.values()) {
            given {
                a_confirmation_token("TK")
                an_auth_repo_that_returns("TK", ResultError(ClientException("", "", "")))
            } whenn {
                do_action(action)
            } then {
                should_call_repo_confirm_email_with("TK")
                invalid_token_error_should_be_shown()
                should_hide_view_loader()
                should_navigate_to_register()
            }
        }
    }

    @Test
    fun test_register_connection_error() {
        for (action in Action.values()) {
            given {
                a_confirmation_token("TK")
                an_auth_repo_that_returns("TK", ResultError(SocketTimeoutException()))
            } whenn {
                do_action(action)
            } then {
                should_call_repo_confirm_email_with("TK")
                should_show_retry()
                should_hide_view_loader()
            }
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: ConfirmEmailPresenter
        @Mock private lateinit var mockView: ConfirmEmailView
        @Mock private lateinit var mockAuthRepo: AuthRepository

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = ConfirmEmailPresenter(mockAuthRepo, testSchedulerProvider)
            presenter.view = mockView

            return this
        }

        fun a_confirmation_token(token: String) {
            BDDMockito.given(mockView.confirmationToken()).willReturn(token)
        }

        fun an_auth_repo_that_returns(token: String, result: Result<Void>) {
            BDDMockito.given(mockAuthRepo.confirmEmail(token))
                    .willReturn(Flowable.just(result))
        }

        fun invalid_token_error_should_be_shown() {
            BDDMockito.then(mockView).should().showInvalidTokenMessage()
        }

        fun should_show_view_loader() {
            BDDMockito.then(mockView).should().showLoader()
        }

        fun should_hide_view_loader() {
            BDDMockito.then(mockView).should().hideLoader()
        }

        fun do_action(action: Action) {
            when (action) {
                Action.CREATE -> presenter.create()
                Action.RETRY -> presenter.onRetryClick()
            }
        }

        fun should_call_repo_confirm_email_with(token: String) {
            BDDMockito.then(mockAuthRepo).should().confirmEmail(token)
        }

        fun should_show_success_message() {
            BDDMockito.then(mockView).should().showSuccessMessage()
        }

        fun should_navigate_to_main_view() {
            BDDMockito.then(mockView).should().navigateToMain()
        }

        fun should_navigate_to_register() {
            BDDMockito.then(mockView).should().navigateToRegisterWithDelay()
        }

        fun should_show_retry() {
            BDDMockito.then(mockView).should().showRetry()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
