package com.abidria.data.experience

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.reactivex.subscribers.TestSubscriber
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ExperienceRepositoryTest {

    val mockWebServer = MockWebServer()
    var repository = ExperienceRepository(Retrofit.Builder()
                                                  .baseUrl(mockWebServer.url("/"))
                                                  .addConverterFactory( GsonConverterFactory.create(
                                                      GsonBuilder().setFieldNamingPolicy(
                                                                      FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                                                   .create()))
                                                  .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                                                  .build())

    @Test
    fun testGetExperiencesRequest() {
        val testSubscriber = TestSubscriber<List<Experience>>()
        mockWebServer.enqueue(MockResponse())

        repository.getExperiences().subscribe(testSubscriber)

        val request = mockWebServer.takeRequest()
        assertEquals("/experiences/", request.getPath())
        assertEquals("GET", request.getMethod())
        assertEquals("", request.getBody().readUtf8())
    }

    @Test
    fun testGetExperiencesResponseSuccess() {
        val testSubscriber = TestSubscriber<List<Experience>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
            ExperienceRepositoryTest::class.java.getResource("/api/GET_experiences.json").readText()
        ))

        repository.getExperiences().subscribe(testSubscriber)
        testSubscriber.awaitTerminalEvent()

        assertEquals(0, testSubscriber.events.get(1).size)
        assertEquals(1, testSubscriber.events.get(0).size)

        val receivedExperiences = testSubscriber.events.get(0).get(0) as List<*>

        val experience = receivedExperiences[0] as Experience
        assertEquals("2", experience.id)
        assertEquals("Baboon, el tío", experience.title)
        assertEquals("jeje", experience.description)
        assertEquals("https://experiences/8c29c4735.small.jpg", experience.picture!!.smallUrl)
        assertEquals("https://experiences/8c29c4735.medium.jpg", experience.picture!!.mediumUrl)
        assertEquals("https://experiences/8c29c4735.large.jpg", experience.picture!!.largeUrl)

        val secondExperience = receivedExperiences[1] as Experience
        assertEquals("3", secondExperience.id)
        assertEquals("Magic Castle of Lost Swamps", secondExperience.title)
        assertEquals("Don't try to go there!", secondExperience.description)
        assertNull(secondExperience.picture)
    }

    @Test
    fun testGetExperienceRequest() {
        val testSubscriber = TestSubscriber<Experience>()
        mockWebServer.enqueue(MockResponse())

        repository.getExperience(experienceId = "2").subscribe(testSubscriber)

        val request = mockWebServer.takeRequest()
        assertEquals("/experiences/", request.getPath())
        assertEquals("GET", request.getMethod())
        assertEquals("", request.getBody().readUtf8())
    }

    @Test
    fun testGetExperienceResponseSuccess() {
        val testSubscriber = TestSubscriber<Experience>()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                ExperienceRepositoryTest::class.java.getResource("/api/GET_experiences.json").readText()
        ))

        repository.getExperience(experienceId = "2").subscribe(testSubscriber)
        testSubscriber.awaitTerminalEvent()

        assertEquals(0, testSubscriber.events.get(1).size)
        assertEquals(1, testSubscriber.events.get(0).size)

        val experience = testSubscriber.events.get(0).get(0) as Experience

        assertEquals("2", experience.id)
        assertEquals("Baboon, el tío", experience.title)
        assertEquals("jeje", experience.description)
        assertEquals("https://experiences/8c29c4735.small.jpg", experience.picture!!.smallUrl)
        assertEquals("https://experiences/8c29c4735.medium.jpg", experience.picture!!.mediumUrl)
        assertEquals("https://experiences/8c29c4735.large.jpg", experience.picture!!.largeUrl)
    }
}
