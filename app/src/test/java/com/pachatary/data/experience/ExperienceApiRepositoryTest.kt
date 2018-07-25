package com.pachatary.data.experience

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pachatary.data.DummyResultError
import com.pachatary.data.auth.AuthHttpInterceptor
import com.pachatary.data.common.ImageUploader
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultInProgress
import com.pachatary.data.common.ResultSuccess
import com.pachatary.data.picture.BigPicture
import com.pachatary.data.picture.LittlePicture
import com.pachatary.data.profile.Profile
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import org.mockito.BDDMockito
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
            request_should_get_person_experiences("self")
            response_should_experience_list_and_next_url()
        }
    }

    @Test
    fun test_get_other_person_s_experiences() {
        given {
            a_web_server_that_returns_get_experiences()
        } whenn {
            persons_experiences_are_requested("others.username")
        } then {
            request_should_get_person_experiences("others.username")
            response_should_experience_list_and_next_url()
        }
    }

    @Test
    fun test_get_explore_experiences_with_params() {
        given {
            a_web_server_that_returns_get_experiences()
        } whenn {
            explore_experiences_are_requested("culture", 8.5, -7.4)
        } then {
            request_should_search_experiences("culture", 8.5, -7.4)
            response_should_experience_list_and_next_url()
        }
    }

    @Test
    fun test_get_explore_experiences_without_params() {
        given {
            a_web_server_that_returns_get_experiences()
        } whenn {
            explore_experiences_are_requested()
        } then {
            request_should_search_experiences()
            response_should_experience_list_and_next_url()
        }
    }

    @Test
    fun test_get_saved_experiences() {
        given {
            a_web_server_that_returns_get_experiences()
        } whenn {
            saved_experiences_are_requested()
        } then {
            request_should_get_saved_experiences()
            response_should_experience_list_and_next_url()
        }
    }

    @Test
    fun test_paginate_experiences() {
        given {
            a_url()
            a_web_server_that_returns_get_experiences()
        } whenn {
            paginate_is_called_with_url()
        } then {
            request_should_call_url()
            response_should_experience_list_and_next_url()
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
    fun test_patch_experience() {
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
    fun test_get_experience() {
        given {
            an_experience_id()
            a_web_server_that_returns_get_experience()
        } whenn {
            experience_flowable()
        } then {
            request_should_get_experience()
            response_should_parse_inprogress_result_and_experience()
        }
    }

    @Test
    fun test_translate_experience_share_id() {
        given {
            an_experience_share_id()
            a_web_server_that_returns_get_experience_share_id_id()
        } whenn {
            translate_experience_share_id()
        } then {
            request_should_get_experience_share_id_id()
            response_should_parse_inprogress_result_and_experience_id()
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

    @Test
    fun test_upload_experience_picture_inprogress() {
        given {
            an_image_uploader_that_returns("image_path", "/experiences/4/picture/",
                                           ResultInProgress())
        } whenn {
            upload_image("4", "image_path")
        } then {
            should_call_upload("image_path", "/experiences/4/picture/")
            should_receive(ResultInProgress())
        }
    }

    @Test
    fun test_upload_experience_picture_error() {
        given {
            an_image_uploader_that_returns("image_path", "/experiences/4/picture/",
                    DummyResultError())
        } whenn {
            upload_image("4", "image_path")
        } then {
            should_call_upload("image_path", "/experiences/4/picture/")
            should_receive(DummyResultError())
        }
    }

    @Test
    fun test_upload_experience_picture_with_profile_picture() {
        given {
            an_image_uploader_that_returns("image_path", "/experiences/4/picture/",
                    ResultSuccess(JsonParser().parse(ExperienceApiRepositoryTest::class.java
                    .getResource("/api/POST_experiences.json").readText()).asJsonObject))
        } whenn {
            upload_image("4", "image_path")
        } then {
            should_call_upload("image_path", "/experiences/4/picture/")
            should_receive(ResultSuccess(Experience(id = "4", title = "Plaça", description = "",
                    picture = BigPicture("https://experiences/00df.small.jpeg",
                                         "https://experiences/00df.medium.jpeg",
                                         "https://experiences/00df.large.jpeg"),
                    isMine = true, isSaved = false, savesCount = 5,
                    authorProfile = Profile(username = "usr.nam", bio = "user info",
                            picture = LittlePicture("https://experiences/029d.tiny.jpg",
                                    "https://experiences/029d.small.jpg",
                                    "https://experiences/029d.medium.jpg"),
                            isMe = true))))
        }
    }

    @Test
    fun test_upload_experience_picture_without_profile_picture() {
        given {
            an_image_uploader_that_returns("image_path", "/experiences/4/picture/",
                    ResultSuccess(JsonParser().parse(ExperienceApiRepositoryTest::class.java
                            .getResource("/api/POST_experiences_without_profile_picture.json")
                            .readText()).asJsonObject))
        } whenn {
            upload_image("4", "image_path")
        } then {
            should_call_upload("image_path", "/experiences/4/picture/")
            should_receive(ResultSuccess(Experience(id = "4", title = "Plaça", description = "",
                    picture = BigPicture("https://experiences/00df.small.jpeg",
                            "https://experiences/00df.medium.jpeg",
                            "https://experiences/00df.large.jpeg"),
                    isMine = true, isSaved = false, savesCount = 5,
                    authorProfile = Profile(username = "usr.nam", bio = "user info",
                                            picture = null, isMe = true))))
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        val testListSubscriber = TestSubscriber<Result<List<Experience>>>()
        val testSubscriber = TestSubscriber<Result<Experience>>()
        val testStringSubscriber = TestSubscriber<Result<String>>()
        val testEmptySubscriber = TestSubscriber<Result<Void>>()
        val mockImageUploader = mock(ImageUploader::class.java)
        val mockWebServer = MockWebServer()
        val repository = ExperienceApiRepo(Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create(
                        GsonBuilder().setFieldNamingPolicy(
                                FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                .create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build(),
                Schedulers.trampoline(), mockImageUploader)
        lateinit var experience: Experience
        var experienceId = ""
        var experienceShareId = ""
        var url = ""

        fun an_experience_id() {
            experienceId = "8"
        }

        fun an_experience_share_id() {
            experienceShareId = "aD43ReE9"
        }

        fun a_url() {
            url = "/some-url"
        }

        fun an_image_uploader_that_returns(image: String, path: String,
                                           result: Result<JsonObject>) {
            BDDMockito.given(mockImageUploader.upload(image, path))
                    .willReturn(Flowable.just(result))
        }

        fun a_web_server_that_returns_get_experiences() {
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(
                    ExperienceApiRepositoryTest::class.java.getResource(
                            "/api/GET_experiences.json").readText()))
        }

        fun an_experience() {
            experience = Experience(id = "1", title = "T", description = "desc")
        }

        fun a_web_server_that_returns_post_experiences() {
            mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                ExperienceApiRepositoryTest::class.java.getResource(
                        "/api/POST_experiences.json").readText()))
        }

        fun a_web_server_that_returns_patch_experiences() {
            mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                ExperienceApiRepositoryTest::class.java.getResource(
                        "/api/PATCH_experience_id.json").readText()))
        }

        fun a_web_server_that_returns_get_experience() {
            mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                    ExperienceApiRepositoryTest::class.java.getResource(
                            "/api/GET_experience_id.json").readText()))
        }

        fun a_web_server_that_returns_get_experience_share_id_id() {
            mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(
                    ExperienceApiRepositoryTest::class.java.getResource(
                            "/api/GET_experiences_experience_share_id_id.json").readText()))
        }

        fun a_web_server_that_returns_201() {
            mockWebServer.enqueue(MockResponse().setResponseCode(201).setBody(""))
        }

        fun a_web_server_that_returns_204() {
            mockWebServer.enqueue(MockResponse().setResponseCode(204).setBody(""))
        }

        fun upload_image(experienceId: String, image: String) {
            repository.uploadExperiencePicture(experienceId, image)
                    .subscribe(testSubscriber)
        }

        fun my_experiences_are_requested() {
            repository.myExperiencesFlowable()
                    .subscribeOn(Schedulers.trampoline())
                    .subscribe(testListSubscriber)
            testListSubscriber.awaitCount(1)
        }

        fun persons_experiences_are_requested(username: String) {
            repository.personsExperienceFlowable(username)
                    .subscribeOn(Schedulers.trampoline())
                    .subscribe(testListSubscriber)
            testListSubscriber.awaitCount(1)
        }

        fun explore_experiences_are_requested(word: String? = null, latitude: Double? = null,
                                              longitude: Double? = null) {
            repository.exploreExperiencesFlowable(word, latitude, longitude)
                    .subscribeOn(Schedulers.trampoline()).subscribe(testListSubscriber)
            testListSubscriber.awaitCount(1)
        }

        fun saved_experiences_are_requested() {
            repository.savedExperiencesFlowable()
                    .subscribeOn(Schedulers.trampoline())
                    .subscribe(testListSubscriber)
            testListSubscriber.awaitCount(1)
        }

        fun experience_is_created() {
            repository.createExperience(experience).subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
        }

        fun paginate_is_called_with_url() {
            repository.paginateExperiences(mockWebServer.url(url).toString())
                    .subscribeOn(Schedulers.trampoline()).subscribe(testListSubscriber)
            testSubscriber.awaitCount(1)
        }

        fun experience_is_edited() {
            repository.editExperience(experience).subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
        }

        fun experience_flowable() {
            repository.experienceFlowable(experienceId).subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
        }

        fun translate_experience_share_id() {
            repository.translateShareId(experienceShareId).subscribe(testStringSubscriber)
            testStringSubscriber.awaitCount(1)
        }

        fun experience_is_saved() {
            repository.saveExperience(save = true, experienceId = experience.id)
                    .subscribe(testEmptySubscriber)
            testEmptySubscriber.awaitCount(1)
        }

        fun experience_is_unsaved() {
            repository.saveExperience(save = false, experienceId = experience.id)
                    .subscribe(testEmptySubscriber)
            testEmptySubscriber.awaitCount(1)
        }

        fun request_should_call_url() {
            val request = mockWebServer.takeRequest()
            assertEquals(url, request.path)
            assertEquals("GET", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun request_should_get_person_experiences(username: String) {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/?username=" + username, request.path)
            assertEquals("GET", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun request_should_get_saved_experiences() {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/?saved=true", request.path)
            assertEquals("GET", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }
        fun request_should_search_experiences(word: String? = null, latitude: Double? = null,
                                              longitude: Double? = null) {
            val request = mockWebServer.takeRequest()
            if (word == null && latitude == null && longitude == null)
                assertEquals("/experiences/search", request.path)
            else assertEquals("/experiences/search?word=" + word + "&latitude=" +
                    latitude.toString() + "&longitude=" + longitude.toString(), request.path)
            assertEquals("GET", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun request_should_post_experience_attrs() {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/", request.path)
            assertEquals("POST", request.getMethod())
            assertEquals("title=T&description=desc",
                    request.getBody().readUtf8())
        }

        fun request_should_patch_experience_attrs() {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/1", request.path)
            assertEquals("PATCH", request.getMethod())
            assertEquals("title=T&description=desc", request.getBody().readUtf8())
        }

        fun request_should_get_experience() {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/" + experienceId, request.path)
            assertEquals("GET", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun request_should_post_experience_id_save() {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/" + experience.id + "/save/", request.path)
            assertEquals("POST", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun request_should_delete_experience_id_save() {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/" + experience.id + "/save/", request.path)
            assertEquals("DELETE", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun request_should_get_experience_share_id_id() {
            val request = mockWebServer.takeRequest()
            assertEquals("/experiences/" + experienceShareId + "/id", request.path)
            assertEquals("GET", request.getMethod())
            assertEquals("", request.getBody().readUtf8())
        }

        fun response_should_experience_list_and_next_url() {
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

        fun response_should_parse_experience() {
            val receivedResult = testSubscriber.events.get(0).get(0) as Result<*>
            val receivedExperience = receivedResult.data as Experience

            assertEquals("4", receivedExperience.id)
            assertEquals("Plaça", receivedExperience.title)
            assertEquals("", receivedExperience.description)
            assertEquals("https://experiences/00df.small.jpeg",
                         receivedExperience.picture!!.smallUrl)
            assertEquals("https://experiences/00df.medium.jpeg",
                         receivedExperience.picture!!.mediumUrl)
            assertEquals("https://experiences/00df.large.jpeg",
                         receivedExperience.picture!!.largeUrl)
            assertEquals(true, receivedExperience.isMine)
            assertEquals(false, receivedExperience.isSaved)
            assertEquals("usr.nam", receivedExperience.authorProfile.username)
            assertEquals("user info", receivedExperience.authorProfile.bio)
            assertEquals("https://experiences/029d.tiny.jpg",
                    receivedExperience.authorProfile.picture!!.tinyUrl)
            assertEquals("https://experiences/029d.small.jpg",
                    receivedExperience.authorProfile.picture!!.smallUrl)
            assertEquals("https://experiences/029d.medium.jpg",
                    receivedExperience.authorProfile.picture!!.mediumUrl)
            assertTrue(receivedExperience.authorProfile.isMe)
            assertEquals(5, receivedExperience.savesCount)
        }

        fun response_should_parse_inprogress_result_and_experience() {
            val firstResult = testSubscriber.events.get(0).get(0) as Result<*>
            assertEquals(firstResult, ResultInProgress<Experience>())

            val secondResult = testSubscriber.events.get(0).get(1) as Result<*>
            val receivedExperience = secondResult.data as Experience

            assertEquals("4", receivedExperience.id)
            assertEquals("Plaça", receivedExperience.title)
            assertEquals("", receivedExperience.description)
            assertEquals("https://experiences/00df.small.jpeg",
                         receivedExperience.picture!!.smallUrl)
            assertEquals("https://experiences/00df.medium.jpeg",
                         receivedExperience.picture!!.mediumUrl)
            assertEquals("https://experiences/00df.large.jpeg",
                         receivedExperience.picture!!.largeUrl)
            assertEquals(true, receivedExperience.isMine)
            assertEquals(false, receivedExperience.isSaved)
            assertEquals("usr.nam", receivedExperience.authorProfile.username)
            assertEquals("user info", receivedExperience.authorProfile.bio)
            assertEquals("https://experiences/029d.tiny.jpg",
                    receivedExperience.authorProfile.picture!!.tinyUrl)
            assertEquals("https://experiences/029d.small.jpg",
                    receivedExperience.authorProfile.picture!!.smallUrl)
            assertEquals("https://experiences/029d.medium.jpg",
                    receivedExperience.authorProfile.picture!!.mediumUrl)
            assertTrue(receivedExperience.authorProfile.isMe)
            assertEquals(5, receivedExperience.savesCount)
        }

        fun response_should_parse_empty_body() {
            val receivedResult = testEmptySubscriber.events.get(0).get(0) as Result<*>
            assertEquals(ResultSuccess<Void>(), receivedResult)
        }

        fun response_should_parse_inprogress_result_and_experience_id() {
            testStringSubscriber.assertValues(ResultInProgress(), ResultSuccess("43"))
        }

        fun should_call_upload(image: String, path: String) {
            BDDMockito.then(mockImageUploader).should().upload(image, path)
        }

        fun should_receive(result: Result<Experience>) {
            testSubscriber.awaitCount(1)
            testSubscriber.assertResult(result)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
