package com.pachatary.presentation.router

import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.auth.AuthToken
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultError
import com.pachatary.data.common.ResultInProgress
import com.pachatary.data.common.ResultSuccess
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations
import java.net.UnknownHostException

class ProfileRouterPresenterTest {

    enum class Action { CREATE, RETRY }

    @Test
    fun test_create_retry_when_no_credentials_and_receives_inprogress() {
        for (action in listOf(Action.CREATE, Action.RETRY)) {
            given {
                an_auth_that_returns_on_has_credentials(false)
                an_auth_that_returns_on_get_person_invitation(inProgress = true)
            } whenn {
                do_action(action)
            } then {
                should_call_get_person_invitation()
                should_show_loader()
                should_hide_retry_view()
            }
        }
    }

    @Test
    fun test_create_retry_when_no_credentials_and_receives_error() {
        for (action in listOf(Action.CREATE, Action.RETRY)) {
            given {
                an_auth_that_returns_on_has_credentials(false)
                an_auth_that_returns_on_get_person_invitation(error = UnknownHostException())
            } whenn {
                do_action(action)
            } then {
                should_call_get_person_invitation()
                should_show_retry_view()
                should_hide_loader()
                should_show_error_message()
            }
        }
    }

    @Test
    fun test_create_retry_when_no_credentials_and_receives_success_navigates_to_profile() {
        for (action in listOf(Action.CREATE, Action.RETRY)) {
            given {
                an_auth_that_returns_on_has_credentials(false)
                an_auth_that_returns_on_get_person_invitation(success = true)
            } whenn {
                do_action(action)
            } then {
                should_call_get_person_invitation()
                should_hide_loader()
                should_hide_retry_view()
                should_navitate_to_profile()
                should_finish_view()
            }
        }
    }

    @Test
    fun test_create_retry_when_credentials_navigates_to_profile() {
        for (action in listOf(Action.CREATE, Action.RETRY)) {
            given {
                an_auth_that_returns_on_has_credentials(true)
            } whenn {
                do_action(action)
            } then {
                should_navitate_to_profile()
                should_finish_view()
            }
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var presenter: ProfileRouterPresenter
        @Mock lateinit var mockAuthRepository: AuthRepository
        @Mock lateinit var mockView: RouterView
        val username = "a.b_c"

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = ProfileRouterPresenter(mockAuthRepository, Schedulers.trampoline())
            presenter.setViewAndUsername(mockView, username)

            return this
        }

        fun an_auth_that_returns_on_has_credentials(response: Boolean) {
            BDDMockito.given(mockAuthRepository.hasPersonCredentials()).willReturn(response)
        }

        fun an_auth_that_returns_on_get_person_invitation(success: Boolean = false,
                                                          inProgress: Boolean = false,
                                                          error: Exception? = null) {
            var authToken: AuthToken? = null
            if (success) authToken = AuthToken("a", "r")
            var result: Result<AuthToken>? = null
            if (success) result = ResultSuccess(authToken)
            else if (inProgress) result = ResultInProgress()
            else if (error != null) result = ResultError(error)
            BDDMockito.given(mockAuthRepository.getPersonInvitation()).willReturn(
                            Flowable.just(result))
        }

        fun do_action(action: Action) {
            when (action) {
                Action.CREATE -> presenter.create()
                Action.RETRY -> presenter.onRetryClick()
            }
        }

        fun should_call_get_person_invitation() {
            BDDMockito.then(mockAuthRepository).should().getPersonInvitation()
        }

        fun should_show_loader() {
            BDDMockito.then(mockView).should().showLoader()
        }

        fun should_hide_retry_view(times: Int = 1) {
            BDDMockito.then(mockView).should(times(times)).hideRetryView()
        }

        fun should_show_retry_view() {
            BDDMockito.then(mockView).should().showRetryView()
        }

        fun should_hide_loader(times: Int = 1) {
            BDDMockito.then(mockView).should(Mockito.times(times)).hideLoader()
        }

        fun should_show_error_message() {
            BDDMockito.then(mockView).should().showErrorMessage()
        }

        fun should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        fun should_navitate_to_profile() {
            BDDMockito.then(mockView).should().navigateToProfile(username)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}