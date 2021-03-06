package com.pachatary.data.scene

import com.pachatary.data.DummyResultError
import com.pachatary.data.DummyScene
import com.pachatary.data.DummySceneResultSuccess
import com.pachatary.data.DummyScenesResultSuccess
import com.pachatary.data.common.*
import io.reactivex.Flowable
import io.reactivex.observers.TestObserver
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class SceneRepositoryTest {

    @Test
    fun test_scenes_flowable_return_cache_flowable_connected_with_api_request() {
        given {
            an_scenes_cache_factory_that_returns_cache_with(DummyScenesResultSuccess("8"))
            an_api_repo_that_returns_scenes_flowable_for("1",
                    ResultInProgress(), DummyScenesResultSuccess("4"))
        } whenn {
            scenes_flowable(experienceId = "1")
        } then {
            should_return_list_flowable_with(DummyScenesResultSuccess("8"))
            should_emit_through_cache_replace(ResultInProgress(), DummyScenesResultSuccess("4"))
        }
    }

    @Test
    fun test_scenes_flowable_return_cache_flowable_connected_with_api_request_inprogress_case() {
        given {
            an_scenes_cache_factory_that_returns_cache_with(ResultInProgress())
            an_api_repo_that_returns_scenes_flowable_for("1", ResultInProgress())
        } whenn {
            scenes_flowable(experienceId = "1")
        } then {
            should_return_list_flowable_with(ResultInProgress())
            should_emit_through_cache_replace(ResultInProgress())
        }
    }

    @Test
    fun test_scenes_flowable_return_cache_flowable_connected_with_api_request_error_case() {
        given {
            an_scenes_cache_factory_that_returns_cache_with(DummyResultError())
            an_api_repo_that_returns_scenes_flowable_for("1", DummyResultError())
        } whenn {
            scenes_flowable(experienceId = "1")
        } then {
            should_return_list_flowable_with(DummyResultError())
            should_emit_through_cache_replace(DummyResultError())
        }
    }

    @Test
    fun test_scenes_flowable_calls_api_when_cached_result_is_error() {
        given {
            an_scenes_cache_factory_that_returns_cache_with(DummyResultError())
            an_api_repo_that_returns_scenes_flowable_for("1", DummyScenesResultSuccess("4"))
        } whenn {
            scenes_flowable(experienceId = "1")
            scenes_flowable(experienceId = "1")
        } then {
            should_return_list_flowable_with(DummyResultError(), forResult = 1)
            should_return_list_flowable_with(forResult = 2)
            should_emit_through_cache_replace(DummyScenesResultSuccess("4"),
                                              DummyScenesResultSuccess("4"))
        }
    }

    @Test
    fun test_repo_caches() {
        given {
            an_scenes_cache_factory_that_returns_cache_with(DummyScenesResultSuccess("8"))
            an_api_repo_that_returns_scenes_flowable_for("1", DummyScenesResultSuccess("4"))
        } whenn {
            scenes_flowable(experienceId = "1")
            scenes_flowable(experienceId = "1")
            scenes_flowable(experienceId = "1")
        } then {
            should_emit_through_cache_replace(DummyScenesResultSuccess("4"))
            should_return_list_flowable_with(DummyScenesResultSuccess("8"), forResult = 1)
            should_return_list_flowable_with(DummyScenesResultSuccess("8"), forResult = 2)
            should_return_list_flowable_with(DummyScenesResultSuccess("8"), forResult = 3)
        }
    }

    @Test
    fun test_scene_flowable_returns_experience_scenes_flowable_filtering_desired_scene() {
        given {
            an_scenes_cache_factory_that_returns_cache_with(DummyScenesResultSuccess("8", "9", "10"))
            an_api_repo_that_returns_scenes_flowable_for("2", DummyScenesResultSuccess("4"))
        } whenn {
            scene_flowable(sceneId = "9", experienceId = "2")
        } then {
            should_return_flowable_with(DummySceneResultSuccess("9"), forResult = 1)
            should_emit_through_cache_replace(DummyScenesResultSuccess("4"))
        }
    }

    @Test
    fun test_create_scene_calls_api_repo_and_emits_through_add_observer_the_new_scene() {
        given {
            an_scenes_cache_factory_that_returns_cache_with(DummyScenesResultSuccess("8"))
            an_api_repo_that_returns_scenes_flowable_for("2", DummyScenesResultSuccess("4"))
            an_api_repo_that_returns_created_flowable_for(DummyScene("6", experienceId = "2"),
                ResultInProgress(), DummySceneResultSuccess("6", experienceId = "2"))
        } whenn {
            scenes_flowable("2")
            create_scene(DummyScene("6", experienceId = "2"))
        } then {
            should_return_flowable_with(ResultInProgress(),
                                        DummySceneResultSuccess("6", experienceId = "2"))
            should_emit_through_cache_add_or_update(listOf(DummyScene("6", experienceId = "2")))
        }
    }

    @Test
    fun test_edit_scene_calls_api_repo_and_emits_through_add_observer_the_updated_scene() {
        given {
            an_scenes_cache_factory_that_returns_cache_with(DummyScenesResultSuccess("8"))
            an_api_repo_that_returns_scenes_flowable_for("2", DummyScenesResultSuccess("4"))
            an_api_repo_that_returns_edited_flowable_for(DummyScene("6", experienceId = "2"),
                    ResultInProgress(), DummySceneResultSuccess("6", experienceId = "2"))
        } whenn {
            scenes_flowable("2")
            edit_scene(DummyScene("6", experienceId = "2"))
        } then {
            should_return_flowable_with(ResultInProgress(),
                                        DummySceneResultSuccess("6", experienceId = "2"))
            should_emit_through_cache_update(listOf(DummyScene("6", experienceId = "2")))
        }
    }

    @Test
    fun test_upload_scene_picture_calls_api_repo_with_delegate_to_emit_through_update_observer() {
        given {
            an_scenes_cache_factory_that_returns_cache_with(DummyScenesResultSuccess("8"))
            an_api_repo_that_returns_scenes_flowable_for("2", DummyScenesResultSuccess("4"))
            an_api_repo_that_returns_upload_flowable_for("6", "picture_path",
                    ResultInProgress(), DummySceneResultSuccess("6", experienceId = "2"))
        } whenn {
            scenes_flowable("2")
            upload_scene_picture("6", "picture_path")
        } then {
            should_emit_through_cache_update(listOf(DummyScene("6", experienceId = "2")))
        }
    }

    @Test
    fun test_refresh() {
        given {
            an_scenes_cache_factory_that_returns_cache_with(DummyScenesResultSuccess("8"))
            an_api_repo_that_returns_scenes_flowable_for("1", DummyScenesResultSuccess("4"))
        } whenn {
            scenes_flowable("1")
        } given {
            an_api_repo_that_returns_scenes_flowable_for("1", DummyScenesResultSuccess("5"))
        } whenn {
            refresh_scenes("1")
        } then {
            should_emit_through_cache_replace(DummyScenesResultSuccess("4"), ResultInProgress(),
                                              DummyScenesResultSuccess("5"))
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().buildScenario().given(func)

    class ScenarioMaker {
        lateinit var repository: SceneRepository
        @Mock lateinit var mockApiRepository: SceneApiRepository
        @Mock lateinit var mockScenesCacheFactory: ResultCacheFactory<Scene>
        private var scenesResultFlowables = mutableListOf<Flowable<Result<List<Scene>>>>()
        private var sceneResultFlowables = mutableListOf<Flowable<Result<Scene>>>()
        private var caches = mutableListOf<ResultCacheFactory.ResultCache<Scene>>()

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            repository = SceneRepository(mockApiRepository, mockScenesCacheFactory)

            return this
        }

        fun an_scenes_cache_factory_that_returns_cache_with(result: Result<List<Scene>>) {
            val addOrUpdateObserver = TestObserver.create<Pair<List<Scene>,
                                                               ResultCacheFactory.AddPosition>>()
            addOrUpdateObserver.onSubscribe(addOrUpdateObserver)
            val updateObserver = TestObserver.create<List<Scene>>()
            updateObserver.onSubscribe(updateObserver)
            val replaceResultObserver = TestObserver.create<Result<List<Scene>>>()
            replaceResultObserver.onSubscribe(replaceResultObserver)
            val scenesFlowable = Flowable.just(result).replay(1).autoConnect()
            caches.add(ResultCacheFactory.ResultCache(replaceResultObserver, addOrUpdateObserver,
                                                      updateObserver, scenesFlowable))
            BDDMockito.given(mockScenesCacheFactory.create()).willReturn(caches.last())
        }

        fun an_api_repo_that_returns_scenes_flowable_for(experienceId: String,
                                                         vararg results: Result<List<Scene>>) {
            BDDMockito.given(mockApiRepository.scenesRequestFlowable(experienceId))
                    .willReturn(Flowable.fromArray(*results))
        }

        fun an_api_repo_that_returns_created_flowable_for(scene: Scene, vararg results: Result<Scene>) {
            BDDMockito.given(mockApiRepository.createScene(scene))
                    .willReturn(Flowable.fromArray(*results))
        }

        fun an_api_repo_that_returns_edited_flowable_for(scene: Scene, vararg results: Result<Scene>) {
            BDDMockito.given(mockApiRepository.editScene(scene))
                    .willReturn(Flowable.fromArray(*results))
        }

        fun an_api_repo_that_returns_upload_flowable_for(sceneId: String,
                                                         croppedImageUriString: String,
                                                         vararg results: Result<Scene>) {
            BDDMockito.given(mockApiRepository.uploadScenePicture(sceneId, croppedImageUriString))
                    .willReturn(Flowable.fromArray(*results))
        }

        fun scenes_flowable(experienceId: String) {
            scenesResultFlowables.add(repository.scenesFlowable(experienceId))
        }

        fun scene_flowable(sceneId: String, experienceId: String) {
            sceneResultFlowables.add(repository.sceneFlowable(experienceId, sceneId))
        }

        fun refresh_scenes(experienceId: String) {
            repository.refreshScenes(experienceId)
        }

        fun create_scene(scene: Scene) {
            sceneResultFlowables.add(repository.createScene(scene))
        }

        fun edit_scene(scene: Scene) {
            sceneResultFlowables.add(repository.editScene(scene))
        }

        fun upload_scene_picture(sceneId: String, imageUriString: String) {
            repository.uploadScenePicture(sceneId, imageUriString)
        }

        fun should_emit_through_cache_replace(vararg results: Result<List<Scene>>, throughCache: Int = 1) {
            val replaceCacheObserver =
                    caches[throughCache - 1].replaceResultObserver as TestObserver
            replaceCacheObserver.onComplete()
            replaceCacheObserver.assertResult(*results)
        }

        fun should_emit_through_cache_add_or_update(vararg scenes: List<Scene>, throughCache: Int = 1) {
            val addOrUpdateObserver = caches[throughCache - 1].addOrUpdateObserver as TestObserver
            addOrUpdateObserver.onComplete()

            val calls = mutableListOf<Pair<List<Scene>, ResultCacheFactory.AddPosition>>()
            for (scene in scenes) { calls.add(Pair(scene, ResultCacheFactory.AddPosition.END)) }
            addOrUpdateObserver.assertResult(*calls.toTypedArray())
        }

        fun should_emit_through_cache_update(vararg scenes: List<Scene>, throughCache: Int = 1) {
            val updateObserver = caches[throughCache - 1].updateObserver as TestObserver
            updateObserver.onComplete()
            updateObserver.assertResult(*scenes)
        }

        fun should_return_list_flowable_with(vararg results: Result<List<Scene>>, forResult: Int = 1) {
            val testSceneListSubscriber = TestSubscriber<Result<List<Scene>>>()
            scenesResultFlowables[forResult - 1].subscribe(testSceneListSubscriber)
            if (!results.isEmpty()) {
                testSceneListSubscriber.awaitCount(1)
                testSceneListSubscriber.assertResult(*results)
            }
            else {
                testSceneListSubscriber.await()
                testSceneListSubscriber.assertNoValues()
            }
        }

        fun should_return_flowable_with(vararg results: Result<Scene>, forResult: Int = 1) {
            val testSceneSubscriber = TestSubscriber<Result<Scene>>()
            sceneResultFlowables[forResult - 1].subscribe(testSceneSubscriber)
            if (!results.isEmpty()) {
                testSceneSubscriber.awaitCount(1)
                testSceneSubscriber.assertResult(*results)
            }
            else {
                testSceneSubscriber.await()
                testSceneSubscriber.assertNoValues()
            }
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
