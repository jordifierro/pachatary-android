package com.pachatary.presentation.register

import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.common.ClientException
import com.pachatary.data.common.ResultError
import com.pachatary.data.common.ResultSuccess
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class RegisterPresenterTest {

    @Test
    fun test_register_ok() {
        given {
            a_username()
            an_email()
            an_auth_repo_that_returns_a_flowable_with_a_person()
        } whenn {
            done_button_is_clicked()
        } then {
            should_show_view_loader()
            should_block_done_button()
            should_call_repo_register_with_username_and_email()
            should_hide_view_loader()
            should_show_success_message_and_finish_view()
        }
    }

    @Test
    fun test_register_error() {
        given {
            a_username()
            an_email()
            a_client_error()
            an_auth_repo_that_returns_a_flowable_with_that_error()
        } whenn {
            done_button_is_clicked()
        } then {
            should_show_view_loader()
            should_block_done_button()
            error_should_be_shown()
            should_hide_view_loader()
            should_unblock_done_button()
        }
    }

    @Test
    fun test_register_already_registered_error_finishes_view() {
        given {
            a_username()
            an_email()
            an_already_registered_client_error()
            an_auth_repo_that_returns_a_flowable_with_that_error()
        } whenn {
            done_button_is_clicked()
        } then {
            should_show_view_loader()
            should_block_done_button()
            should_finish_view()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: RegisterPresenter
        @Mock private lateinit var mockView: RegisterView
        @Mock private lateinit var mockAuthRepo: AuthRepository
        var username = ""
        var email = ""
        var clientError: ClientException? = null

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = RegisterPresenter(mockAuthRepo, testSchedulerProvider)
            presenter.view = mockView

            return this
        }

        fun a_username() {
            username = "usr.nm"
            BDDMockito.given(mockView.getUsername()).willReturn(username)
        }

        fun an_email() {
            email = "e@m.c"
            BDDMockito.given(mockView.getEmail()).willReturn(email)
        }

        fun a_client_error() {
            clientError = ClientException(source = "s", code = "c", message = "mess")
        }

        fun an_already_registered_client_error() {
            clientError = ClientException(source = "person", code = "already_registered",
                    message = "Person already registered")
        }

        fun an_auth_repo_that_returns_a_flowable_with_a_person() {
            BDDMockito.given(mockAuthRepo.register(username, email))
                    .willReturn(Flowable.just(ResultSuccess()))
        }

        fun an_auth_repo_that_returns_a_flowable_with_that_error() {
            BDDMockito.given(mockAuthRepo.register(username, email))
                    .willReturn(Flowable.just(ResultError(clientError!!)))
        }

        fun done_button_is_clicked() {
            presenter.doneButtonClick()
        }

        fun should_call_repo_register_with_username_and_email() {
            BDDMockito.then(mockAuthRepo).should().register(username, email)
        }

        fun should_show_success_message_and_finish_view() {
            BDDMockito.then(mockView).should()
                    .showMessage("Successfully registered!\n Check your email to finalize the process")
            BDDMockito.then(mockView).should().finish()
        }

        fun should_finish_view() {
            BDDMockito.then(mockView).should().finish()
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

        fun should_block_done_button() {
            BDDMockito.then(mockView).should().blockDoneButton(true)
        }

        fun should_unblock_done_button() {
            BDDMockito.then(mockView).should().blockDoneButton(false)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
