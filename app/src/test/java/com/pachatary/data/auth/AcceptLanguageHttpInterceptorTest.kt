package com.pachatary.data.auth

import com.pachatary.data.common.AcceptLanguageHttpInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test


class AcceptLanguageHttpInterceptorTest {

    @Test
    fun test_accept_language_header_is_added() {
        given {
            a_web_server_that_returns_mock_response()
            a_device_language("es")
        } whenn {
            create_http_interceptor()
            make_any_call_with_auth_http_interceptor()
        } then {
            should_add_accept_language_header_with("es")
        }
    }

    @Test
    fun test_accept_language_header_replaces_underscores_with_slashes() {
        given {
            a_web_server_that_returns_mock_response()
            a_device_language("en_US")
        } whenn {
            create_http_interceptor()
            make_any_call_with_auth_http_interceptor()
        } then {
            should_add_accept_language_header_with("en-US")
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var mockWebServer: MockWebServer
        lateinit var acceptLanguageHttpInterceptor: AcceptLanguageHttpInterceptor
        var language = ""

        fun a_web_server_that_returns_mock_response() {
            mockWebServer = MockWebServer()
            mockWebServer.start()
            mockWebServer.enqueue(MockResponse())
        }

        fun a_device_language(language: String) {
            this.language = language
        }

        fun create_http_interceptor() {
            acceptLanguageHttpInterceptor = AcceptLanguageHttpInterceptor(language)
        }

        fun make_any_call_with_auth_http_interceptor() {
            val okHttpClient = OkHttpClient().newBuilder()
                    .addInterceptor(acceptLanguageHttpInterceptor)
                    .build()
            okHttpClient.newCall(
                    Request.Builder()
                            .url(mockWebServer.url("/"))
                            .build())
                    .execute()
        }

        fun should_add_accept_language_header_with(language: String) {
            assertEquals(language, mockWebServer.takeRequest().getHeader("Accept-Language"))
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}