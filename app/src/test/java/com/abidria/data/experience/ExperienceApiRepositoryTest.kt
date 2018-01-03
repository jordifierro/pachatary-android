package com.abidria.data.experience

import android.content.Context
import com.abidria.data.auth.AuthHttpInterceptor
import com.abidria.data.common.Result
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ExperienceApiRepositoryTest {

    @Test
    fun test_get_my_experiences() {
        given {
            a_web_server_that_returns_get_experiences()
        } whenn {
            my_experiences_are_requested()
        } then {
            request_should_get_experiences(mine = true)
            response_should_experience_list(isMine = true)
        }
    }

    @Test
    fun test_get_explore_experiences() {
        given {
            a_web_server_that_returns_get_experiences()
        } whenn {
            explore_experiences_are_requested()
        } then {
            request_should_get_experiences()
            response_should_experience_list()
        }
    }

    @Test
    fun test_get_saved_experiences() {
        given {
            a_web_server_that_returns_get_experiences()
        } whenn {
            saved_experiences_are_requested()
        } then {
            request_should_get_experiences(saved = true)
            response_should_experience_list(isSaved = true)
        }
    }

    @Test
    fun test_post_experiences() {
        given {
            an_experience()
            a_web_server_that_returns_post_experiences()
        } whenn {
            experience_is_created()
        } then {
            request_should_post_experience_attrs()
            response_should_parse_experience()
        }
    }

    @Test
    fun test_patch_experiences() {
        given {
            an_experience()
            a_web_server_that_returns_patch_experiences()
        } whenn {
            experience_is_edited()
        } then {
            request_should_patch_experience_attrs()
            response_should_parse_experience()
        }
    }

    @Test
    fun test_save_experience() {
        given {
            an_experience()
            a_web_server_that_returns_201()
        } whenn {
            experience_is_saved()
        } then {
            request_should_post_experience_id_save()
            response_should_parse_empty_body()
        }
    }

    @Test
    fun test_unsave_experience() {
        given {
            an_experience()
            a_web_server_that_returns_204()
        } whenn {
            experience_is_unsaved()
        } then {
            request_should_delete_experience_id_save()
            response_should_parse_empty_body()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        val testListSubscriber = TestSubscriber<Result<List<Experience>>>()
        val testSubscriber = TestSubscriber<Result<Experience>>()
        val testEmptySubscriber = TestSubscriber<Result<Void>>()
        val mockContext = mock(Context::class.java)
        val mockAuthHttpInterceptor = mock(AuthHttpInterceptor::class.java)
        val mockWebServer = MockWebServer()
        val repository = ExperienceApiRepository(Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create(
                        GsonBuilder().setFieldNamingPolicy(
                                FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build(),
                Schedulers.trampoline(), mockContext, mockAuthHttpInterceptor)
        lateinit var experience: Experience

        fun a_web_server_that_returns_get_experiences() {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                    ExperienceApiRepositoryTest::class.java.getResource("/api/GET_experiences.json").readText()))
        }

        fun an_experience() {
            experience = Experience(id = "1", title = "T", description = "desc", picture = null)
        }

        fun a_web_server_that_returns_post_experiences() {
            mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                    ExperienceApiRepositoryTest::class.java.getResource("/api/POST_experiences.json").readText()))
        }

        fun a_web_server_that_returns_patch_experiences() {
            mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                    ExperienceApiRepositoryTest::class.java.getResource("/api/PATCH_experience_id.json").readText()))
        }

        fun a_web_server_that_returns_201() {
            mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(""))
        }

        fun a_web_server_that_returns_204() {
            mockWebServer.enqueue(MockResponse().setResponseCode(204).setBody(""))
        }

        fun my_experiences_are_requested() {
            repository.myExperiencesFlowable().subscribeOn(Schedulers.trampoline()).subscribe(testListSubscriber)
            testListSubscriber.awaitCount(1)
        }

        fun explore_experiences_are_requested() {
            repository.exploreExperiencesFlowable().subscribeOn(Schedulers.trampoline()).subscribe(testListSubscriber)
            testListSubscriber.awaitCount(1)
        }

        fun saved_experiences_are_requested() {
            repository.savedExperiencesFlowable().subscribeOn(Schedulers.trampoline()).subscribe(testListSubscriber)
            testListSubscriber.awaitCount(1)
        }

        fun experience_is_created() {
            repository.createExperience(experience).subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
        }

        fun experience_is_edited() {
            repository.editExperience(experience).subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
        }

        fun experience_is_saved() {
            repository.saveExperience(save = true, experienceId = experience.id).subscribe(testEmptySubscriber)
            testEmptySubscriber.awaitCount(1)
        }

        fun experience_is_unsaved() {
            repository.saveExperience(save = false, experienceId = experience.id).subscribe(testEmptySubscriber)
            testEmptySubscriber.awaitCount(1)
        }

        fun request_should_get_experiences(mine: Boolean = false, saved: Boolean = false) {
            val request = mockWebServer.takeRequest()
            if (saved) assertEquals("/experiences/?saved=true", request.getPath())
            else if (mine) assertEquals("/experiences/?mine=true", request.getPath())
            else assertEquals("/experiences/?mine=false", request.getPath())
            assertEquals("GET", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun request_should_post_experience_attrs() {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/", request.getPath())
            assertEquals("POST", request.getMethod())
            assertEquals("title=T&description=desc",
                    request.getBody().readUtf8())
        }

        fun request_should_patch_experience_attrs() {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/1", request.getPath())
            assertEquals("PATCH", request.getMethod())
            assertEquals("title=T&description=desc", request.getBody().readUtf8())
        }

        fun request_should_post_experience_id_save() {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/" + experience.id + "/save/", request.getPath())
            assertEquals("POST", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun request_should_delete_experience_id_save() {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/" + experience.id + "/save/", request.getPath())
            assertEquals("DELETE", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun response_should_experience_list(isMine: Boolean = false, isSaved: Boolean = false) {
            val result = testListSubscriber.events.get(0).get(0) as Result<*>
            val experiences = result.data as List<*>

            val experience = experiences[0] as Experience
            assertEquals("2", experience.id)
            assertEquals("Baboon, el tío", experience.title)
            assertEquals("jeje", experience.description)
            assertEquals("https://experiences/8c29c4735.small.jpg", experience.picture!!.smallUrl)
            assertEquals("https://experiences/8c29c4735.medium.jpg", experience.picture!!.mediumUrl)
            assertEquals("https://experiences/8c29c4735.large.jpg", experience.picture!!.largeUrl)
            assertEquals(isMine, experience.isMine)
            assertEquals(isSaved, experience.isSaved)

            val secondExperience = experiences[1] as Experience
            assertEquals("3", secondExperience.id)
            assertEquals("Magic Castle of Lost Swamps", secondExperience.title)
            assertEquals("Don't try to go there!", secondExperience.description)
            assertEquals(isMine, experience.isMine)
            assertEquals(isSaved, experience.isSaved)
            assertNull(secondExperience.picture)
        }

        fun response_should_parse_experience() {
            val receivedResult = testSubscriber.events.get(0).get(0) as Result<*>
            val receivedExperience = receivedResult.data as Experience

            assertEquals("4", receivedExperience.id)
            assertEquals("Plaça", receivedExperience.title)
            assertEquals("", receivedExperience.description)
            assertEquals("https://experiences/00df.small.jpeg", receivedExperience.picture!!.smallUrl)
            assertEquals("https://experiences/00df.medium.jpeg", receivedExperience.picture!!.mediumUrl)
            assertEquals("https://experiences/00df.large.jpeg", receivedExperience.picture!!.largeUrl)
        }

        fun response_should_parse_empty_body() {
            val receivedResult = testEmptySubscriber.events.get(0).get(0) as Result<*>
            assertEquals(Result(null, null), receivedResult)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
