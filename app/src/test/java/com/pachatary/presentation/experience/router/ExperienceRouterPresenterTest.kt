package com.pachatary.presentation.experience.router

import com.pachatary.data.auth.AuthRepository
import com.pachatary.data.auth.AuthToken
import com.pachatary.data.common.Result
import com.pachatary.data.experience.ExperienceRepository
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations
import java.net.UnknownHostException

class ExperienceRouterPresenterTest {

    @Test
    fun test_create_when_no_credentials_and_receives_inprogress() {
        given {
            an_auth_that_returns_on_has_credentials(false)
            an_auth_that_returns_on_get_person_invitation(inProgress = true)
        } whenn {
            create_presenter()
        } then {
            should_call_get_person_invitation()
            should_show_loader()
            should_hide_retry_view()
        }
    }

    @Test
    fun test_create_when_no_credentials_and_receives_error() {
        given {
            an_auth_that_returns_on_has_credentials(false)
            an_auth_that_returns_on_get_person_invitation(error = UnknownHostException())
        } whenn {
            create_presenter()
        } then {
            should_call_get_person_invitation()
            should_show_retry_view()
            should_hide_loader()
            should_show_error_message()
        }
    }

    @Test
    fun test_create_when_no_credentials_and_receives_success_and_translate_inprogress() {
        given {
            an_auth_that_returns_on_has_credentials(false)
            an_auth_that_returns_on_get_person_invitation(success = true)
            an_experience_repo_that_returns_on_translate(inProgress = true)
        } whenn {
            create_presenter()
        } then {
            should_call_get_person_invitation()
            should_hide_loader()

            should_hide_retry_view(times = 2)

            should_call_translate_share_id()
            should_show_loader()
        }
    }

    @Test
    fun test_create_when_no_credentials_and_receives_success_and_translate_error() {
        given {
            an_auth_that_returns_on_has_credentials(false)
            an_auth_that_returns_on_get_person_invitation(success = true)
            an_experience_repo_that_returns_on_translate(error = UnknownHostException())
        } whenn {
            create_presenter()
        } then {
            should_call_get_person_invitation()
            should_hide_retry_view()

            should_hide_loader(times = 2)

            should_call_translate_share_id()
            should_show_retry_view()
        }
    }

    @Test
    fun test_create_when_no_credentials_and_receives_success_and_translate_success() {
        given {
            an_auth_that_returns_on_has_credentials(false)
            an_auth_that_returns_on_get_person_invitation(success = true)
            an_experience_repo_that_returns_on_translate(experienceId = "7")
        } whenn {
            create_presenter()
        } then {
            should_call_get_person_invitation()

            should_hide_loader(times = 2)
            should_hide_retry_view(times = 2)

            should_call_translate_share_id()
            should_navigate_to_experience("7")
            should_finish_view()
        }
    }

    @Test
    fun test_create_when_credentials_and_receives_success_and_translate_inprogress() {
        given {
            an_auth_that_returns_on_has_credentials(true)
            an_auth_that_returns_on_get_person_invitation(success = true)
            an_experience_repo_that_returns_on_translate(inProgress = true)
        } whenn {
            create_presenter()
        } then {
            should_call_translate_share_id()
            should_show_loader()
            should_hide_retry_view()
        }
    }

    @Test
    fun test_create_when_credentials_and_receives_success_and_translate_error() {
        given {
            an_auth_that_returns_on_has_credentials(true)
            an_auth_that_returns_on_get_person_invitation(success = true)
            an_experience_repo_that_returns_on_translate(error = UnknownHostException())
        } whenn {
            create_presenter()
        } then {
            should_call_translate_share_id()
            should_hide_loader()
            should_show_retry_view()
        }
    }

    @Test
    fun test_create_when_credentials_and_receives_success_and_translate_success() {
        given {
            an_auth_that_returns_on_has_credentials(true)
            an_experience_repo_that_returns_on_translate(experienceId = "7")
        } whenn {
            create_presenter()
        } then {
            should_call_translate_share_id()
            should_hide_loader()
            should_hide_retry_view()
            should_navigate_to_experience("7")
            should_finish_view()
        }
    }

    @Test
    fun test_retry_when_no_credentials_and_receives_inprogress() {
        given {
            an_auth_that_returns_on_has_credentials(false)
            an_auth_that_returns_on_get_person_invitation(inProgress = true)
        } whenn {
            retry_click()
        } then {
            should_call_get_person_invitation()
            should_show_loader()
            should_hide_retry_view()
        }
    }

