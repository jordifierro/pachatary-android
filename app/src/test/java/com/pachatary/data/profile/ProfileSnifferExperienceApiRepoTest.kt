package com.pachatary.data.profile

import com.pachatary.data.DummyExperience
import com.pachatary.data.DummyExperienceResultSuccess
import com.pachatary.data.DummyExperiencesResultSuccess
import com.pachatary.data.DummyProfile
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultSuccess
import com.pachatary.data.experience.Experience
import com.pachatary.data.experience.ExperienceApiRepository
import io.reactivex.Flowable
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class ProfileSnifferExperienceApiRepoTest {

    @Test
    fun test_explore_experiences_is_sniffed() {
        given {
            an_explore_experiences_that_returns(DummyExperiencesResultSuccess(
                    listOf("1", "2", "3"), listOf("a", "b", "c")))
        } whenn {
            explore_experiences()
        } then {
            should_sniff(listOf(DummyProfile("a"), DummyProfile("b"), DummyProfile("c")))
        }
    }

    @Test
    fun test_my_experiences_is_sniffed() {
        given {
            my_experiences_that_returns(DummyExperiencesResultSuccess(
                    listOf("1", "2", "3"), listOf("a", "b", "c")))
        } whenn {
            my_experiences()
        } then {
            should_sniff(listOf(DummyProfile("a"), DummyProfile("b"), DummyProfile("c")))
        }
    }

    @Test
    fun test_saved_experiences_is_sniffed() {
        given {
            saved_experiences_that_returns(DummyExperiencesResultSuccess(
                    listOf("1", "2", "3"), listOf("a", "b", "c")))
        } whenn {
            saved_experiences()
        } then {
            should_sniff(listOf(DummyProfile("a"), DummyProfile("b"), DummyProfile("c")))
        }
    }

    @Test
    fun test_persons_experiences_is_not_sniffed() {
        given {
            persons_experiences_that_returns("username", DummyExperiencesResultSuccess(
                    listOf("1", "2", "3"), listOf("a", "b", "c")))
        } whenn {
            persons_experiences("username")
        } then {
            should_sniff_nothing()
        }
    }

    @Test
    fun test_paginate_experiences_is_sniffed() {
        given {
            paginate_experiences_that_returns("username", DummyExperiencesResultSuccess(
                    listOf("1", "2", "3"), listOf("a", "b", "c")))
        } whenn {
            paginate_experiences("username")
        } then {
            should_sniff(listOf(DummyProfile("a"), DummyProfile("b"), DummyProfile("c")))
        }
    }

    @Test
    fun test_experience_is_sniffed() {
        given {
            experience_that_returns("5", DummyExperienceResultSuccess("1", "a"))
        } whenn {
            experience("5")
        } then {
            should_only_sniff(DummyProfile("a"))
        }
    }

    @Test
    fun test_create_experience_is_not_sniffed() {
        given {
            create_experience_that_returns(DummyExperience("3"), DummyExperienceResultSuccess("1", "a"))
        } whenn {
            create_experience(DummyExperience("3"))
        } then {
            should_sniff_nothing()
        }
    }

    @Test
    fun test_edit_experience_is_not_sniffed() {
        given {
            edit_experience_that_returns(DummyExperience("3"), DummyExperienceResultSuccess("1", "a"))
        } whenn {
            edit_experience(DummyExperience("3"))
        } then {
            should_sniff_nothing()
        }
    }

    @Test
    fun test_save_experience_is_not_sniffed() {
        given {
            save_experience_that_returns_void("3", true)
        } whenn {
            save_experience("3", true)
        } then {
            should_sniff_nothing()
        }
    }

    @Test
    fun test_translate_experience_id_is_not_sniffed() {
        given {
            translate_id_that_returns("3", "4")
        } whenn {
            translate_id("3")
        } then {
            should_sniff_nothing()
        }
    }

    @Test
    fun test_upload_experience_picture_is_not_sniffed() {
        given {
            upload_experience_picture_that_returns("3", "file", DummyExperienceResultSuccess("1", "a"))
        } whenn {
            upload_experience_picture("3", "file")
        } then {
            should_sniff_nothing()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().start(func)

    class ScenarioMaker {
        private lateinit var sniffer: ProfileSnifferExperienceApiRepo
        @Mock private lateinit var mockApiRepository: ExperienceApiRepository
        @Mock private lateinit var mockProfileRepo: ProfileRepository

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            sniffer = ProfileSnifferExperienceApiRepo(mockProfileRepo, mockApiRepository)

            return this
        }

        fun an_explore_experiences_that_returns(result: Result<List<Experience>>) {
            BDDMockito.given(mockApiRepository.exploreExperiencesFlowable(null, null, null))
                    .willReturn(Flowable.just(result))
        }

        fun my_experiences_that_returns(result: Result<List<Experience>>) {
            BDDMockito.given(mockApiRepository.myExperiencesFlowable())
                    .willReturn(Flowable.just(result))
        }

        fun saved_experiences_that_returns(result: Result<List<Experience>>) {
            BDDMockito.given(mockApiRepository.savedExperiencesFlowable())
                    .willReturn(Flowable.just(result))
        }

        fun persons_experiences_that_returns(username: String, result: Result<List<Experience>>) {
            BDDMockito.given(mockApiRepository.personsExperienceFlowable(username))
                    .willReturn(Flowable.just(result))
        }

        fun paginate_experiences_that_returns(url: String, result: Result<List<Experience>>) {
            BDDMockito.given(mockApiRepository.paginateExperiences(url))
                    .willReturn(Flowable.just(result))
        }

        fun experience_that_returns(experienceId: String, result: Result<Experience>) {
            BDDMockito.given(mockApiRepository.experienceFlowable(experienceId))
                    .willReturn(Flowable.just(result))
        }

        fun create_experience_that_returns(experience: Experience, result: Result<Experience>) {
            BDDMockito.given(mockApiRepository.createExperience(experience))
                    .willReturn(Flowable.just(result))
        }

        fun edit_experience_that_returns(experience: Experience, result: Result<Experience>) {
            BDDMockito.given(mockApiRepository.editExperience(experience))
                    .willReturn(Flowable.just(result))
        }

        fun save_experience_that_returns_void(experienceId: String, save: Boolean) {
            BDDMockito.given(mockApiRepository.saveExperience(save, experienceId))
                    .willReturn(Flowable.just(ResultSuccess()))
        }

        fun translate_id_that_returns(shareId: String, experienceId: String) {
            BDDMockito.given(mockApiRepository.translateShareId(shareId))
                    .willReturn(Flowable.just(ResultSuccess(experienceId)))

        }

        fun upload_experience_picture_that_returns(experienceId: String, imageUriString: String,
                                                   result: Result<Experience>) {
            BDDMockito.given(mockApiRepository.uploadExperiencePicture(experienceId, imageUriString))
                    .willReturn(Flowable.just(result))
        }

        fun explore_experiences() {
            sniffer.exploreExperiencesFlowable(null, null, null).subscribe()
        }

        fun my_experiences() {
            sniffer.myExperiencesFlowable().subscribe()
        }

        fun saved_experiences() {
            sniffer.savedExperiencesFlowable().subscribe()
        }

        fun persons_experiences(username: String) {
            sniffer.personsExperienceFlowable(username).subscribe()
        }

        fun paginate_experiences(url: String) {
            sniffer.paginateExperiences(url).subscribe()
        }

        fun experience(experienceId: String) {
            sniffer.experienceFlowable(experienceId).subscribe()
        }

        fun create_experience(experience: Experience) {
            sniffer.createExperience(experience).subscribe()
        }

        fun edit_experience(experience: Experience) {
            sniffer.editExperience(experience).subscribe()
        }

        fun save_experience(experienceId: String, save: Boolean) {
            sniffer.saveExperience(save, experienceId).subscribe()
        }

        fun translate_id(shareId: String) {
            sniffer.translateShareId(shareId).subscribe()
        }

        fun upload_experience_picture(experienceId: String, imageUriString: String) {
            sniffer.uploadExperiencePicture(experienceId, imageUriString).subscribe()
        }

        fun should_sniff(profiles: List<Profile>) {
            for (profile in profiles)
                BDDMockito.then(mockProfileRepo).should().cacheProfile(profile)
            BDDMockito.verifyNoMoreInteractions(mockProfileRepo)
        }

        fun should_only_sniff(profile: Profile) {
            BDDMockito.then(mockProfileRepo).should().cacheProfile(profile)
            BDDMockito.verifyNoMoreInteractions(mockProfileRepo)
        }

        fun should_sniff_nothing() {
            BDDMockito.verifyZeroInteractions(mockProfileRepo)
        }

        infix fun start(func: ScenarioMaker.() -> Unit) = buildScenario().given(func)
        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
