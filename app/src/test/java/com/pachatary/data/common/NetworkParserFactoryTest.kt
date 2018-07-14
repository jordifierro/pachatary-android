package com.pachatary.data.common

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.pachatary.data.auth.AuthHttpInterceptor
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceApiRepository
import com.pachatary.data.experience.ExperienceApiRepositoryTest
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class NetworkParserFactoryTest {

    @Test
    fun test_get_my_experiences_success() {
        given {
            a_web_server_that_returns_get_experiences()
        } whenn {
            my_experiences_are_requested()
        } then {
            request_should_get_person_experiences("self")
            response_should_experience_list_and_next_url()
        }
    }

    @Test
    fun test_get_my_experiences_unknown_host_retries_3_and_returns_result_error() {
        given {

        } whenn {
            my_experiences_are_requested_on_unknown_host_server()
        } then {
            should_respond_result_error_unknown_host_exception()
            should_make_3_calls()
        }
    }

    @Test
    fun test_get_my_experiences_timeout_retries_3_and_returns_result_error() {
        given {

        } whenn {
            my_experiences_are_requested_on_slow_server()
        } then {
            should_respond_result_error_timeout()
            should_make_3_calls()
        }
    }

    @Test
    fun test_get_my_experiences_error_crashes() {
        given {

        } whenn {
            my_experiences_are_requested_with_shutdown_server()
        } then {
            should_throw_error()
            should_make_3_calls()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        val testListSubscriber = TestSubscriber<Result<List<Experience>>>()
        val mockAuthHttpInterceptor = mock(AuthHttpInterceptor::class.java)
        val mockContext = mock(Context::class.java)
        val mockWebServer = MockWebServer()
        var logInterceptor = LoggerHttpInterceptor()
        val repository = ExperienceApiRepository(Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create(
                        GsonBuilder().setFieldNamingPolicy(
                                FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(OkHttpClient.Builder().addInterceptor(logInterceptor)
                                              .readTimeout(10, TimeUnit.MILLISECONDS).build())
                .build(),
                Schedulers.trampoline(), mockContext, mockAuthHttpInterceptor)
        val unknownHostRetrofit = Retrofit.Builder()
                .baseUrl("http://qwerty.skdjwnfkslwjrndlwjr.asdf/")
                .addConverterFactory(GsonConverterFactory.create(
                        GsonBuilder().setFieldNamingPolicy(
                                FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(OkHttpClient.Builder().addInterceptor(logInterceptor).build())
                .build()
        val unknownHostRepo = ExperienceApiRepository(unknownHostRetrofit,
                Schedulers.trampoline(), mockContext, mockAuthHttpInterceptor)

        fun buildScenario(): ScenarioMaker {
            return this
        }

        fun a_web_server_that_returns_get_experiences() {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                    ExperienceApiRepositoryTest::class.java.getResource(
                            "/api/GET_experiences.json").readText()))
        }

        fun my_experiences_are_requested() {
            repository.myExperiencesFlowable()
                    .subscribeOn(Schedulers.trampoline())
                    .subscribe(testListSubscriber)
        }

        fun my_experiences_are_requested_with_shutdown_server() {
            mockWebServer.shutdown()
            repository.myExperiencesFlowable()
                    .subscribeOn(Schedulers.trampoline())
                    .subscribe(testListSubscriber)
        }

        fun my_experiences_are_requested_on_unknown_host_server() {
            unknownHostRepo.myExperiencesFlowable()
                    .subscribeOn(Schedulers.trampoline())
                    .subscribe(testListSubscriber)
        }

        fun my_experiences_are_requested_on_slow_server() {
            mockWebServer.enqueue(MockResponse().setBodyDelay(20, TimeUnit.MILLISECONDS))
            repository.myExperiencesFlowable()
                    .subscribeOn(Schedulers.trampoline())
                    .subscribe(testListSubscriber)
        }

        fun request_should_get_person_experiences(username: String) {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/?username=" + username, request.path)
            assertEquals("GET", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun response_should_experience_list_and_next_url() {
            testListSubscriber.awaitCount(1)
            val result = testListSubscriber.events.get(0).get(0) as Result<*>
            val experiences = result.data as List<*>

            val experience = experiences[0] as Experience
            assertEquals("2", experience.id)
            assertEquals("Baboon, el tío", experience.title)
            assertEquals("jeje", experience.description)
            assertEquals("https://experiences/8c29c4735.small.jpg", experience.picture!!.smallUrl)
            assertEquals("https://experiences/8c29c4735.medium.jpg", experience.picture!!.mediumUrl)
            assertEquals("https://experiences/8c29c4735.large.jpg", experience.picture!!.largeUrl)
            assertEquals(true, experience.isMine)
            assertEquals(false, experience.isSaved)
            assertEquals("da_usr", experience.authorProfile.username)
            assertEquals("about me", experience.authorProfile.bio)
            assertNull(experience.authorProfile.picture)
            assertTrue(experience.authorProfile.isMe)
            assertEquals(4, experience.savesCount)

            val secondExperience = experiences[1] as Experience
            assertEquals("3", secondExperience.id)
            assertEquals("Magic Castle of Lost Swamps", secondExperience.title)
            assertEquals("Don't try to go there!", secondExperience.description)
            assertEquals(false, secondExperience.isMine)
            assertEquals(true, secondExperience.isSaved)
            assertNull(secondExperience.picture)
            assertEquals("usr.nam", secondExperience.authorProfile.username)
            assertEquals("user info", secondExperience.authorProfile.bio)
            assertEquals("https://experiences/029d.tiny.jpg",
                    secondExperience.authorProfile.picture!!.tinyUrl)
            assertEquals("https://experiences/029d.small.jpg",
                    secondExperience.authorProfile.picture!!.smallUrl)
            assertEquals("https://experiences/029d.medium.jpg",
                    secondExperience.authorProfile.picture!!.mediumUrl)
            assertFalse(secondExperience.authorProfile.isMe)
            assertEquals(7, secondExperience.savesCount)

            assertEquals("https://next_url", result.nextUrl)
        }

        fun should_respond_result_error_unknown_host_exception() {
            testListSubscriber.awaitCount(1)
            val result = testListSubscriber.events.get(0).get(0) as Result<*>
            assert(result.isError())
            assert(result.error is UnknownHostException)
        }

        fun should_respond_result_error_timeout() {
            testListSubscriber.awaitCount(1)
            val result = testListSubscriber.events.get(0).get(0) as Result<*>
            assert(result.isError())
            assert(result.error is SocketTimeoutException)
        }

        fun should_make_3_calls() {
            assertEquals(logInterceptor.calls, 3)
        }

        fun should_throw_error() {
            testListSubscriber.awaitCount(1)
            testListSubscriber.assertNoValues()
            val error = testListSubscriber.events.get(1).get(0) as Throwable
            assert(error is ConnectException)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }

    class LoggerHttpInterceptor : Interceptor {

        var calls = 0

        override fun intercept(chain: Interceptor.Chain): Response {
            calls += 1
            return chain.proceed(chain.request())
        }
    }
}
