package com.pachatary.data.auth

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mockito.mock


class AuthHttpInterceptorTest {

    @Test
    fun when_request_is_made_adds_authorization_header() {
        given {
            a_web_server_that_returns_mock_response()
            an_auth_storage_repository_that_returns_an_auth_token()
        } whenn {
            make_any_call_with_auth_http_interceptor()
        } then {
            should_add_authorization_header_with_access_token()
        }
    }
    
    @Test
    fun when_no_credentials_adds_nothing() {
        given {
            a_web_server_that_returns_mock_response()
            an_auth_storage_repository_that_raises_no_logged_exception()
        } whenn {
            make_any_call_with_auth_http_interceptor()
        } then {
            should_made_request_but_add_nothing()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var mockWebServer: MockWebServer
        lateinit var authStorageRepository: AuthStorageRepository
        lateinit var authToken: AuthToken

        fun a_web_server_that_returns_mock_response() {
            mockWebServer = MockWebServer()
            mockWebServer.start()
            mockWebServer.enqueue(MockResponse())
        }
        
        fun an_auth_storage_repository_that_returns_an_auth_token() {
            authStorageRepository = mock(AuthStorageRepository::class.java) 
            authToken = AuthToken(accessToken = "A", refreshToken = "R")    
            BDDMockito.given(authStorageRepository.getPersonCredentials()).willReturn(authToken)
        }
        
        fun an_auth_storage_repository_that_raises_no_logged_exception() {
            authStorageRepository = mock(AuthStorageRepository::class.java)
            BDDMockito.given(authStorageRepository.getPersonCredentials()).willThrow(NoLoggedException("Error"))
        }
        
        fun make_any_call_with_auth_http_interceptor() {
            val okHttpClient = OkHttpClient().newBuilder()
                    .addInterceptor(AuthHttpInterceptor(authStorageRepository))
                    .build()
            okHttpClient.newCall(
                    Request.Builder()
                            .url(mockWebServer.url("/"))
                            .build())
                    .execute()
        }
        
        fun should_add_authorization_header_with_access_token() {
            val request = mockWebServer.takeRequest()
            assertEquals("Token " + authToken.accessToken, request.getHeader("Authorization"))
        }
        
        fun should_made_request_but_add_nothing() {
            assertNull(mockWebServer.takeRequest().getHeader("Authorization"))
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}