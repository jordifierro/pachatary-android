package com.pachatary.data.profile

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultInProgress
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ProfileApiRepositoryTest {

    @Test
    fun test_get_my_profile() {
        given {
            a_web_server_that_returns_profile()
        } whenn {
            self_profile()
        } then {
            request_should_get_profile("self")
            response_should_be_inprogress_and_profile()
        }
    }

    @Test
    fun test_get_profile() {
        given {
            a_web_server_that_returns_profile()
        } whenn {
            profile("usr.nm")
        } then {
            request_should_get_profile("usr.nm")
            response_should_be_inprogress_and_profile()
        }
    }

    @Test
    fun test_patch_profile() {
        given {
            a_web_server_that_returns_profile()
        } whenn {
            edit_profile("description")
        } then {
            request_should_patch_profile("bio description")
            response_should_be_inprogress_and_profile()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        val testSubscriber = TestSubscriber<Result<Profile>>()
        val mockWebServer = MockWebServer()
        val repository = ProfileApiRepository(Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create(
                        GsonBuilder().setFieldNamingPolicy(
                                FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build(),
                Schedulers.trampoline())

        fun a_web_server_that_returns_profile() {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                    ProfileApiRepositoryTest::class.java.getResource(
                            "/api/profile.json").readText()))
        }

        fun self_profile() {
            repository.selfProfile()
                    .subscribe(testSubscriber)
        }

        fun profile(username: String) {
            repository.profile(username)
                    .subscribe(testSubscriber)
        }

        fun edit_profile(bio: String) {
            repository.editProfile(bio)
                    .subscribe(testSubscriber)
        }

        fun request_should_get_profile(username: String) {
            val request = mockWebServer.takeRequest()
            assertEquals("/profiles/" + username, request.path)
            assertEquals("GET", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun request_should_patch_profile(bio: String) {
            val request = mockWebServer.takeRequest()
            assertEquals("/profiles/self", request.path)
            assertEquals("PATCH", request.getMethod())
            assertEquals("bio=description", request.getBody().readUtf8())
        }

        fun response_should_be_inprogress_and_profile() {
            testSubscriber.awaitCount(2)

            val firstResult = testSubscriber.events.get(0).get(0) as Result<*>
            assertEquals(ResultInProgress<Profile>(), firstResult)

            val secondResult = testSubscriber.events.get(0).get(1) as Result<*>
            val profile = secondResult.data as Profile

            assertEquals("usr.nm", profile.username)
            assertEquals("bio description", profile.bio)
            assertEquals("https://experiences/8c29.tiny.jpg", profile.picture!!.tinyUrl)
            assertEquals("https://experiences/8c29.small.jpg", profile.picture!!.smallUrl)
            assertEquals("https://experiences/8c29.medium.jpg", profile.picture!!.mediumUrl)
            assertFalse(profile.isMe)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
