package com.abidria.data.auth

import com.abidria.data.common.Result
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import junit.framework.Assert.assertEquals
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class AuthApiRepositoryTest {

    @Test
    fun on_get_person_invitation_calls_post_people_and_parses_auth_token() {
        given {
            a_web_server_that_returns_get_people_credentials_response_200()
        } whenn {
            get_person_invitation()
        } then {
            request_should_post_to_people_with_client_secret_key()
            response_should_be_auth_token()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        val mockWebServer = MockWebServer()
        val clientSecretKey = "y"
        val repository = AuthApiRepository(Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create(
                        GsonBuilder().setFieldNamingPolicy(
                                FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build(), clientSecretKey)
        val testAuthTokenSubscriber = TestSubscriber<Result<AuthToken>>()

        fun a_web_server_that_returns_get_people_credentials_response_200() {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                    AuthApiRepository::class.java.getResource("/api/POST_people.json").readText()))
        }

        fun get_person_invitation() {
            repository.getPersonInvitation().subscribeOn(Schedulers.trampoline()).subscribe(testAuthTokenSubscriber)
            testAuthTokenSubscriber.awaitCount(1)
        }

        fun request_should_post_to_people_with_client_secret_key() {
            val request = mockWebServer.takeRequest()
            assertEquals("/people/", request.getPath())
            assertEquals("POST", request.getMethod())
            assertEquals("client_secret_key=" + clientSecretKey, request.getBody().readUtf8())
        }

        fun response_should_be_auth_token() {
            testAuthTokenSubscriber.assertResult(
                    Result(AuthToken("868a2b9a", "9017c7e7"), null))
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}