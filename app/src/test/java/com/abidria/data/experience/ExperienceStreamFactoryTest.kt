package com.abidria.data.experience

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

class ExperienceStreamFactoryTest {

    @Test
    fun test_stream_caches_last_item_emitted() {
        given {
            a_created_stream()
            a_list_of_experiences_is_emitted_through_replace_all_observer()
            a_new_list_is_emitted_through_replace_all_observer()
        } whenn {
            another_observer_subscribes_to_flowable()
        } then {
            this_other_observer_should_received_second_emitted_list()
        }
    }

    @Test
    fun test_emit_through_add_or_update_a_new_experience() {
        given {
            a_created_stream()
            a_list_of_experiences_is_emitted_through_replace_all_observer()
        } whenn {
            new_experience_is_emitted_through_add_or_update()
        } then {
            a_list_with_previous_experiences_and_new_one_should_be_received()
        }
    }

    @Test
    fun test_emit_through_add_or_update_an_old_experience_modified() {
        given {
            a_created_stream()
            a_list_of_experiences_is_emitted_through_replace_all_observer()
        } whenn {
            modified_experience_is_emitted_through_add_or_update()
        } then {
            a_list_with_previous_experiences_but_with_experience_modifications()
        }
    }

    @Test
    fun test_emit_through_replace_all_removes_old_list_and_emits_new_one() {
        given {
            a_created_stream()
            a_list_of_experiences_is_emitted_through_replace_all_observer()
        } whenn {
            a_new_list_is_emitted_through_replace_all_observer()
        } then {
            new_list_should_be_emitted_instead_of_old_one()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        lateinit var newExperience: Experience
        lateinit var updatedExperience: Experience
        lateinit var oldExperiences: List<Experience>
        lateinit var newExperiences: List<Experience>
        lateinit var stream: ExperienceStreamFactory.ExperiencesStream
        val testSubscriber: TestSubscriber<Result<List<Experience>>> = TestSubscriber.create()
        val secondTestSubscriber: TestSubscriber<Result<List<Experience>>> = TestSubscriber.create()

        fun buildScenario(): ScenarioMaker {
            newExperience = Experience("5", "Title", "description", null)
            updatedExperience = Experience("1", "Other", "info", null)
            val firstExperience = Experience("1", "A", "a", null)
            val secondExperience = Experience("2", "B", "b", null)
            oldExperiences = listOf(firstExperience, secondExperience)
            val thirdExperience = Experience("3", "C", "c", null)
            val forthExperience = Experience("4", "D", "d", null)
            newExperiences = listOf(thirdExperience, forthExperience)

            return this
        }

        fun a_created_stream() {
            stream = ExperienceStreamFactory().create()
            stream.experiencesFlowable.subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
        }

        fun a_list_of_experiences_is_emitted_through_replace_all_observer() {
            stream.replaceAllExperiencesObserver.onNext(Result(oldExperiences, null))
        }

        fun new_experience_is_emitted_through_add_or_update() {
            stream.addOrUpdateExperienceObserver.onNext(Result(newExperience, null))
        }

        fun modified_experience_is_emitted_through_add_or_update() {
            stream.addOrUpdateExperienceObserver.onNext(Result(updatedExperience, null))
        }

        fun a_new_list_is_emitted_through_replace_all_observer() {
            stream.replaceAllExperiencesObserver.onNext(Result(newExperiences, null))
        }

        fun another_observer_subscribes_to_flowable() {
            stream.experiencesFlowable.subscribeOn(Schedulers.trampoline()).subscribe(secondTestSubscriber)
        }

        fun a_list_with_previous_experiences_and_new_one_should_be_received() {
            testSubscriber.awaitCount(2)

            val secondResult = testSubscriber.events.get(0).get(1) as Result<*>
            val secondExperienceList = secondResult.data as List<*>
            assertEquals(oldExperiences.union(listOf(newExperience)).toList(), secondExperienceList)
        }

        fun a_list_with_previous_experiences_but_with_experience_modifications() {
            testSubscriber.awaitCount(2)

            val secondResult = testSubscriber.events.get(0).get(1) as Result<*>
            val secondExperienceList = secondResult.data as List<*>
            val updatedList = listOf(updatedExperience, oldExperiences[1])
            assertEquals(updatedList, secondExperienceList)
        }

        fun new_list_should_be_emitted_instead_of_old_one() {
            testSubscriber.awaitCount(2)

            val secondResult = testSubscriber.events.get(0).get(1) as Result<*>
            val secondExperienceList = secondResult.data as List<*>
            assertEquals(newExperiences, secondExperienceList)
        }

        fun this_other_observer_should_received_second_emitted_list() {
            secondTestSubscriber.awaitCount(1)

            val firstResult = secondTestSubscriber.events.get(0).get(0) as Result<*>
            val firstExperiencesList = firstResult.data as List<*>
            assertEquals(newExperiences, firstExperiencesList)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
