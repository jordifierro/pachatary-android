package com.pachatary.data.auth

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test


class ClientVersionHttpInterceptorTest {

    @Test
    fun test_client_version_1() {
        given {
            a_web_server_that_returns_mock_response()
            an_app_version(1)
        } whenn {
            create_http_interceptor()
            make_any_call_with_auth_http_interceptor()
        } then {
            should_add_user_agent_header_with_And_001()
        }
    }

    @Test
    fun test_client_version_134() {
        given {
            a_web_server_that_returns_mock_response()
            an_app_version(134)
        } whenn {
            create_http_interceptor()
            make_any_call_with_auth_http_interceptor()
        } then {
            should_add_user_agent_header_with_And_134()
        }
    }

    @Test
    fun test_client_version_9999() {
        given {
            a_web_server_that_returns_mock_response()
            an_app_version(9999)
        } whenn {
            create_http_interceptor()
            make_any_call_with_auth_http_interceptor()
        } then {
            should_add_user_agent_header_with_And_9999()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var mockWebServer: MockWebServer
        lateinit var clientVersionHttpInterceptor: ClientVersionHttpInterceptor
        var versionCode: Int = 0

        fun a_web_server_that_returns_mock_response() {
            mockWebServer = MockWebServer()
            mockWebServer.start()
            mockWebServer.enqueue(MockResponse())
        }

        fun an_app_version(version: Int) {
            versionCode = version
        }

        fun create_http_interceptor() {
            clientVersionHttpInterceptor = ClientVersionHttpInterceptor(versionCode)
        }

        fun make_any_call_with_auth_http_interceptor() {
            val okHttpClient = OkHttpClient().newBuilder()
                    .addInterceptor(clientVersionHttpInterceptor)
                    .build()
            okHttpClient.newCall(
                    Request.Builder()
                            .url(mockWebServer.url("/"))
                            .build())
                    .execute()
        }

        fun should_add_user_agent_header_with_And_001() {
            val request = mockWebServer.takeRequest()
            assertEquals("And-001", request.getHeader("User-Agent"))
        }

        fun should_add_user_agent_header_with_And_134() {
            val request = mockWebServer.takeRequest()
            assertEquals("And-134", request.getHeader("User-Agent"))
        }

        fun should_add_user_agent_header_with_And_9999() {
            val request = mockWebServer.takeRequest()
            assertEquals("And-9999", request.getHeader("User-Agent"))
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}