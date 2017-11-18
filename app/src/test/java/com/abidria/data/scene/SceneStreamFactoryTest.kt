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

class SceneStreamFactoryTest {

    @Test
    fun test_stream_caches_last_item_emitted() {
        given {
            a_created_stream()
            a_list_of_scenes_is_emitted_through_replace_all_observer()
            a_new_list_is_emitted_through_replace_all_observer()
        } whenn {
            another_observer_subscribes_to_flowable()
        } then {
            this_other_observer_should_received_second_emitted_list()
        }
    }

    @Test
    fun test_emit_through_add_or_update_a_new_scene() {
        given {
            a_created_stream()
            a_list_of_scenes_is_emitted_through_replace_all_observer()
        } whenn {
            new_scene_is_emitted_through_add_or_update()
        } then {
            a_list_with_previous_scenes_and_new_one_should_be_received()
        }
    }

    @Test
    fun test_emit_through_add_or_update_an_old_scene_modified() {
        given {
            a_created_stream()
            a_list_of_scenes_is_emitted_through_replace_all_observer()
        } whenn {
            modified_scene_is_emitted_through_add_or_update()
        } then {
            a_list_with_previous_scenes_but_with_scene_modifications()
        }
    }

    @Test
    fun test_emit_through_replace_all_removes_old_list_and_emits_new_one() {
        given {
            a_created_stream()
            a_list_of_scenes_is_emitted_through_replace_all_observer()
        } whenn {
            a_new_list_is_emitted_through_replace_all_observer()
        } then {
            new_list_should_be_emitted_instead_of_old_one()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var newScene: Scene
        lateinit var updatedScene: Scene
        lateinit var oldScenes: List<Scene>
        lateinit var newScenes: List<Scene>
        lateinit var stream: SceneStreamFactory.ScenesStream
        val testSubscriber: TestSubscriber<Result<List<Scene>>> = TestSubscriber.create()
        val secondTestSubscriber: TestSubscriber<Result<List<Scene>>> = TestSubscriber.create()

        fun buildScenario(): ScenarioMaker {
            newScene = Scene("5", "Title", "description", null,
                             2.6, 1.2, "1")
            updatedScene = Scene("1", "Other", "info", null,
                                 3.4, 0.9, "1")
            val firstScene = Scene("1", "A", "a", null,
                                   3.4, 0.9, "1")
            val secondScene = Scene("2", "B", "b", null,
                                    3.4, 0.9, "1")
            oldScenes = listOf(firstScene, secondScene)
            val thirdScene = Scene("3", "C", "c", null,
                                   3.4, 0.9, "1")
            val forthScene = Scene("4", "D", "d", null,
                                   3.4, 0.9, "1")
            newScenes = listOf(thirdScene, forthScene)

            return this
        }

        fun a_created_stream() {
            stream = SceneStreamFactory().create()
            stream.scenesFlowable.subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        }

        fun a_list_of_scenes_is_emitted_through_replace_all_observer() {
            stream.replaceAllScenesObserver.onNext(Result(oldScenes, null))
        }

        fun new_scene_is_emitted_through_add_or_update() {
            stream.addOrUpdateSceneObserver.onNext(Result(newScene, null))
        }

        fun modified_scene_is_emitted_through_add_or_update() {
            stream.addOrUpdateSceneObserver.onNext(Result(updatedScene, null))
        }

        fun a_new_list_is_emitted_through_replace_all_observer() {
            stream.replaceAllScenesObserver.onNext(Result(newScenes, null))
        }

        fun another_observer_subscribes_to_flowable() {
            stream.scenesFlowable.subscribeOn(Schedulers.trampoline()).subscribe(secondTestSubscriber)
        }

        fun a_list_with_previous_scenes_and_new_one_should_be_received() {
            testSubscriber.awaitCount(2)

            val secondResult = testSubscriber.events.get(0).get(1) as Result<*>
            val secondSceneList = secondResult.data as List<*>
            assertEquals(oldScenes.union(listOf(newScene)).toList(), secondSceneList)
        }

        fun a_list_with_previous_scenes_but_with_scene_modifications() {
            testSubscriber.awaitCount(2)

            val secondResult = testSubscriber.events.get(0).get(1) as Result<*>
            val secondSceneList = secondResult.data as List<*>
            val updatedList = listOf(updatedScene, oldScenes[1])
            assertEquals(updatedList, secondSceneList)
        }

        fun new_list_should_be_emitted_instead_of_old_one() {
            testSubscriber.awaitCount(2)

            val secondResult = testSubscriber.events.get(0).get(1) as Result<*>
            val secondSceneList = secondResult.data as List<*>
            assertEquals(newScenes, secondSceneList)
        }

        fun this_other_observer_should_received_second_emitted_list() {
            secondTestSubscriber.awaitCount(1)

            val firstResult = secondTestSubscriber.events.get(0).get(0) as Result<*>
            val firstScenesList = firstResult.data as List<*>
            assertEquals(newScenes, firstScenesList)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
