package com.pachatary.data.common

import com.pachatary.data.scene.Scene
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertEquals
import org.junit.Test

class ResultCacheFactoryTest {

    @Test
    fun test_cache_emits_automatically_result_with_none_last_event() {
        given {
            nothing()
        } whenn {
            a_created_cache()
        } then {
            this_other_observer_should_receive_initial_result()
        }
    }

    @Test
    fun test_cache_caches_last_item_emitted() {
        given {
            a_created_cache()
            a_list_of_scenes_is_emitted_through_add_list_observer(
                                                            ResultCacheFactory.AddPosition.START)
            a_new_list_is_emitted_through_replace()
        } whenn {
            another_observer_subscribes_to_flowable()
        } then {
            this_other_observer_should_received_second_emitted_list()
        }
    }

    @Test
    fun test_emit_through_add_or_update_a_new_scene_adds_it_on_specified_position() {
        for (position in ResultCacheFactory.AddPosition.values()) {
            given {
                a_created_cache()
                a_list_of_scenes_is_emitted_through_add_list_observer(
                                                            ResultCacheFactory.AddPosition.START)
            } whenn {
                new_scene_is_emitted_through_add_or_update(position)
            } then {
                should_receive_a_list_with_new_scene_and_previous_scenes(position)
            }
        }
    }

    @Test
    fun test_emit_through_add_or_update_an_old_scene_modified() {
        given {
            a_created_cache()
            a_list_of_scenes_is_emitted_through_add_list_observer(
                                                            ResultCacheFactory.AddPosition.START)
        } whenn {
            modified_scene_is_emitted_through_add_or_update(ResultCacheFactory.AddPosition.START)
        } then {
            a_list_with_previous_scenes_but_with_scene_modifications()
        }
    }

    @Test
    fun test_emit_through_add_list_adds_elements_at_the_end() {
        given {
            a_created_cache()
            a_list_of_scenes_is_emitted_through_add_list_observer(
                                                            ResultCacheFactory.AddPosition.START)
        } whenn {
            a_new_list_is_emitted_through_add_list_observer(ResultCacheFactory.AddPosition.END)
        } then {
            new_list_should_be_appended_to_old_one()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var newScene: Scene
        lateinit var secondScene: Scene
        lateinit var updatedScene: Scene
        lateinit var oldScenes: List<Scene>
        lateinit var newScenes: List<Scene>
        lateinit var cache: ResultCacheFactory.ResultCache<Scene>
        val testSubscriber: TestSubscriber<Result<List<Scene>>> = TestSubscriber.create()
        val secondTestSubscriber: TestSubscriber<Result<List<Scene>>> = TestSubscriber.create()

        fun buildScenario(): ScenarioMaker {
            newScene = Scene("5", "Title", "description", null, 2.6, 1.2, "1")
            updatedScene = Scene("1", "Other", "info", null, 3.4, 0.9, "1")
            val firstScene = Scene("1", "A", "a", null, 3.4, 0.9, "1")
            secondScene = Scene("2", "B", "b", null, 3.4, 0.9, "1")
            oldScenes = listOf(firstScene, secondScene)
            val thirdScene = Scene("3", "C", "c", null, 3.4, 0.9, "1")
            val forthScene = Scene("4", "D", "d", null, 3.4, 0.9, "1")
            newScenes = listOf(thirdScene, forthScene)

            return this
        }

        fun nothing() {}

        fun a_created_cache() {
            cache = ResultCacheFactory<Scene>().create()
            cache.resultFlowable.subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        }

        fun a_list_of_scenes_is_emitted_through_add_list_observer(
                                                        position: ResultCacheFactory.AddPosition) {
            cache.addOrUpdateObserver.onNext(Pair(oldScenes, position))
        }

        fun new_scene_is_emitted_through_add_or_update(position: ResultCacheFactory.AddPosition) {
            cache.addOrUpdateObserver.onNext(Pair(listOf(newScene), position))
        }

        fun modified_scene_is_emitted_through_add_or_update(
                                                        position: ResultCacheFactory.AddPosition) {
            cache.addOrUpdateObserver.onNext(Pair(listOf(updatedScene), position))
        }

        fun a_new_list_is_emitted_through_add_list_observer(
                                                        position: ResultCacheFactory.AddPosition) {
            cache.addOrUpdateObserver.onNext(Pair(newScenes, position))
        }

        fun a_new_list_is_emitted_through_replace() {
            cache.replaceResultObserver.onNext(ResultSuccess(newScenes))
        }

        fun another_observer_subscribes_to_flowable() {
            cache.resultFlowable.subscribeOn(Schedulers.trampoline())
                    .subscribe(secondTestSubscriber)
        }

        fun should_receive_a_list_with_new_scene_and_previous_scenes(
                                                        position: ResultCacheFactory.AddPosition) {
            testSubscriber.awaitCount(3)

            val secondResult = testSubscriber.events.get(0).get(2) as Result<*>
            val secondSceneList = secondResult.data as List<*>
            if (position == ResultCacheFactory.AddPosition.START)
                assertEquals(listOf(newScene).union(oldScenes).toList(), secondSceneList)
            else
                assertEquals(oldScenes.union(listOf(newScene)).toList(), secondSceneList)
        }

        fun a_list_with_previous_scenes_but_with_scene_modifications() {
            testSubscriber.awaitCount(3)

            val secondResult = testSubscriber.events.get(0).get(2) as Result<*>
            val secondSceneList = secondResult.data as List<*>
            val updatedList = listOf(updatedScene, oldScenes[1])
            assertEquals(updatedList, secondSceneList)
        }

        fun new_list_should_be_appended_to_old_one() {
            testSubscriber.awaitCount(3)

            val secondResult = testSubscriber.events.get(0).get(2) as Result<*>
            val secondSceneList = secondResult.data as List<*>
            assertEquals(oldScenes.union(newScenes).toList(), secondSceneList)
        }

        fun this_other_observer_should_receive_initial_result() {
            testSubscriber.awaitCount(1)

            val result = testSubscriber.events.get(0).get(0) as Result<*>
            assertEquals(ResultSuccess<List<Scene>>(listOf(), action = Request.Action.NONE), result)
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
