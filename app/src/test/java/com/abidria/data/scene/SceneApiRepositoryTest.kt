package com.abidria.data.scene

import com.abidria.data.common.Result
import com.abidria.data.experience.ExperienceApiRepositoryTest
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class SceneApiRepositoryTest {

    val mockWebServer = MockWebServer()
    var repository = SceneApiRepository(Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(
                    GsonBuilder().setFieldNamingPolicy(
                            FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build(),
            Schedulers.trampoline())

    @Test
    fun testGetScenesRequest() {
        val testSubscriber = TestSubscriber<Result<List<Scene>>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/GET_scenes_?experience.json").readText()))

        repository.scenesFlowableAndRefreshObserver("7").first.subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        val request = mockWebServer.takeRequest()
        assertEquals("/scenes/?experience=7", request.getPath())
        assertEquals("GET", request.getMethod())
        assertEquals("", request.getBody().readUtf8())
    }

    @Test
    fun testGetScenesResponseSuccess() {
        val testSubscriber = TestSubscriber<Result<List<Scene>>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/GET_scenes_?experience.json").readText()))

        repository.scenesFlowableAndRefreshObserver(experienceId = "").first.subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        assertEquals(0, testSubscriber.events.get(1).size)
        assertEquals(1, testSubscriber.events.get(0).size)

        val receivedResult = testSubscriber.events.get(0).get(0) as Result<*>
        val receivedScenes = receivedResult.data as List<*>

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

    @Test
    fun testGetScenesRetriesThriceAndCrashes() {
        val testSubscriber = TestSubscriber<Result<List<Scene>>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        repository.scenesFlowableAndRefreshObserver(experienceId = "").first.subscribe(testSubscriber)
        testSubscriber.awaitCount(3)

        assertEquals(1, testSubscriber.events.get(1).size)
        assertEquals(0, testSubscriber.events.get(0).size)
    }

    @Test
    fun testGetScenesRefresherAsksAgain() {
        val testSubscriber = TestSubscriber<Result<List<Scene>>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
            ExperienceApiRepositoryTest::class.java.getResource("/api/GET_scenes_?experience.json").readText()))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
            ExperienceApiRepositoryTest::class.java.getResource("/api/GET_scenes_?experience.json").readText()))

        val scenesFlowableAndRefresher = repository.scenesFlowableAndRefreshObserver(experienceId = "2")
        scenesFlowableAndRefresher.first.subscribe(testSubscriber)
        scenesFlowableAndRefresher.second.onNext(Any())
        testSubscriber.awaitCount(2)

        assertEquals(0, testSubscriber.events.get(1).size)
        assertEquals(2, testSubscriber.events.get(0).size)
    }
}