    @Test
    fun test_retry_when_no_credentials_and_receives_error() {
        given {
            an_auth_that_returns_on_has_credentials(false)
            an_auth_that_returns_on_get_person_invitation(error = UnknownHostException())
        } whenn {
            retry_click()
        } then {
            should_call_get_person_invitation()
            should_show_retry_view()
            should_hide_loader()
            should_show_error_message()
        }
    }

    @Test
    fun test_retry_when_no_credentials_and_receives_success_and_translate_inprogress() {
        given {
            an_auth_that_returns_on_has_credentials(false)
            an_auth_that_returns_on_get_person_invitation(success = true)
            an_experience_repo_that_returns_on_translate(inProgress = true)
        } whenn {
            retry_click()
        } then {
            should_call_get_person_invitation()
            should_hide_loader()

            should_hide_retry_view(2)

            should_call_translate_share_id()
            should_show_loader()
        }
    }

    @Test
    fun test_retry_when_no_credentials_and_receives_success_and_translate_error() {
        given {
            an_auth_that_returns_on_has_credentials(false)
            an_auth_that_returns_on_get_person_invitation(success = true)
            an_experience_repo_that_returns_on_translate(error = UnknownHostException())
        } whenn {
            retry_click()
        } then {
            should_call_get_person_invitation()
            should_hide_retry_view()

            should_hide_loader(times = 2)

            should_call_translate_share_id()
            should_show_retry_view()
        }
    }

    @Test
    fun test_retry_when_no_credentials_and_receives_success_and_translate_success() {
        given {
            an_auth_that_returns_on_has_credentials(false)
            an_auth_that_returns_on_get_person_invitation(success = true)
            an_experience_repo_that_returns_on_translate(experienceId = "7")
        } whenn {
            retry_click()
        } then {
            should_call_get_person_invitation()

            should_hide_loader(times = 2)
            should_hide_retry_view(times = 2)

            should_call_translate_share_id()
            should_navigate_to_experience("7")
            should_finish_view()
        }
    }

    @Test
    fun test_retry_when_credentials_and_receives_success_and_translate_inprogress() {
        given {
            an_auth_that_returns_on_has_credentials(true)
            an_auth_that_returns_on_get_person_invitation(success = true)
            an_experience_repo_that_returns_on_translate(inProgress = true)
        } whenn {
            retry_click()
        } then {
            should_call_translate_share_id()
            should_show_loader()
            should_hide_retry_view()
        }
    }

    @Test
    fun test_retry_when_credentials_and_receives_success_and_translate_error() {
        given {
            an_auth_that_returns_on_has_credentials(true)
            an_auth_that_returns_on_get_person_invitation(success = true)
            an_experience_repo_that_returns_on_translate(error = UnknownHostException())
        } whenn {
            retry_click()
        } then {
            should_call_translate_share_id()
            should_hide_loader()
            should_show_retry_view()
        }
    }

    @Test
    fun test_retry_when_credentials_and_receives_success_and_translate_success() {
        given {
            an_auth_that_returns_on_has_credentials(true)
            an_experience_repo_that_returns_on_translate(experienceId = "7")
        } whenn {
            retry_click()
        } then {
            should_call_translate_share_id()
            should_hide_loader()
            should_hide_retry_view()
            should_navigate_to_experience("7")
            should_finish_view()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var presenter: ExperienceRouterPresenter
        @Mock lateinit var mockAuthRepository: AuthRepository
        @Mock lateinit var mockExperienceRepository: ExperienceRepository
        @Mock lateinit var mockView: ExperienceRouterView
        val experienceShareId = "sd4Er32R"

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = ExperienceRouterPresenter(mockAuthRepository, mockExperienceRepository,
                                                  Schedulers.trampoline())
            presenter.setViewAndExperienceShareId(mockView, experienceShareId)

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
            BDDMockito.given(mockAuthRepository.getPersonInvitation()).willReturn(
                            Flowable.just(Result(authToken, inProgress = inProgress, error = error)))
        }

        fun an_experience_repo_that_returns_on_translate(experienceId: String? = null,
                                                         inProgress: Boolean = false,
                                                         error: Exception? = null) {
            BDDMockito.given(mockExperienceRepository.translateShareId(experienceShareId))
                    .willReturn(Flowable.just(
                            Result(experienceId, inProgress = inProgress, error = error)))
        }

        fun create_presenter() {
            presenter.create()
        }

        fun retry_click() {
            presenter.onRetryClick()
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

        fun should_call_translate_share_id() {
            BDDMockito.then(mockExperienceRepository).should().translateShareId(experienceShareId)
        }

        fun should_navigate_to_experience(experienceId: String) {
            BDDMockito.then(mockView).should().navigateToExperience(experienceId)
        }

        fun should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}