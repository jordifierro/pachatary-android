package com.pachatary.presentation.register

import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.auth.ClientException
import com.pachatary.data.auth.Person
import com.pachatary.data.common.Result
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ConfirmEmailPresenterTest {

    @Test
    fun test_register_ok() {
        given {
            a_confirmation_token()
            an_auth_repo_that_returns_a_flowable_with_a_person()
        } whenn {
            presenter_is_created()
        } then {
            should_show_view_loader()
            should_call_repo_confirm_email_with_confirmation_token()
            should_hide_view_loader()
            should_show_success_message()
            should_navigate_to_main_view()
        }
    }

    @Test
    fun test_register_error() {
        given {
            a_confirmation_token()
            a_client_error()
            an_auth_repo_that_returns_a_flowable_with_that_error()
        } whenn {
            presenter_is_created()
        } then {
            should_show_view_loader()
            should_call_repo_confirm_email_with_confirmation_token()
            error_should_be_shown()
            should_hide_view_loader()
            should_finish_view()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: ConfirmEmailPresenter
        @Mock private lateinit var mockView: ConfirmEmailView
        @Mock private lateinit var mockAuthRepo: AuthRepository
        var confirmationToken = ""
        var clientError: ClientException? = null

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = ConfirmEmailPresenter(mockAuthRepo, testSchedulerProvider)
            presenter.view = mockView

            return this
        }

        fun a_confirmation_token() {
            confirmationToken = "BCDZ"
            BDDMockito.given(mockView.confirmationToken()).willReturn(confirmationToken)
        }

        fun a_client_error() {
            clientError = ClientException(source = "s", code = "c", message = "mess")
        }

        fun an_auth_repo_that_returns_a_flowable_with_a_person() {
            BDDMockito.given(mockAuthRepo.confirmEmail(confirmationToken))
                    .willReturn(Flowable.just(Result(
                            Person(true, "u", "e", false), null)))
        }

        fun an_auth_repo_that_returns_a_flowable_with_that_error() {
            BDDMockito.given(mockAuthRepo.confirmEmail(confirmationToken))
                    .willReturn(Flowable.just(Result<Person>(null, clientError)))
        }

        fun error_should_be_shown() {
            BDDMockito.then(mockView).should().showMessage(clientError!!.message)
        }

        fun should_show_view_loader() {
            BDDMockito.then(mockView).should().showLoader()
        }

        fun should_hide_view_loader() {
            BDDMockito.then(mockView).should().hideLoader()
        }

        fun presenter_is_created() {
            presenter.create()
        }

        fun should_call_repo_confirm_email_with_confirmation_token() {
            BDDMockito.then(mockAuthRepo).should().confirmEmail(confirmationToken)
        }

        fun should_show_success_message() {
            BDDMockito.then(mockView).should().showMessage("Email successfully confirmed!")
        }

        fun should_navigate_to_main_view() {
            BDDMockito.then(mockView).should().navigateToMain()
        }

        fun should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
