package com.abidria.data.experience

import com.abidria.data.common.Result
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ExperienceApiRepositoryTest {

    val mockWebServer = MockWebServer()
    val repository = ExperienceApiRepository(Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(
                    GsonBuilder().setFieldNamingPolicy(
                            FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build(),
            Schedulers.trampoline())

    @Test
    fun testGetExperiencesRequest() {
        val testSubscriber = TestSubscriber<Result<List<Experience>>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/GET_experiences.json").readText()))

        repository.experiencesFlowable().subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        val request = mockWebServer.takeRequest()
        assertEquals("/experiences/", request.getPath())
        assertEquals("GET", request.getMethod())
        assertEquals("", request.getBody().readUtf8())
    }

    @Test
    fun testGetExperiencesResponseSuccess() {
        val testSubscriber = TestSubscriber<Result<List<Experience>>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/GET_experiences.json").readText()))

        repository.experiencesFlowable().subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        assertEquals(0, testSubscriber.events.get(1).size)
        assertEquals(1, testSubscriber.events.get(0).size)

        val result = testSubscriber.events.get(0).get(0) as Result<*>
        val experiences = result.data as List<*>

        val experience = experiences[0] as Experience
        assertEquals("2", experience.id)
        assertEquals("Baboon, el tío", experience.title)
        assertEquals("jeje", experience.description)
        assertEquals("https://experiences/8c29c4735.small.jpg", experience.picture!!.smallUrl)
        assertEquals("https://experiences/8c29c4735.medium.jpg", experience.picture!!.mediumUrl)
        assertEquals("https://experiences/8c29c4735.large.jpg", experience.picture!!.largeUrl)

        val secondExperience = experiences[1] as Experience
        assertEquals("3", secondExperience.id)
        assertEquals("Magic Castle of Lost Swamps", secondExperience.title)
        assertEquals("Don't try to go there!", secondExperience.description)
        assertNull(secondExperience.picture)
    }

    @Test
    fun testResponseErrorRetriesThriceAndCrashes() {
        val testSubscriber = TestSubscriber<Result<List<Experience>>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        repository.experiencesFlowable().subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        assertEquals(1, testSubscriber.events.get(1).size)
        assertEquals(0, testSubscriber.events.get(0).size)
        assertEquals(3, mockWebServer.requestCount)
    }

    @Test
    fun testEmitThroughRefresherAsksAgain() {
        val testSubscriber = TestSubscriber<Result<List<Experience>>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/GET_experiences.json").readText()))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/GET_experiences.json").readText()))

        repository.experiencesFlowable().subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        repository.refreshExperiences()
        testSubscriber.awaitCount(2)

        assertEquals(0, testSubscriber.events.get(1).size)
        assertEquals(2, testSubscriber.events.get(0).size)

        val firstResult = testSubscriber.events.get(0).get(0) as Result<*>
        assertTrue(firstResult.isSuccess())

        val secondResult = testSubscriber.events.get(0).get(1) as Result<*>
        val experiences = secondResult.data as List<*>

        val experience = experiences[0] as Experience
        assertEquals("2", experience.id)
        assertEquals("Baboon, el tío", experience.title)
        assertEquals("jeje", experience.description)
        assertEquals("https://experiences/8c29c4735.small.jpg", experience.picture!!.smallUrl)
        assertEquals("https://experiences/8c29c4735.medium.jpg", experience.picture!!.mediumUrl)
        assertEquals("https://experiences/8c29c4735.large.jpg", experience.picture!!.largeUrl)

        val secondExperience = experiences[1] as Experience
        assertEquals("3", secondExperience.id)
        assertEquals("Magic Castle of Lost Swamps", secondExperience.title)
        assertEquals("Don't try to go there!", secondExperience.description)
        assertNull(secondExperience.picture)
    }
}
