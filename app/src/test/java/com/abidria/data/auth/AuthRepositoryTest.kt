package com.abidria.data.auth

import com.abidria.data.common.Result
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import junit.framework.Assert.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mockito.mock
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepositoryTest {

    @Test
    fun has_person_credentials_returns_true() {
        given {
            an_auth_storage_repository_that_returns_an_auth_token()
        } whenn {
            has_person_credentials()
        } then {
            result_is_true()
        }
    }

    @Test
    fun has_not_person_credentials_returns_false() {
        given {
            an_auth_storage_repository_that_raises_no_logged_exception()
        } whenn {
            has_person_credentials()
        } then {
            result_is_false()
        }
    }

    @Test
    fun get_person_invitation_returns_flowable_and_saves_auth_token() {
        given {
            an_auth_api_that_returns_a_flowable_with_auth_token()
        } whenn {
            get_person_invitation()
        } then {
            should_return_auth_token_flowable()
            should_save_auth_token_on_auth_storage_repository()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        val authStorageRepository = mock(AuthStorageRepository::class.java)
        val authApiRepository = mock(AuthApiRepository::class.java)
        val repository = AuthRepository(authStorageRepository, authApiRepository)
        var hasCredentialsResult = false
        val testAuthTokenSubscriber = TestSubscriber<Result<AuthToken>>()
        lateinit var authToken: AuthToken

        fun an_auth_storage_repository_that_returns_an_auth_token() {
            val authToken = AuthToken(accessToken = "A", refreshToken = "R")
            BDDMockito.given(authStorageRepository.getPersonCredentials()).willReturn(authToken)
        }

        fun an_auth_storage_repository_that_raises_no_logged_exception() {
            BDDMockito.given(authStorageRepository.getPersonCredentials()).willThrow(NoLoggedException("Error"))
        }

        fun an_auth_api_that_returns_a_flowable_with_auth_token() {
            authToken = AuthToken("A", "R")
            BDDMockito.given(authApiRepository.getPersonInvitation()).willReturn(
                    Flowable.just(Result(authToken, null)))
        }

        fun has_person_credentials() {
            hasCredentialsResult = repository.hasPersonCredentials()
        }

        fun get_person_invitation() {
            repository.getPersonInvitation().subscribeOn(Schedulers.trampoline()).subscribe(testAuthTokenSubscriber)
        }

        fun result_is_true() {
            assertTrue(hasCredentialsResult)
        }

        fun result_is_false() {
            assertFalse(hasCredentialsResult)
        }

        fun should_return_auth_token_flowable() {
            testAuthTokenSubscriber.assertResult(Result(authToken, null))
        }

        fun should_save_auth_token_on_auth_storage_repository() {
            BDDMockito.then(authStorageRepository).should().setPersonCredentials(authToken)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}