package com.pachatary.data.scene

import android.content.Context
import com.pachatary.data.auth.AuthHttpInterceptor
import com.pachatary.data.common.Result
import com.pachatary.data.experience.ExperienceApiRepositoryTest
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class SceneApiRepositoryTest {

    val mockContext = mock(Context::class.java)
    val mockAuthHttpInterceptor = mock(AuthHttpInterceptor::class.java)
    val mockWebServer = MockWebServer()
    var repository = SceneApiRepository(Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(
                    GsonBuilder().setFieldNamingPolicy(
                            FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build(),
            Schedulers.trampoline(), mockContext, mockAuthHttpInterceptor)

    @Test
    fun testGetScenesRequest() {
        val testSubscriber = TestSubscriber<Result<List<Scene>>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/GET_scenes_?experience.json").readText()))

        repository.scenesRequestFlowable("7").subscribe(testSubscriber)
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

        repository.scenesRequestFlowable(experienceId = "").subscribe(testSubscriber)
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

        repository.scenesRequestFlowable(experienceId = "").subscribe(testSubscriber)
        testSubscriber.awaitCount(3)

        assertEquals(1, testSubscriber.events.get(1).size)
        assertEquals(0, testSubscriber.events.get(0).size)
    }

    @Test
    fun testCreateSceneRequest() {
        val testSubscriber = TestSubscriber<Result<Scene>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/POST_scenes.json").readText()))
        val scene = Scene(id = "1", title = "T", description = "desc",
                          latitude = 1.0, longitude = -2.3, experienceId = "3", picture = null)

        repository.createScene(scene).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        val request = mockWebServer.takeRequest()
        assertEquals("/scenes/", request.getPath())
        assertEquals("POST", request.getMethod())
        assertEquals("title=T&description=desc&latitude=1.0&longitude=-2.3&experience_id=3",
                     request.getBody().readUtf8())
    }

    @Test
    fun testCreateSceneResponseSuccess() {
        val testSubscriber = TestSubscriber<Result<Scene>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/POST_scenes.json").readText()))

        val scene = Scene(id = "1", title = "T", description = "desc",
                latitude = 1.0, longitude = -2.3, experienceId = "3", picture = null)

        repository.createScene(scene).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        assertEquals(0, testSubscriber.events.get(1).size)
        assertEquals(1, testSubscriber.events.get(0).size)

        val receivedResult = testSubscriber.events.get(0).get(0) as Result<*>
        val receivedScene = receivedResult.data as Scene

        assertEquals("4", receivedScene.id)
        assertEquals("Plaça", receivedScene.title)
        assertEquals("", receivedScene.description)
        assertEquals("https://scenes/00df.small.jpeg", receivedScene.picture!!.smallUrl)
        assertEquals("https://scenes/00df.medium.jpeg", receivedScene.picture!!.mediumUrl)
        assertEquals("https://scenes/00df.large.jpeg", receivedScene.picture!!.largeUrl)
        assertEquals(41.364679, receivedScene.latitude, 1e-15)
        assertEquals(2.135489, receivedScene.longitude, 1e-15)
        assertEquals("5", receivedScene.experienceId)
    }

    @Test
    fun testEditSceneRequest() {
        val testSubscriber = TestSubscriber<Result<Scene>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/PATCH_scene_id.json").readText()))
        val scene = Scene(id = "1", title = "T", description = "desc",
                          latitude = 1.0, longitude = -2.3, experienceId = "3", picture = null)

        repository.editScene(scene).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        val request = mockWebServer.takeRequest()
        assertEquals("/scenes/1", request.getPath())
        assertEquals("PATCH", request.getMethod())
        assertEquals("title=T&description=desc&latitude=1.0&longitude=-2.3&experience_id=3",
                request.getBody().readUtf8())
    }

    @Test
    fun testEditSceneResponseSuccess() {
        val testSubscriber = TestSubscriber<Result<Scene>>()
        mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                ExperienceApiRepositoryTest::class.java.getResource("/api/PATCH_scene_id.json").readText()))

        val scene = Scene(id = "1", title = "T", description = "desc",
                          latitude = 1.0, longitude = -2.3, experienceId = "3", picture = null)

        repository.editScene(scene).subscribe(testSubscriber)
        testSubscriber.awaitCount(1)

        assertEquals(0, testSubscriber.events.get(1).size)
        assertEquals(1, testSubscriber.events.get(0).size)

        val receivedResult = testSubscriber.events.get(0).get(0) as Result<*>
        val receivedScene = receivedResult.data as Scene

        assertEquals("4", receivedScene.id)
        assertEquals("Plaça", receivedScene.title)
        assertEquals("", receivedScene.description)
        assertEquals("https://scenes/00df.small.jpeg", receivedScene.picture!!.smallUrl)
        assertEquals("https://scenes/00df.medium.jpeg", receivedScene.picture!!.mediumUrl)
        assertEquals("https://scenes/00df.large.jpeg", receivedScene.picture!!.largeUrl)
        assertEquals(41.364679, receivedScene.latitude, 1e-15)
        assertEquals(2.135489, receivedScene.longitude, 1e-15)
        assertEquals("5", receivedScene.experienceId)
    }
}
