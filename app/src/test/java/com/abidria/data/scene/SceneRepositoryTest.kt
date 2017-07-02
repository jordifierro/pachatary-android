package com.abidria.data.scene

import com.abidria.data.experience.ExperienceRepositoryTest
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

class SceneRepositoryTest {

    val mockWebServer = MockWebServer()
    var repository = SceneRepository(Retrofit.Builder()
                                             .baseUrl(mockWebServer.url("/"))
                                             .addConverterFactory( GsonConverterFactory.create(
                                                 GsonBuilder().setFieldNamingPolicy(
                                                                 FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                                              .create()))
                                             .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                                             .build())

    @Test
    fun testGetScenesRequest() {
        val testSubscriber = TestSubscriber<List<Scene>>()
        mockWebServer.enqueue(MockResponse())

        repository.getScenes("7").subscribe(testSubscriber)

        val request = mockWebServer.takeRequest()
        assertEquals("/scenes/?experience=7", request.getPath())
        assertEquals("GET", request.getMethod())
        assertEquals("", request.getBody().readUtf8())
    }

    @Test
    fun testGetScenesResponseSuccess() {
        val testSubscriber = TestSubscriber<List<Scene>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                ExperienceRepositoryTest::class.java.getResource("/api/GET_scenes_?experience.json").readText()
        ))

        repository.getScenes(experienceId = "").subscribe(testSubscriber)
        testSubscriber.awaitTerminalEvent()

        assertEquals(0, testSubscriber.events.get(1).size)
        assertEquals(1, testSubscriber.events.get(0).size)

        val receivedScenes = testSubscriber.events.get(0).get(0) as List<*>

        val firstScene = receivedScenes[0] as Scene
        assertEquals("4", firstScene.id)
        assertEquals("Plaça", firstScene.title)
        assertEquals("", firstScene.description)
        assertEquals("https://scenes/00df.small.jpeg", firstScene.picture!!.smallUrl)
        assertEquals("https://scenes/00df.medium.jpeg", firstScene.picture!!.mediumUrl)
        assertEquals("https://scenes/00df.large.jpeg", firstScene.picture!!.largeUrl)
        assertEquals(41.364679, firstScene.latitude, 1e-15)
        assertEquals(2.135489, firstScene.longitude, 1e-15)
        assertEquals("5", firstScene.experienceId)

        val secondScene = receivedScenes[1] as Scene
        assertEquals("3", secondScene.id)
        assertEquals("Barri", secondScene.title)
        assertEquals("Lorem ipsum dolor sit amet", secondScene.description)
        assertNull(secondScene.picture)
        assertEquals(41.392682, secondScene.latitude, 1e-15)
        assertEquals(2.144423, secondScene.longitude, 1e-15)
        assertEquals("5", secondScene.experienceId)
    }
}
