package com.pachatary.presentation.main

import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.auth.AuthToken
import com.pachatary.data.common.Result
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
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

    @Test
    fun test_on_login_click_navigates_to_ask_login_email_and_finishes() {
        given {
            nothing()
        } whenn {
            on_login_click()
        } then {
            should_navigate_to_ask_login()
            should_finish_view()
        }
    }

    @Test
    fun test_on_privacy_policy_click_navigates_to_privacy_policy() {
        given {
            nothing()
        } whenn {
            on_privacy_policy_click()
        } then {
            should_navigate_to_privacy_policy()
        }
    }

    @Test
    fun test_on_terms_and_conditions_click_navigates_to_privacy_policy() {
        given {
            nothing()
        } whenn {
            on_terms_and_conditions_click()
        } then {
            should_navigate_to_terms_and_conditions()
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

        fun nothing() {}

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

        fun on_login_click() {
            presenter.onLoginClick()
        }

        fun on_privacy_policy_click() {
            presenter.onPrivacyPolicyClick()
        }

        fun on_terms_and_conditions_click() {
            presenter.onTermsAndConditionsClick()
        }

        fun should_show_loader() {
            BDDMockito.then(mockView).should().showLoader()
        }

        fun should_disable_start_button() {
            BDDMockito.then(mockView).should().disableButtons()
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
            BDDMockito.then(mockView).should().enableButtons()
        }

        fun should_show_error_message() {
            BDDMockito.then(mockView).should().showErrorMessage()
        }

        fun should_navigate_to_ask_login() {
            BDDMockito.then(mockView).should().navigateToAskLogin()
        }

        fun should_navigate_to_privacy_policy() {
            BDDMockito.then(mockView).should().navigateToPrivacyPolicy()
        }

        fun should_navigate_to_terms_and_conditions() {
            BDDMockito.then(mockView).should().navigateToTermsAndConditions()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
