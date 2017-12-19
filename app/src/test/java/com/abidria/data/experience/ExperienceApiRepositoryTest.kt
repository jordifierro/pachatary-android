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
    fun testCreateExperienceRequest() {
        val testSubscriber = TestSubscriber<Result<Experience>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/POST_experiences.json").readText()))
        val experience = Experience(id = "1", title = "T", description = "desc", picture = null)

        repository.createExperience(experience).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        val request = mockWebServer.takeRequest()
        assertEquals("/experiences/", request.getPath())
        assertEquals("POST", request.getMethod())
        assertEquals("title=T&description=desc",
                request.getBody().readUtf8())
    }

    @Test
    fun testCreateExperienceResponseSuccess() {
        val testSubscriber = TestSubscriber<Result<Experience>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/POST_experiences.json").readText()))

        val experience = Experience(id = "1", title = "T", description = "desc", picture = null)

        repository.createExperience(experience).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        assertEquals(0, testSubscriber.events.get(1).size)
        assertEquals(1, testSubscriber.events.get(0).size)

        val receivedResult = testSubscriber.events.get(0).get(0) as Result<*>
        val receivedExperience = receivedResult.data as Experience

        assertEquals("4", receivedExperience.id)
        assertEquals("Plaça", receivedExperience.title)
        assertEquals("", receivedExperience.description)
        assertEquals("https://experiences/00df.small.jpeg", receivedExperience.picture!!.smallUrl)
        assertEquals("https://experiences/00df.medium.jpeg", receivedExperience.picture!!.mediumUrl)
        assertEquals("https://experiences/00df.large.jpeg", receivedExperience.picture!!.largeUrl)
    }

    @Test
    fun testEditExperienceRequest() {
        val testSubscriber = TestSubscriber<Result<Experience>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/PATCH_experience_id.json").readText()))
        val experience = Experience(id = "1", title = "T", description = "desc", picture = null)

        repository.editExperience(experience).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        val request = mockWebServer.takeRequest()
        assertEquals("/experiences/1", request.getPath())
        assertEquals("PATCH", request.getMethod())
        assertEquals("title=T&description=desc",
                request.getBody().readUtf8())
    }

    @Test
    fun testEditExperienceResponseSuccess() {
        val testSubscriber = TestSubscriber<Result<Experience>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/PATCH_experience_id.json").readText()))

        val experience = Experience(id = "1", title = "T", description = "desc", picture = null)

        repository.editExperience(experience).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        assertEquals(0, testSubscriber.events.get(1).size)
        assertEquals(1, testSubscriber.events.get(0).size)

        val receivedResult = testSubscriber.events.get(0).get(0) as Result<*>
        val receivedExperience = receivedResult.data as Experience

        assertEquals("4", receivedExperience.id)
        assertEquals("Plaça", receivedExperience.title)
        assertEquals("", receivedExperience.description)
        assertEquals("https://experiences/00df.small.jpeg", receivedExperience.picture!!.smallUrl)
        assertEquals("https://experiences/00df.medium.jpeg", receivedExperience.picture!!.mediumUrl)
        assertEquals("https://experiences/00df.large.jpeg", receivedExperience.picture!!.largeUrl)
    }
}
