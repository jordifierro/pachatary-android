package com.abidria.data.scene

import com.abidria.data.common.Result
import io.reactivex.Flowable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class SceneRepositoryTest {

    @Test
    fun test_get_scenes_flowable_returns_apis_one() {
        given {
            an_experience_id()
            an_scene_for_that_experience()
            an_scenes_flowable_for_that_experience()
            an_api_repository_that_returns_that_flowable()
            a_repository()
        } whenn {
            subscription_is_made_to_experience_id_scenes_flowable()
        } then {
            scene_should_be_received()
        }
    }

    @Test
    fun test_get_scenes_flowable_returns_last_cached() {
        given {
            an_experience_id()
            an_scene_for_that_experience()
            another_scene_for_that_experience()
            an_scenes_flowable_that_returns_one_scene_each_time()
            an_api_repository_that_returns_that_flowable()
            a_repository()
        } whenn {
            subscription_is_made_to_experience_id_scenes_flowable()
        } then {
            scene_should_be_received()
            another_scene_should_be_received_second()
        } whenn {
            another_subscription_is_made_to_experience_id_scenes_flowable()
        } then {
            another_scene_should_be_received_first_on_second_subscriber()
        }
    }

    @Test
    fun test_refresh_emits_on_cached_refresher_observer() {
        given {
            an_experience_id()
            an_api_repository_that_returns_an_observer()
        } whenn {
            subscription_is_made_to_experience_id_scenes_flowable()
            refresh_scenes_is_called_for_that_experience_id()
        } then {
            one_emition_should_be_made_through_that_observer()
        }
    }

    @Test
    fun test_get_scene_flowable_returns_only_scene_filtered() {
        given {
            an_experience_id()
            an_scene_for_that_experience()
            another_scene_for_that_experience()
            an_scenes_flowable_that_returns_both_scenes_together()
            an_api_repository_that_returns_that_flowable()
        } whenn {
            subscription_is_made_to_experience_id_and_scene_id_of_another_scene()
        } then {
            only_another_scene_should_be_received()
        }
    }

    @Test
    fun test_create_scene_returns_api_created_scene() {
        given {
            an_experience_id()
            an_scene_for_that_experience()
            a_flowable_that_returns_that_scene()
            an_api_repository_that_returns_that_flowable_on_create()
        } whenn {
            create_new_scene_is_called()
        } then {
            api_repository_create_scene_method_should_be_called()
            an_scene_is_received_by_result_flowable()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var repository: SceneRepository
        @Mock lateinit var mockApiRepository: SceneApiRepository
        var experience_id = ""
        lateinit var scene: Scene
        lateinit var anotherScene: Scene
        lateinit var scenesFlowable: Flowable<Result<List<Scene>>>
        lateinit var sceneFlowable: Flowable<Result<Scene>>
        lateinit var resultFlowable: Flowable<Result<Scene>>
        val testSubscriber = TestSubscriber<Result<List<Scene>>>()
        val secondTestSubscriber = TestSubscriber<Result<List<Scene>>>()
        val testSceneSubscriber = TestSubscriber<Result<Scene>>()
        val testObserver = TestObserver<Any>()

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            repository = SceneRepository(mockApiRepository)

            return this
        }

        fun an_experience_id() {
            experience_id = "3"
        }

        fun an_scene_for_that_experience() {
            scene = Scene(id = "9", title = "T", description = "", picture = null,
                          latitude = 1.0, longitude = 0.0, experienceId = experience_id)
        }

        fun another_scene_for_that_experience() {
            anotherScene = Scene(id = "5", title = "B", description = "", picture = null,
                                 latitude = 1.0, longitude = 0.0, experienceId = experience_id)
        }

        fun an_scenes_flowable_for_that_experience() {
            scenesFlowable = Flowable.just(Result(Arrays.asList(scene), null))
        }

        fun an_scenes_flowable_that_returns_one_scene_each_time() {
            scenesFlowable = Flowable.just(Result(Arrays.asList(scene), null),
                                           Result(Arrays.asList(anotherScene), null))
        }

        fun an_scenes_flowable_that_returns_both_scenes_together() {
            scenesFlowable = Flowable.just(Result(Arrays.asList(scene, anotherScene), null))
        }

        fun a_flowable_that_returns_that_scene() {
            sceneFlowable = Flowable.just(Result(scene, null))
        }

        fun an_api_repository_that_returns_that_flowable() {
            BDDMockito.given(mockApiRepository.scenesFlowableAndRefreshObserver(experience_id))
                    .willReturn(Pair(first = scenesFlowable, second = PublishSubject.create()))
        }

        fun an_api_repository_that_returns_an_observer() {
            BDDMockito.given(mockApiRepository.scenesFlowableAndRefreshObserver(experience_id))
                        .willReturn(Pair(first = Flowable.never(), second = testObserver))
        }

        fun an_api_repository_that_returns_that_flowable_on_create() {
            BDDMockito.given(mockApiRepository.createScene(scene)).willReturn(sceneFlowable)
        }

        fun a_repository() {
            repository = SceneRepository(mockApiRepository)
        }

        fun subscription_is_made_to_experience_id_scenes_flowable() {
            repository.scenesFlowable(experience_id).subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
        }

        fun refresh_scenes_is_called_for_that_experience_id() {
            repository.refreshScenes(experience_id)
        }

        fun another_subscription_is_made_to_experience_id_scenes_flowable() {
            repository.scenesFlowable(experience_id)
                    .subscribeOn(Schedulers.trampoline()).subscribe(secondTestSubscriber)
            secondTestSubscriber.awaitCount(1)
        }

        fun subscription_is_made_to_experience_id_and_scene_id_of_another_scene() {
            repository.sceneFlowable(experienceId = experience_id, sceneId = anotherScene.id)
                    .subscribeOn(Schedulers.trampoline()).subscribe(testSceneSubscriber)
            testSubscriber.awaitCount(1)
        }

        fun create_new_scene_is_called() {
            resultFlowable = repository.createScene(scene)
            resultFlowable.subscribeOn(Schedulers.trampoline()).subscribe(testSceneSubscriber)
        }

        fun scene_should_be_received() {
            val receivedResult = testSubscriber.events.get(0).get(0) as Result<*>
            val receivedScenes = receivedResult.data as List<*>

            assertEquals(scene, receivedScenes.get(0))
        }

        fun another_scene_should_be_received_second() {
            val receivedResult = testSubscriber.events.get(0).get(1) as Result<*>
            val receivedScenes = receivedResult.data as List<*>

            assertEquals(anotherScene, receivedScenes.get(0))
        }

        fun another_scene_should_be_received_first_on_second_subscriber() {
            val receivedResult = secondTestSubscriber.events.get(0).get(0) as Result<*>
            val receivedScenes = receivedResult.data as List<*>

            assertEquals(anotherScene, receivedScenes.get(0))
        }

        fun one_emition_should_be_made_through_that_observer() {
            testObserver.assertValueCount(1)
        }

        fun only_another_scene_should_be_received() {
            val receivedResult = testSceneSubscriber.events.get(0).get(0) as Result<*>

            assertEquals(anotherScene, receivedResult.data)
        }

        fun api_repository_create_scene_method_should_be_called() {
            BDDMockito.then(mockApiRepository).should().createScene(scene)
        }

        fun an_scene_is_received_by_result_flowable() {
            val receivedResult = testSceneSubscriber.events.get(0).get(0) as Result<*>

            assertEquals(scene, receivedResult.data)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
