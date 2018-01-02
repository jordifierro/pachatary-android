package com.abidria.data.auth

import com.abidria.data.common.Result
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class AuthApiRepositoryTest {

    @Test
    fun test_on_get_person_invitation_calls_post_people_and_parses_auth_token() {
        given {
            a_web_server_that_returns_get_people_credentials_response_200()
        } whenn {
            get_person_invitation()
        } then {
            request_should_post_to_people_with_client_secret_key()
            response_should_be_auth_token()
        }
    }

    @Test
    fun test_on_register_parses_person() {
        given {
            a_username()
            an_email()
            a_web_server_that_returns_person_response_200_when_patch_people()
        } whenn {
            register_person()
        } then {
            request_should_patch_person_me_with_username_and_email()
            response_should_be_person()
        }
    }

    @Test
    fun test_on_register_client_exception_returns_result_with_error() {
        given {
            a_username()
            an_email()
            a_web_server_that_returns_error_response_422_invalid_entity_when_patch_people()
        } whenn {
            register_person()
        } then {
            request_should_patch_person_me_with_username_and_email()
            response_should_be_error()
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
        val testRegisterSubscriber = TestSubscriber<Result<Person>>()
        var username = ""
        var email = ""

        fun a_username() {
            username = "user.nm"
        }

        fun an_email() {
            email = "e@m.c"
        }

        fun a_web_server_that_returns_get_people_credentials_response_200() {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                    AuthApiRepository::class.java.getResource("/api/POST_people.json").readText()))
        }

        fun a_web_server_that_returns_person_response_200_when_patch_people() {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                    AuthApiRepository::class.java.getResource("/api/PATCH_people.json").readText()))
        }

        fun a_web_server_that_returns_error_response_422_invalid_entity_when_patch_people() {
            mockWebServer.enqueue(MockResponse().setResponseCode(422).setBody(
                    AuthApiRepository::class.java.getResource("/api/PATCH_people_ERROR.json").readText()))
        }

        fun get_person_invitation() {
            repository.getPersonInvitation().subscribeOn(Schedulers.trampoline()).subscribe(testAuthTokenSubscriber)
            testAuthTokenSubscriber.awaitCount(1)
        }

        fun register_person() {
            repository.register(username = username, email = email)
                    .subscribeOn(Schedulers.trampoline()).subscribe(testRegisterSubscriber)
            testRegisterSubscriber.awaitCount(1)
        }

        fun request_should_post_to_people_with_client_secret_key() {
            val request = mockWebServer.takeRequest()
            assertEquals("/people/", request.getPath())
            assertEquals("POST", request.getMethod())
            assertEquals("client_secret_key=" + clientSecretKey, request.getBody().readUtf8())
        }

        fun request_should_patch_person_me_with_username_and_email() {
            val request = mockWebServer.takeRequest()
            assertEquals("/people/me", request.getPath())
            assertEquals("PATCH", request.getMethod())
            val formParams = "username=" + username + "&email=e%40m.c" //" + email
            assertEquals(formParams, request.getBody().readUtf8())
        }

        fun response_should_be_auth_token() {
            testAuthTokenSubscriber.assertResult(
                    Result(AuthToken("868a2b9a", "9017c7e7"), null))
        }

        fun response_should_be_person() {
            testRegisterSubscriber.assertResult(
                    Result(Person(isRegistered = true, username = "user.name",
                                  email = "test@mail.com", isEmailConfirmed = false), null)
            )
        }

        fun response_should_be_error() {
            testRegisterSubscriber.assertResult(Result(null,
                    error = ClientException(source = "username", code = "not_allowed",
                                            message = "Username not allowed"))
            )
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}