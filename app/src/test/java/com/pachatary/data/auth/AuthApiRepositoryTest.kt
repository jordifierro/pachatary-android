package com.pachatary.data.auth

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.pachatary.data.common.*
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
            response_should_be_first_in_progress_then_auth_token()
        }
    }

    @Test
    fun test_on_register_returns_success() {
        given {
            a_username()
            an_email()
            a_web_server_that_returns_empty_response_204_when_patch_people()
        } whenn {
            register_person()
        } then {
            request_should_patch_person_me_with_username_and_email()
            response_should_be_success()
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

    @Test
    fun test_on_confirm_email_parses_success() {
        given {
            a_confirmation_token()
            a_web_server_that_returns_empty_response_204_when_post_email_confirmation()
        } whenn {
            confirm_email_person()
        } then {
            request_should_post_email_confirmation_with_confirmation_token()
            response_should_be_inprogress_and_success()
        }
    }

    @Test
    fun test_on_confirm_email_client_exception_returns_result_with_error() {
        given {
            a_confirmation_token()
            a_web_server_that_returns_error_422_invalid_entity_on_post_email_confirmation()
        } whenn {
            confirm_email_person()
        } then {
            request_should_post_email_confirmation_with_confirmation_token()
            response_should_be_inprogress_and_invalid_confirmation_token_error()
        }
    }

    @Test
    fun test_on_ask_login_email() {
        given {
            an_email()
            a_web_server_that_returns_204()
        } whenn {
            ask_login_email()
        } then {
            request_should_post_login_email_with_email()
            response_should_be_inprogress_and_success()
        }
    }

    @Test
    fun test_on_login() {
        given {
            a_login_token()
            a_web_server_that_returns_post_login_200()
        } whenn {
            login()
        } then {
            request_should_post_login_with_token()
            response_should_be_in_progress_and_success_with_auth_token()
        }
    }

    @Test
    fun test_client_versions() {
        given {
            a_web_server_that_returns_get_client_versions_200()
        } whenn {
            client_versions()
        } then {
            request_should_get_client_versions()
            response_should_return_inprogress_and_3()
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
                .build(), clientSecretKey, Schedulers.trampoline())
        val testAuthTokenSubscriber = TestSubscriber<Result<AuthToken>>()
        val testVoidSubscriber = TestSubscriber<Result<Void>>()
        val testIntSubscriber = TestSubscriber<Result<Int>>()
        var username = ""
        var email = ""
        var confirmationToken = ""
        var loginToken = ""

        fun a_username() {
            username = "user.nm"
        }

        fun an_email() {
            email = "e@m.c"
        }

        fun a_confirmation_token() {
            confirmationToken = "BASD"
        }

        fun a_login_token() {
            loginToken = "ABC"
        }

        fun a_web_server_that_returns_get_people_credentials_response_200() {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                    AuthApiRepository::class.java.getResource("/api/POST_people.json").readText()))
        }

        fun a_web_server_that_returns_empty_response_204_when_patch_people() {
            mockWebServer.enqueue(MockResponse().setResponseCode(204).setBody(""))
        }

        fun a_web_server_that_returns_empty_response_204_when_post_email_confirmation() {
            mockWebServer.enqueue(MockResponse().setResponseCode(204).setBody(""))
        }

        fun a_web_server_that_returns_error_response_422_invalid_entity_when_patch_people() {
            mockWebServer.enqueue(MockResponse().setResponseCode(422).setBody(
                AuthApiRepository::class.java.getResource(
                        "/api/PATCH_people_ERROR.json").readText()))
        }

        fun a_web_server_that_returns_error_422_invalid_entity_on_post_email_confirmation() {
            mockWebServer.enqueue(MockResponse().setResponseCode(422).setBody(
                    AuthApiRepository::class.java.getResource(
                            "/api/POST_people_me_email_confirmation_ERROR.json").readText()))
        }

        fun a_web_server_that_returns_post_login_200() {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                    AuthApiRepository::class.java.getResource("/api/POST_login.json").readText()))
        }

        fun a_web_server_that_returns_204() {
            mockWebServer.enqueue(MockResponse().setResponseCode(204).setBody(""))
        }

        fun a_web_server_that_returns_get_client_versions_200() {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                    AuthApiRepository::class.java.getResource(
                            "/api/GET_client_versions.json").readText()))
        }

        fun get_person_invitation() {
            repository.getPersonInvitation()
                    .subscribeOn(Schedulers.trampoline())
                    .subscribe(testAuthTokenSubscriber)
            testAuthTokenSubscriber.awaitCount(1)
        }

        fun register_person() {
            repository.register(username = username, email = email)
                    .subscribeOn(Schedulers.trampoline()).subscribe(testVoidSubscriber)
            testVoidSubscriber.awaitCount(1)
        }

        fun confirm_email_person() {
            repository.confirmEmail(confirmationToken = confirmationToken)
                    .subscribeOn(Schedulers.trampoline()).subscribe(testVoidSubscriber)
            testVoidSubscriber.awaitCount(1)
        }

        fun ask_login_email() {
            repository.askLoginEmail(email).subscribe(testVoidSubscriber)
            testVoidSubscriber.awaitCount(2)
        }

        fun login() {
            repository.login(loginToken).subscribe(testAuthTokenSubscriber)
            testAuthTokenSubscriber.awaitCount(2)
        }

        fun client_versions() {
            repository.clientVersions().subscribe(testIntSubscriber)
            testIntSubscriber.awaitCount(2)
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

        fun request_should_post_email_confirmation_with_confirmation_token() {
            val request = mockWebServer.takeRequest()
            assertEquals("/people/me/email-confirmation", request.getPath())
            assertEquals("POST", request.getMethod())
            val formParams = "confirmation_token=" + confirmationToken
            assertEquals(formParams, request.getBody().readUtf8())
        }

        fun request_should_post_login_email_with_email() {
            val request = mockWebServer.takeRequest()
            assertEquals("/people/me/login-email", request.getPath())
            assertEquals("POST", request.getMethod())
            assertEquals("email=e%40m.c", request.getBody().readUtf8())
        }

        fun request_should_post_login_with_token() {
            val request = mockWebServer.takeRequest()
            assertEquals("/people/me/login", request.getPath())
            assertEquals("POST", request.getMethod())
            assertEquals("token=" + loginToken, request.getBody().readUtf8())
        }

        fun request_should_get_client_versions() {
            val request = mockWebServer.takeRequest()
            assertEquals("/client-versions", request.getPath())
            assertEquals("GET", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun response_should_be_first_in_progress_then_auth_token() {
            testAuthTokenSubscriber.assertResult(ResultInProgress(),
                                                 ResultSuccess(AuthToken("868a2b9a", "9017c7e7")))
        }

        fun response_should_be_success() {
            testVoidSubscriber.assertResult(ResultSuccess())
        }

        fun response_should_be_inprogress_and_success() {
            testVoidSubscriber.assertResult(ResultInProgress(), ResultSuccess())
        }

        fun response_should_be_error() {
            testVoidSubscriber.assertResult(
                    ResultError(ClientException(source = "username", code = "not_allowed",
                            message = "Username not allowed")))
        }

        fun response_should_be_inprogress_and_invalid_confirmation_token_error() {
            testVoidSubscriber.assertResult(ResultInProgress(),
                    ResultError(ClientException(source = "confirmation_token", code = "invalid",
                            message = "Invalid confirmation token")))
        }

        fun response_should_be_in_progress_and_success_with_auth_token() {
            testAuthTokenSubscriber.assertResult(ResultInProgress(),
                    ResultSuccess(AuthToken("A_T_12345", "R_T_67890")))
        }

        fun response_should_return_inprogress_and_3() {
            testIntSubscriber.assertResult(ResultInProgress(), ResultSuccess(3))
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}