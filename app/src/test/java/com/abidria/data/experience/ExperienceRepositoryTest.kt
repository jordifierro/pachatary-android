package com.abidria.data.experience

import com.abidria.data.common.Result
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


class ExperienceRepositoryTest {

    @Test
    fun test_experiences_flowable_return_stream_flowable_connected_with_api_request() {
        given {
            an_experiences_stream_factory_that_returns_stream()
            an_api_repo_that_returns_experiences_flowable_with_an_experience()
        } whenn {
            experiences_flowable_is_called()
        } then {
            should_return_flowable_created_by_factory()
            should_connect_api_experiences_flowable_on_next_to_replace_all_experiences_observer()
        }
    }

    @Test
    fun test_on_refresh_experiences_asks_again_to_api_repo_and_emits_new_result_through_replace_all() {
        given {
            an_experiences_stream_factory_that_returns_stream()
            an_api_repo_that_returns_experiences_flowable_with_an_experience()
        } whenn {
            experiences_flowable_is_called()
        } given {
            an_api_repo_that_returns_experiences_flowable_with_another_experience()
        } whenn {
            refresh_experiences_is_called()
        } then {
            should_emit_first_and_second_experience_through_replace_all()
        }
    }

    @Test
    fun test_same_experience_experiences_flowable_call_returns_same_flowable() {
        given {
            an_experiences_stream_factory_that_returns_stream()
            an_experiences_stream_factory_that_returns_another_stream_when_called_again()
            an_api_repo_that_returns_experiences_flowable_with_an_experience()
        } whenn {
            experiences_flowable_is_called()
            experiences_flowable_is_called_again()
        } then {
            first_result_should_be_experiences_flowable()
            second_result_should_be_same_experiences_flowable()
        }
    }

