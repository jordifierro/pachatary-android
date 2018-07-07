package com.pachatary.data.scene

import android.content.Context
import com.pachatary.data.auth.AuthHttpInterceptor
import com.pachatary.data.common.Result
import com.pachatary.data.experience.ExperienceApiRepositoryTest
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
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

    @Test
    fun test_get_scenes() {
        given {
            a_web_server_that_returns(200, "GET_scenes_?experience")
        } whenn {
            scenes_request_flowable_for(experienceId = "8")
        } then {
            should_request_get_scene_for(experienceId = "8")
            should_response_inprogress_and_parsed_scenes_list()
        }
    }

    @Test
    fun test_create_scene() {
        given {
            an_scene()
            a_web_server_that_returns(201, "POST_scenes")
        } whenn {
            create_scene_with_that_scene()
        } then {
            should_call_with_that_scene_params(path = "/scenes/", method = "POST")
            should_return_parsed_scene_response()
        }
    }

    @Test
    fun test_edit_scene() {
        given {
            a_web_server_that_returns(201, "PATCH_scene_id")
            an_scene(id = "7")
        } whenn {
            edit_scene_with_that_scene()
        } then {
            should_call_with_that_scene_params(path = "/scenes/7", method = "PATCH")
            should_return_parsed_scene_response()
        }
    }

    @Test
    fun test_upload_picture_scene_parser() {
        given {
            a_json_object_from_POST_scenes_response()
        } whenn {
            parse_that_scene_json()
        } then {
            should_response_parsed_scene()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
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
        val testSceneListSubscriber = TestSubscriber<Result<List<Scene>>>()
        val testSceneSubscriber = TestSubscriber<Result<Scene>>()
        lateinit var scene: Scene
        lateinit var jsonObject: JsonObject
        lateinit var parsedResult: Scene

        fun buildScenario(): ScenarioMaker {
            return this
        }

        fun an_scene(id: String = "1") {
            scene = Scene(id = id, title = "T", description = "desc",
                          latitude = 1.0, longitude = -2.3, experienceId = "3", picture = null)
        }

        fun a_web_server_that_returns(statusCode: Int, jsonResponseFilename: String) {
            mockWebServer.enqueue(MockResponse()
                    .setResponseCode(statusCode)
                    .setBody(ExperienceApiRepositoryTest::class.java
                            .getResource("/api/" + jsonResponseFilename + ".json").readText()))
        }

        fun a_json_object_from_POST_scenes_response() {
            jsonObject = JsonParser().parse(ExperienceApiRepositoryTest::class.java
                    .getResource("/api/POST_scenes.json").readText()).asJsonObject
        }


        fun scenes_request_flowable_for(experienceId: String) {
            repository.scenesRequestFlowable(experienceId).subscribe(testSceneListSubscriber)
        }

        fun create_scene_with_that_scene() {
            repository.createScene(scene).subscribe(testSceneSubscriber)
        }

        fun edit_scene_with_that_scene() {
            repository.editScene(scene).subscribe(testSceneSubscriber)
        }

        fun parse_that_scene_json() {
            parsedResult = repository.parseSceneJson(jsonObject)
        }

        fun should_request_get_scene_for(experienceId: String) {
            val request = mockWebServer.takeRequest()
            assertEquals("/scenes/?experience=" + experienceId, request.getPath())
            assertEquals("GET", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun should_call_with_that_scene_params(path: String, method: String) {
            val request = mockWebServer.takeRequest()
            assertEquals(path, request.getPath())
            assertEquals(method, request.getMethod())
            assertEquals("title=" + scene.title +
                         "&description=" + scene.description +
                         "&latitude=" + scene.latitude +
                         "&longitude=" + scene.longitude +
                         "&experience_id=" + scene.experienceId,
                    request.getBody().readUtf8())
        }

        fun should_response_inprogress_and_parsed_scenes_list() {
            testSceneListSubscriber.awaitCount(1)

            assertEquals(0, testSceneListSubscriber.events.get(1).size)
            assertEquals(2, testSceneListSubscriber.events.get(0).size)

            val firstResult = testSceneListSubscriber.events.get(0).get(0) as Result<*>
            assertEquals(Result(listOf<Scene>(), inProgress = true), firstResult)

            val secondResult = testSceneListSubscriber.events.get(0).get(1) as Result<*>
            val receivedScenes = secondResult.data as List<*>

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

        fun should_return_parsed_scene_response() {
            testSceneSubscriber.awaitCount(1)

            assertEquals(0, testSceneSubscriber.events.get(1).size)
            assertEquals(1, testSceneSubscriber.events.get(0).size)

            val receivedResult = testSceneSubscriber.events.get(0).get(0) as Result<*>
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

        fun should_response_parsed_scene() {
            assertEquals("4", parsedResult.id)
            assertEquals("Plaça", parsedResult.title)
            assertEquals("", parsedResult.description)
            assertEquals("https://scenes/00df.small.jpeg", parsedResult.picture!!.smallUrl)
            assertEquals("https://scenes/00df.medium.jpeg", parsedResult.picture!!.mediumUrl)
            assertEquals("https://scenes/00df.large.jpeg", parsedResult.picture!!.largeUrl)
            assertEquals(41.364679, parsedResult.latitude, 1e-15)
            assertEquals(2.135489, parsedResult.longitude, 1e-15)
            assertEquals("5", parsedResult.experienceId)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
