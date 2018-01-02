package com.abidria.data.experience

import com.abidria.data.common.Result
import com.abidria.data.common.ResultStreamFactory
import io.reactivex.Flowable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.*
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class ExperienceRepositoryTest {

    @Test
    fun test_my_experiences_flowable_return_my_experiences_and_refresh_them() {
        given {
            an_experiences_stream_factory_that_returns_six_experiences()
            an_api_repo_that_returns_my_experiences_flowable_with_an_experience()
        } whenn {
            my_experiences_flowable_is_called()
        } then {
            should_return_flowable_with_two_mine_experiences()
            should_remove_all_mine_exps_and_add_experience_received_by_api_repo()
        }
    }

    @Test
    fun test_explore_experiences_flowable_return_explore_experiences_and_refresh_them() {
        given {
            an_experiences_stream_factory_that_returns_six_experiences()
            an_api_repo_that_returns_explore_experiences_flowable_with_an_experience()
        } whenn {
            explore_experiences_flowable_is_called()
        } then {
            should_return_flowable_with_two_not_mine_and_not_saved_experiences()
            should_remove_all_not_mine_but_not_saved_exps_and_add_experience_received_by_api_repo()
        }
    }

    @Test
    fun test_saved_experiences_flowable_return_saved_experiences_and_refresh_them() {
        given {
            an_experiences_stream_factory_that_returns_six_experiences()
            an_api_repo_that_returns_saved_experiences_flowable_with_an_experience()
        } whenn {
            saved_experiences_flowable_is_called()
        } then {
            should_return_flowable_with_two_saved_experiences()
            should_remove_all_saved_exps_and_add_experience_received_by_api_repo()
        }
    }

    @Test
    fun test_experience_flowable_returns_experiences_flowable_filtering_desired_experience() {
        given {
            an_experience_id()
            an_experiences_stream_factory_that_returns_stream_with_several_experiences()
        } whenn {
            experience_flowable_is_called_with_experience_id()
        } then {
            only_experience_with_experience_id_should_be_received()
        }
    }

    @Test
    fun test_create_experience_calls_api_repo_and_emits_through_add_observer_the_new_experience() {
        given {
            an_experience_id()
            an_experience()
            an_experiences_stream_factory_that_returns_stream()
            an_api_repo_that_returns_created_experience()
        } whenn {
            experiences_flowable_is_called_with_experience_id()
            create_experience_is_called()
        } then {
            should_call_api_created_experience()
            should_emit_created_experience_through_add_or_update_experiences_observer()
        }
    }

    @Test
    fun test_upload_experience_picture_calls_api_repo_with_delegate_to_emit_through_update_observer() {
        given {
            an_experience_id()
            an_experience()
            a_cropped_image_uri_string()
            an_experiences_stream_factory_that_returns_stream()
        } whenn {
            experiences_flowable_is_called_with_experience_id()
            upload_experience_picture_is_called()
        } then {
            should_call_api_upload_experience_picture_with_experience_id_and_image_uri_string()
        } whenn {
            delegate_is_called_with_experience()
        } then {
            delegate_param_should_emit_experience_through_add_or_update_observer()
        }

    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().start(func)

    @Suppress("UNCHECKED_CAST")
    class ScenarioMaker {
        lateinit var repository: ExperienceRepository
        @Mock lateinit var mockApiRepository: ExperienceApiRepository
        @Mock lateinit var mockExperiencesStreamFactory: ResultStreamFactory<Experience>
        var experienceId = ""
        var croppedImageUriString = ""
        lateinit var experience: Experience
        lateinit var experiencesFlowable: Flowable<Result<List<Experience>>>
        lateinit var addOrUpdateObserver: TestObserver<Result<List<Experience>>>
        lateinit var removeAllThatObserver: TestObserver<(Experience) -> Boolean>
        lateinit var apiExperiencesFlowable: Flowable<Result<List<Experience>>>
        lateinit var experiencesFlowableResult: Flowable<Result<List<Experience>>>
        lateinit var experienceFlowableResult: Flowable<Result<Experience>>
        lateinit var createdExperienceFlowableResult: Flowable<Result<Experience>>
        lateinit var experienceA: Experience
        lateinit var experienceB: Experience
        lateinit var experienceC: Experience
        lateinit var experienceD: Experience
        lateinit var experienceE: Experience
        lateinit var experienceF: Experience

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            repository = ExperienceRepository(mockApiRepository, mockExperiencesStreamFactory)

            return this
        }

        fun an_experience_id() {
            experienceId = "1"
        }

        fun an_experience() {
            experience = Experience(id = "", title = "Title", description = "some desc.", picture = null)
        }

        fun a_cropped_image_uri_string() {
            croppedImageUriString = "image_uri"
        }

        fun an_api_repo_that_returns_created_experience() {
            val createdExperienceFlowable = Flowable.just(Result(experience, null))
            BDDMockito.given(mockApiRepository.createExperience(experience)).willReturn(createdExperienceFlowable)
        }

        fun an_experiences_stream_factory_that_returns_six_experiences() {
            experienceA = Experience(id = "1", title = "T", description = "desc", picture = null)
            experienceB = Experience(id = "2", title = "T", description = "desc", picture = null)
            experienceC = Experience(id = "3", title = "T", description = "desc", picture = null, isMine = true)
            experienceD = Experience(id = "4", title = "T", description = "desc", picture = null, isMine = true)
            experienceE = Experience(id = "5", title = "T", description = "desc", picture = null, isSaved = true)
            experienceF = Experience(id = "6", title = "T", description = "desc", picture = null, isSaved = true)
            addOrUpdateObserver = TestObserver.create()
            addOrUpdateObserver.onSubscribe(addOrUpdateObserver)
            removeAllThatObserver = TestObserver.create()
            experiencesFlowable = Flowable.just(Result(listOf(experienceA, experienceB, experienceC,
                                                              experienceD, experienceE, experienceF), null))
            BDDMockito.given(mockExperiencesStreamFactory.create()).willReturn(
                    ResultStreamFactory.ResultStream(addOrUpdateObserver, removeAllThatObserver, experiencesFlowable))
        }

        fun an_experiences_stream_factory_that_returns_stream() {
            addOrUpdateObserver = TestObserver.create()
            addOrUpdateObserver.onSubscribe(addOrUpdateObserver)
            removeAllThatObserver = TestObserver.create()
            experiencesFlowable = Flowable.never()
            BDDMockito.given(mockExperiencesStreamFactory.create()).willReturn(
                    ResultStreamFactory.ResultStream(addOrUpdateObserver, removeAllThatObserver, experiencesFlowable))
        }

        fun an_experiences_stream_factory_that_returns_stream_with_several_experiences() {
            val experienceA = Experience(id = "1", title = "T", description = "desc", picture = null)
            val experienceB = Experience(id = "2", title = "T", description = "desc", picture = null)
            addOrUpdateObserver = TestObserver.create()
            removeAllThatObserver = TestObserver.create()
            experiencesFlowable = Flowable.just(Result(listOf(experienceA, experienceB), null))
            BDDMockito.given(mockExperiencesStreamFactory.create()).willReturn(
                    ResultStreamFactory.ResultStream(addOrUpdateObserver, removeAllThatObserver, experiencesFlowable))
        }

        fun an_api_repo_that_returns_my_experiences_flowable_with_an_experience() {
            experience = Experience("2", "T", "d", null, isMine = true)
            apiExperiencesFlowable = Flowable.just(Result(listOf(experience), null))

            BDDMockito.given(mockApiRepository.myExperiencesFlowable()).willReturn(apiExperiencesFlowable)
        }

        fun an_api_repo_that_returns_explore_experiences_flowable_with_an_experience() {
            experience = Experience("2", "T", "d", null, isMine = true)
            apiExperiencesFlowable = Flowable.just(Result(listOf(experience), null))

            BDDMockito.given(mockApiRepository.exploreExperiencesFlowable()).willReturn(apiExperiencesFlowable)
        }

        fun an_api_repo_that_returns_saved_experiences_flowable_with_an_experience() {
            experience = Experience("2", "T", "d", null, isMine = true)
            apiExperiencesFlowable = Flowable.just(Result(listOf(experience), null))

            BDDMockito.given(mockApiRepository.savedExperiencesFlowable()).willReturn(apiExperiencesFlowable)
        }

        fun my_experiences_flowable_is_called() {
            experiencesFlowableResult = repository.myExperiencesFlowable()
        }

        fun explore_experiences_flowable_is_called() {
            experiencesFlowableResult = repository.exploreExperiencesFlowable()
        }

        fun saved_experiences_flowable_is_called() {
            experiencesFlowableResult = repository.savedExperiencesFlowable()
        }

        fun experiences_flowable_is_called_with_experience_id() {
            experienceFlowableResult = repository.experienceFlowable(experienceId)
        }

        fun create_experience_is_called() {
            createdExperienceFlowableResult = repository.createExperience(experience)
        }

        fun delegate_is_called_with_experience() {
            repository.emitThroughAddOrUpdate.invoke(Result(experience, null))
        }

        fun experience_flowable_is_called_with_experience_id() {
            experienceFlowableResult = repository.experienceFlowable(experienceId)
        }

        fun upload_experience_picture_is_called() {
            repository.uploadExperiencePicture(experienceId, croppedImageUriString)
        }

        fun should_return_flowable_with_two_mine_experiences() {
            val testSubscriber = TestSubscriber<Result<List<Experience>>>()
            experiencesFlowableResult.subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
            val result = testSubscriber.events.get(0).get(0) as Result<*>
            assertEquals(result, Result(listOf(experienceC, experienceD), null))
        }

        fun should_return_flowable_with_two_not_mine_and_not_saved_experiences() {
            val testSubscriber = TestSubscriber<Result<List<Experience>>>()
            experiencesFlowableResult.subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
            testSubscriber.assertResult(Result(listOf(experienceA, experienceB), null))
        }

        fun should_return_flowable_with_two_saved_experiences() {
            val testSubscriber = TestSubscriber<Result<List<Experience>>>()
            experiencesFlowableResult.subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
            testSubscriber.assertResult(Result(listOf(experienceE, experienceF), null))
        }

        fun should_remove_all_mine_exps_and_add_experience_received_by_api_repo() {
            removeAllThatObserver.onComplete()
            val lambda = removeAllThatObserver.events.get(0).get(0) as (Experience) -> Boolean
            assertTrue(lambda(Experience("1", "T", "d", picture = null, isMine = true)))
            assertFalse(lambda(Experience("1", "T", "d", picture = null, isMine = false)))
            addOrUpdateObserver.onComplete()
            addOrUpdateObserver.assertResult(Result(listOf(experience), null))
        }

        fun should_remove_all_not_mine_but_not_saved_exps_and_add_experience_received_by_api_repo() {
            removeAllThatObserver.onComplete()
            val lambda = removeAllThatObserver.events.get(0).get(0) as (Experience) -> Boolean
            assertFalse(lambda(Experience("1", "T", "d", picture = null, isMine = true)))
            assertFalse(lambda(Experience("1", "T", "d", picture = null, isSaved = true)))
            assertTrue(lambda(Experience("1", "T", "d", picture = null, isMine = false)))
            addOrUpdateObserver.onComplete()
            addOrUpdateObserver.assertResult(Result(listOf(experience), null))
        }

        fun should_remove_all_saved_exps_and_add_experience_received_by_api_repo() {
            removeAllThatObserver.onComplete()
            val lambda = removeAllThatObserver.events.get(0).get(0) as (Experience) -> Boolean
            assertFalse(lambda(Experience("1", "T", "d", picture = null, isSaved = false)))
            assertTrue(lambda(Experience("1", "T", "d", picture = null, isSaved = true)))
            addOrUpdateObserver.onComplete()
            addOrUpdateObserver.assertResult(Result(listOf(experience), null))
        }

        fun only_experience_with_experience_id_should_be_received() {
            val testSubscriber = TestSubscriber.create<Result<Experience>>()
            experienceFlowableResult.subscribeOn(Schedulers.trampoline()).subscribe(testSubscriber)
            testSubscriber.awaitCount(1)
            val result = testSubscriber.events.get(0).get(0) as Result<*>
            val receivedExperience = result.data as Experience
            assertEquals(experienceId, receivedExperience.id)
        }

        fun should_call_api_upload_experience_picture_with_experience_id_and_image_uri_string() {
            BDDMockito.then(mockApiRepository).should()
                    .uploadExperiencePicture(experienceId, croppedImageUriString, repository.emitThroughAddOrUpdate)
        }

        fun delegate_param_should_emit_experience_through_add_or_update_observer() {
            addOrUpdateObserver.onComplete()
            addOrUpdateObserver.assertResult(Result(listOf(experience), null))
        }

        fun should_call_api_created_experience() {
            BDDMockito.then(mockApiRepository).should().createExperience(experience)
        }

        fun should_emit_created_experience_through_add_or_update_experiences_observer() {
            createdExperienceFlowableResult.subscribeOn(Schedulers.trampoline()).subscribe()
            addOrUpdateObserver.onComplete()
            addOrUpdateObserver.assertResult(Result(listOf(experience), null))
        }

        infix fun start(func: ScenarioMaker.() -> Unit) = buildScenario().given(func)
        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