    @Test
    fun test_experience_flowable_returns_experiences_flowable_filtering_desired_experience() {
        given {
            an_experience_id()
            an_experiences_stream_factory_that_returns_stream_with_several_experiences()
            an_api_repo_that_returns_experiences_flowable_with_an_experience()
        } whenn {
            experience_flowable_is_called_with_experience_id()
        } then {
            only_experience_with_experience_id_should_be_received()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().start(func)

    class ScenarioMaker {
        lateinit var repository: ExperienceRepository
        @Mock lateinit var mockApiRepository: ExperienceApiRepository
        @Mock lateinit var mockExperiencesStreamFactory: ExperienceStreamFactory
        var experienceId = ""
        lateinit var experience: Experience
        lateinit var secondExperience: Experience
        lateinit var experiencesFlowable: Flowable<Result<List<Experience>>>
        lateinit var addOrUpdateObserver: TestObserver<Result<Experience>>
        lateinit var replaceAllObserver: TestObserver<Result<List<Experience>>>
        lateinit var secondExperiencesFlowable: Flowable<Result<List<Experience>>>
        lateinit var secondAddOrUpdateObserver: TestObserver<Result<Experience>>
        lateinit var secondReplaceAllObserver: TestObserver<Result<List<Experience>>>
        lateinit var apiExperiencesFlowable: Flowable<Result<List<Experience>>>
        lateinit var experiencesFlowableResult: Flowable<Result<List<Experience>>>
        lateinit var experienceFlowableResult: Flowable<Result<Experience>>

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            repository = ExperienceRepository(mockApiRepository, mockExperiencesStreamFactory)

            return this
        }

        fun an_experience_id() {
            experienceId = "1"
        }

        fun an_experiences_stream_factory_that_returns_stream() {
            replaceAllObserver = TestObserver.create()
            replaceAllObserver.onSubscribe(replaceAllObserver)
            addOrUpdateObserver = TestObserver.create()
            addOrUpdateObserver.onSubscribe(addOrUpdateObserver)
            experiencesFlowable = Flowable.never()
            BDDMockito.given(mockExperiencesStreamFactory.create()).willReturn(
                    ExperienceStreamFactory.ExperiencesStream(replaceAllObserver, addOrUpdateObserver, experiencesFlowable))
        }

        fun an_experiences_stream_factory_that_returns_another_stream_when_called_again() {
            secondReplaceAllObserver = TestObserver.create()
            secondReplaceAllObserver.onSubscribe(replaceAllObserver)
            secondAddOrUpdateObserver = TestObserver.create()
            secondAddOrUpdateObserver.onSubscribe(addOrUpdateObserver)
            secondExperiencesFlowable = Flowable.never()
            BDDMockito.given(mockExperiencesStreamFactory.create()).willReturn(
                    ExperienceStreamFactory.ExperiencesStream(secondReplaceAllObserver,
                                                    secondAddOrUpdateObserver, secondExperiencesFlowable))
        }

        fun an_experiences_stream_factory_that_returns_stream_with_several_experiences() {
            val experienceA = Experience(id = "1", title = "T", description = "desc", picture = null)
            val experienceB = Experience(id = "2", title = "T", description = "desc", picture = null)
            replaceAllObserver = TestObserver.create()
            addOrUpdateObserver = TestObserver.create()
            experiencesFlowable = Flowable.just(Result(listOf(experienceA, experienceB), null))
            BDDMockito.given(mockExperiencesStreamFactory.create()).willReturn(
                    ExperienceStreamFactory.ExperiencesStream(replaceAllObserver, addOrUpdateObserver, experiencesFlowable))
        }

        fun an_api_repo_that_returns_experiences_flowable_with_an_experience() {
            experience = Experience("2", "T", "d", null)
            apiExperiencesFlowable = Flowable.just(Result(listOf(experience), null))

            BDDMockito.given(mockApiRepository.experiencesFlowable()).willReturn(apiExperiencesFlowable)
        }

        fun an_api_repo_that_returns_experiences_flowable_with_another_experience() {
            secondExperience = Experience("4", "Y", "g", null)

            BDDMockito.given(mockApiRepository.experiencesFlowable())
                    .willReturn(Flowable.just(Result(listOf(secondExperience), null)))
        }

        fun experiences_flowable_is_called() {
            experiencesFlowableResult = repository.experiencesFlowable()
        }

        fun refresh_experiences_is_called() {
            repository.refreshExperiences()
        }

        fun experiences_flowable_is_called_again() {
            secondExperiencesFlowable = repository.experiencesFlowable()
        }

        fun experience_flowable_is_called_with_experience_id() {
            experienceFlowableResult = repository.experienceFlowable(experienceId)
        }

        fun should_return_flowable_created_by_factory() {
            Assert.assertEquals(experiencesFlowable, experiencesFlowableResult)
        }

        fun should_connect_api_experiences_flowable_on_next_to_replace_all_experiences_observer() {
            replaceAllObserver.onComplete()
            replaceAllObserver.assertResult(Result(listOf(experience), null))
        }

        fun should_emit_first_and_second_experience_through_replace_all() {
            replaceAllObserver.onComplete()
            replaceAllObserver.assertResult(Result(listOf(experience), null),
                                            Result(listOf(secondExperience), null))
        }

        fun first_result_should_be_experiences_flowable() {
            Assert.assertEquals(experiencesFlowable, experiencesFlowableResult)
        }

        fun second_result_should_be_same_experiences_flowable() {
            Assert.assertEquals(secondExperiencesFlowable, experiencesFlowableResult)
        }

        fun only_experience_with_experience_id_should_be_received() {
            val testSubscriber = TestSubscriber.create<Result<Experience>>()
            experienceFlowableResult.subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
            val result = testSubscriber.events.get(0).get(0) as Result<*>
            val receivedExperience = result.data as Experience
            assertEquals(experienceId, receivedExperience.id)
        }

        infix fun start(func: ScenarioMaker.() -> Unit) = buildScenario().given(func)
        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
