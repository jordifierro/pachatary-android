package com.pachatary.data.scene

import com.pachatary.data.common.*
import io.reactivex.Flowable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class SceneRepositoryTest {

    @Test
    fun test_scenes_flowable_return_cache_flowable_connected_with_api_request() {
        given {
            an_experience_id()
            an_scenes_cache_factory_that_returns_cache()
            an_api_repo_that_returns_scenes_flowable_with_an_scene()
        } whenn {
            scenes_flowable_is_called_with_experience_id()
        } then {
            should_return_flowable_created_by_factory()
            should_connect_api_scenes_flowable_on_next_to_replace_all_scenes_observer()
        }
    }

    @Test
    fun test_scenes_flowable_return_cache_flowable_connected_with_api_request_inprogress_case() {
        given {
            an_experience_id()
            an_scenes_cache_factory_that_returns_cache()
            an_api_repo_that_returns(Status.IN_PROGRESS)
        } whenn {
            scenes_flowable_is_called_with_experience_id()
        } then {
            should_return_flowable_created_by_factory()
            should_connect_api_scenes_flowable_and_emit_through_replace(Status.IN_PROGRESS)
        }
    }

    @Test
    fun test_scenes_flowable_return_cache_flowable_connected_with_api_request_error_case() {
        given {
            an_experience_id()
            an_scenes_cache_factory_that_returns_cache()
            an_api_repo_that_returns(Status.ERROR)
        } whenn {
            scenes_flowable_is_called_with_experience_id()
        } then {
            should_return_flowable_created_by_factory()
            should_connect_api_scenes_flowable_and_emit_through_replace(Status.ERROR)
        }
    }

    @Test
    fun test_same_experience_scenes_flowable_call_returns_same_flowable() {
        given {
            an_experience_id()
            a_second_experience_id()
            an_scenes_cache_factory_that_returns_cache()
            an_scenes_cache_factory_that_returns_another_cache_when_called_again()
            an_api_repo_that_returns_scenes_flowable_with_an_scene()
            an_api_repo_that_returns_scenes_flowable_with_an_scene_for_second_experience()
        } whenn {
            scenes_flowable_is_called_with_experience_id()
            scenes_flowable_is_called_with_second_experience_id()
            scenes_flowable_is_called_with_experience_id_again()
        } then {
            first_result_should_be_first_scenes_flowable()
            second_result_should_be_second_scenes_flowable()
            third_result_should_be_first_scenes_flowable()
        }
    }

    @Test
    fun test_scene_flowable_returns_experience_scenes_flowable_filtering_desired_scene() {
        given {
            an_experience_id()
            an_scene_id()
            an_scenes_cache_factory_that_returns_cache_with_several_scenes()
            an_api_repo_that_returns_scenes_flowable_with_an_scene()
        } whenn {
            scene_flowable_is_called_with_experience_and_scene_id()
        } then {
            only_scene_with_scene_id_should_be_received()
        }
    }

    @Test
    fun test_create_scene_calls_api_repo_and_emits_through_add_observer_the_new_scene() {
        given {
            an_experience_id()
            an_scene()
            an_scenes_cache_factory_that_returns_cache()
            an_api_repo_that_returns_scenes_flowable_with_an_scene()
            an_api_repo_that_returns_created_scene()
        } whenn {
            scenes_flowable_is_called_with_experience_id()
            create_scene_is_called()
        } then {
            should_call_api_created_scene()
            should_emit_created_scene_through_add_or_update_scenes_observer()
        }
    }

    @Test
    fun test_upload_scene_picture_calls_api_repo_with_delegate_to_emit_through_update_observer() {
        given {
            an_experience_id()
            an_scene_id()
            an_cropped_image_uri_string()
            an_scene()
            an_scenes_cache_factory_that_returns_cache()
            an_api_repo_that_returns_scenes_flowable_with_an_scene()
        } whenn {
            scenes_flowable_is_called_with_experience_id()
            upload_scene_picture_is_called()
        } then {
            should_call_api_upload_scene_picture_with_scene_id_and_image_uri_string()
        } whenn {
            delegate_is_called_with_scene()
        } then {
            delegate_param_should_emit_scene_through_add_or_update_observer()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var repository: SceneRepository
        @Mock lateinit var mockApiRepository: SceneApiRepository
        @Mock lateinit var mockScenesCacheFactory: ResultCacheFactory<Scene>
        var experienceId = ""
        var secondExperienceId = ""
        var sceneId = ""
        var croppedImageUriString = ""
        val resultError = ResultError<List<Scene>>(Exception())
        lateinit var scene: Scene
        lateinit var scenesFlowable: Flowable<Result<List<Scene>>>
        lateinit var addOrUpdateObserver: TestObserver<List<Scene>>
        lateinit var updateObserver: TestObserver<List<Scene>>
        lateinit var replaceResultObserver: TestObserver<Result<List<Scene>>>
        lateinit var secondScenesFlowable: Flowable<Result<List<Scene>>>
        lateinit var secondAddOrUpdateObserver: TestObserver<List<Scene>>
        lateinit var secondUpdateObserver: TestObserver<List<Scene>>
        lateinit var secondReplaceResultObserver: TestObserver<Result<List<Scene>>>
        lateinit var apiScenesFlowable: Flowable<Result<List<Scene>>>
        lateinit var scenesFlowableResult: Flowable<Result<List<Scene>>>
        lateinit var secondScenesFlowableResult: Flowable<Result<List<Scene>>>
        lateinit var thirdScenesFlowableResult: Flowable<Result<List<Scene>>>
        lateinit var sceneFlowableResult: Flowable<Result<Scene>>
        lateinit var createdSceneFlowableResult: Flowable<Result<Scene>>

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            repository = SceneRepository(mockApiRepository, mockScenesCacheFactory)

            return this
        }

        fun an_experience_id() {
            experienceId = "1"
        }

        fun a_second_experience_id() {
            secondExperienceId = "2"
        }

        fun an_scene_id() {
            sceneId = "2"
        }

        fun an_scene() {
            scene = Scene(id = "1", title = "T", description = "desc",
                          latitude = 1.0, longitude = -2.3,
                          experienceId = experienceId, picture = null)
        }

        fun an_cropped_image_uri_string() {
            croppedImageUriString = "image_uri"
        }

        fun an_scenes_cache_factory_that_returns_cache() {
            addOrUpdateObserver = TestObserver.create()
            addOrUpdateObserver.onSubscribe(addOrUpdateObserver)
            updateObserver = TestObserver.create()
            replaceResultObserver = TestObserver.create()
            replaceResultObserver.onSubscribe(replaceResultObserver)
            scenesFlowable = Flowable.never()
            BDDMockito.given(mockScenesCacheFactory.create()).willReturn(
                    ResultCacheFactory.ResultCache(replaceResultObserver, addOrUpdateObserver,
                                                        updateObserver, scenesFlowable))
        }

        fun an_scenes_cache_factory_that_returns_another_cache_when_called_again() {
            secondAddOrUpdateObserver = TestObserver.create()
            secondAddOrUpdateObserver.onSubscribe(secondAddOrUpdateObserver)
            secondUpdateObserver = TestObserver.create()
            secondReplaceResultObserver = TestObserver.create()
            secondReplaceResultObserver.onSubscribe(secondReplaceResultObserver)
            secondScenesFlowable = Flowable.never()
            BDDMockito.given(mockScenesCacheFactory.create()).willReturn(
                    ResultCacheFactory.ResultCache(secondReplaceResultObserver,
                            secondAddOrUpdateObserver, secondUpdateObserver, secondScenesFlowable))
        }

        fun an_scenes_cache_factory_that_returns_cache_with_several_scenes() {
            val sceneA = Scene(id = "1", title = "T", description = "desc",
                               latitude = 1.0, longitude = -2.3, experienceId = "3", picture = null)
            val sceneB = Scene(id = "2", title = "T", description = "desc",
                               latitude = 1.0, longitude = -2.3, experienceId = "3", picture = null)
            addOrUpdateObserver = TestObserver.create()
            updateObserver = TestObserver.create()
            replaceResultObserver = TestObserver.create()
            replaceResultObserver.onSubscribe(replaceResultObserver)
            scenesFlowable = Flowable.just(ResultSuccess(listOf(sceneA, sceneB)))
            BDDMockito.given(mockScenesCacheFactory.create()).willReturn(
                    ResultCacheFactory.ResultCache(replaceResultObserver, addOrUpdateObserver,
                            updateObserver, scenesFlowable))
        }

        fun an_api_repo_that_returns_scenes_flowable_with_an_scene() {
            scene = Scene("2", "T", "d", null, 0.0, 0.0, "1")
            apiScenesFlowable = Flowable.just(ResultSuccess(listOf(scene)))

            BDDMockito.given(mockApiRepository.scenesRequestFlowable(experienceId))
                    .willReturn(apiScenesFlowable)
        }

        fun an_api_repo_that_returns(status: Status) {
            if (status == Status.IN_PROGRESS)
                BDDMockito.given(mockApiRepository.scenesRequestFlowable(experienceId))
                        .willReturn(Flowable.just(ResultInProgress()))
            else if (status == Status.ERROR)
                BDDMockito.given(mockApiRepository.scenesRequestFlowable(experienceId))
                        .willReturn(Flowable.just(resultError))
        }

        fun an_api_repo_that_returns_created_scene() {
            val createdSceneFlowable = Flowable.just(ResultSuccess(scene))
            BDDMockito.given(mockApiRepository.createScene(scene)).willReturn(createdSceneFlowable)
        }

        fun an_api_repo_that_returns_scenes_flowable_with_an_scene_for_second_experience() {
            BDDMockito.given(mockApiRepository.scenesRequestFlowable(secondExperienceId))
                    .willReturn(apiScenesFlowable)
        }

        fun scenes_flowable_is_called_with_experience_id() {
            scenesFlowableResult = repository.scenesFlowable(experienceId)
        }

        fun scene_flowable_is_called_with_experience_and_scene_id() {
            sceneFlowableResult = repository.sceneFlowable(experienceId, sceneId)
        }

        fun scenes_flowable_is_called_with_second_experience_id() {
            secondScenesFlowableResult = repository.scenesFlowable(secondExperienceId)
        }

        fun scenes_flowable_is_called_with_experience_id_again() {
            thirdScenesFlowableResult = repository.scenesFlowable(experienceId)
        }

        fun create_scene_is_called() {
            createdSceneFlowableResult = repository.createScene(scene)
        }

        fun upload_scene_picture_is_called() {
            repository.uploadScenePicture(sceneId, croppedImageUriString)
        }

        fun delegate_is_called_with_scene() {
            repository.emitThroughAddOrUpdate.invoke(ResultSuccess(scene))
        }

        fun should_return_flowable_created_by_factory() {
            Assert.assertEquals(scenesFlowable, scenesFlowableResult)
        }

        fun should_connect_api_scenes_flowable_on_next_to_replace_all_scenes_observer() {
            replaceResultObserver.onComplete()
            replaceResultObserver.assertResult(ResultSuccess(listOf(scene)))
        }

        fun first_result_should_be_first_scenes_flowable() {
            Assert.assertEquals(scenesFlowable, scenesFlowableResult)
        }

        fun second_result_should_be_second_scenes_flowable() {
            Assert.assertEquals(secondScenesFlowable, secondScenesFlowableResult)
        }

        fun third_result_should_be_first_scenes_flowable() {
            Assert.assertEquals(scenesFlowable, thirdScenesFlowableResult)
        }

        fun only_scene_with_scene_id_should_be_received() {
            val testSubscriber = TestSubscriber.create<Result<Scene>>()
            sceneFlowableResult.subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
            val result = testSubscriber.events.get(0).get(0) as Result<*>
            val receivedScene = result.data as Scene
            assertEquals(sceneId, receivedScene.id)
        }

        fun should_call_api_created_scene() {
            BDDMockito.then(mockApiRepository).should().createScene(scene)
        }

        fun should_emit_created_scene_through_add_or_update_scenes_observer() {
            createdSceneFlowableResult.subscribeOn(Schedulers.trampoline()).subscribe()
            replaceResultObserver.onComplete()
            replaceResultObserver.assertResult(ResultSuccess(listOf(scene)))
            addOrUpdateObserver.onComplete()
            addOrUpdateObserver.assertResult(listOf(scene))
        }

        fun should_call_api_upload_scene_picture_with_scene_id_and_image_uri_string() {
            BDDMockito.then(mockApiRepository).should()
                    .uploadScenePicture(sceneId, croppedImageUriString,
                                        repository.emitThroughAddOrUpdate)
        }

        fun delegate_param_should_emit_scene_through_add_or_update_observer() {
            replaceResultObserver.onComplete()
            replaceResultObserver.assertResult(ResultSuccess(listOf(scene)))
            addOrUpdateObserver.onComplete()
            addOrUpdateObserver.assertResult(listOf(scene))
        }

        fun should_connect_api_scenes_flowable_and_emit_through_replace(status: Status) {
            replaceResultObserver.onComplete()
            if (status == Status.IN_PROGRESS) replaceResultObserver.assertResult(ResultInProgress())
            else if (status == Status.ERROR) replaceResultObserver.assertResult(resultError)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
