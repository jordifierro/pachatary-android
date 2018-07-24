package com.pachatary.data.profile

import com.pachatary.data.DummyProfile
import com.pachatary.data.DummyProfileResult
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultInProgress
import io.reactivex.Flowable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mockito.mock

class ProfileRepositoryTest {

    @Test
    fun test_cached_profile() {
        given {
            cached_profiles(listOf(DummyProfile("a"), DummyProfile("b")))
        } whenn {
            get_profile("b")
        } then {
            should_return(DummyProfileResult("b"))
        }
    }

    @Test
    fun test_cached_self_profile() {
        given {
            cached_profiles(listOf(DummyProfile("a", isMe = true), DummyProfile("b")))
        } whenn {
            self_profile()
        } then {
            should_return(DummyProfileResult("a", isMe = true))
        }
    }

    @Test
    fun test_not_cached_profile() {
        given {
            cached_profiles(listOf(DummyProfile("a"), DummyProfile("b")))
            an_api_repo_that_returns("c", ResultInProgress(), DummyProfileResult("c"))
        } whenn {
            get_profile("c")
        } then {
            should_return(ResultInProgress(), DummyProfileResult("c"))
            should_call_profile("c")
        }
    }

    @Test
    fun test_not_cached_self_profile() {
        given {
            cached_profiles(listOf(DummyProfile("a"), DummyProfile("b")))
            an_api_repo_that_returns("self",
                    ResultInProgress(), DummyProfileResult("c", isMe = true))
        } whenn {
            self_profile()
        } then {
            should_return(ResultInProgress(), DummyProfileResult("c", isMe = true))
            should_call_self_profile()
        }
    }

    @Test
    fun test_edit_profile() {
        given {
            cached_profiles(listOf(DummyProfile("a"), DummyProfile("b", bio = "old", isMe = true)))
            an_api_repo_that_returns_on_edit("new bio",
                    ResultInProgress(), DummyProfileResult("b", bio = "new bio", isMe = true))
        } whenn {
            edit_profile("new bio")
            self_profile()
        } then {
            should_call_edit_profile("new bio")
            edit_should_return(ResultInProgress(),
                    DummyProfileResult("b", bio = "new bio", isMe = true))
            should_return(DummyProfileResult("b", bio = "new bio", isMe = true))
        }
    }

    @Test
    fun test_upload_profile_picture() {
        given {
            cached_profiles(listOf(DummyProfile("a"), DummyProfile("b", bio = "old", isMe = true)))
            an_api_repo_that_returns_on_upload_picture("image_path",
                    ResultInProgress(), DummyProfileResult("b", bio = "new bio", isMe = true))
        } whenn {
            upload_picture("image_path")
            self_profile()
        } then {
            should_call_upload_profile_picture("image_path")
            should_return(DummyProfileResult("b", bio = "new bio", isMe = true))
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {

        private val mockApiRepo = mock(ProfileApiRepository::class.java)
        private val repository = ProfileRepository(mockApiRepo)
        private val testSubscriber = TestSubscriber.create<Result<Profile>>()
        private val testEditSubscriber = TestSubscriber.create<Result<Profile>>()

        fun cached_profiles(profiles: List<Profile>) {
            for (profile in profiles)
                repository.cacheProfile(profile)
        }

        fun an_api_repo_that_returns(username: String, vararg results: Result<Profile>) {
            if (username == "self")
                BDDMockito.given(mockApiRepo.selfProfile()).willReturn(Flowable.fromArray(*results))
            else BDDMockito.given(mockApiRepo.profile(username))
                    .willReturn(Flowable.fromArray(*results))
        }

        fun an_api_repo_that_returns_on_edit(bio: String, vararg results: Result<Profile>) {
            BDDMockito.given(mockApiRepo.editProfile(bio)).willReturn(Flowable.fromArray(*results))
        }

        fun an_api_repo_that_returns_on_upload_picture(image: String, vararg results: Result<Profile>) {
            BDDMockito.given(mockApiRepo.uploadProfilePicture(image))
                    .willReturn(Flowable.fromArray(*results))
        }

        fun get_profile(username: String) {
            repository.profile(username).subscribe(testSubscriber)
        }

        fun self_profile() {
            repository.selfProfile().subscribe(testSubscriber)
        }

        fun edit_profile(bio: String) {
            repository.editProfile(bio).subscribe(testEditSubscriber)
        }

        fun upload_picture(image: String) {
            repository.uploadProfilePicture(image)
        }

        fun should_return(vararg results: Result<Profile>) {
            testSubscriber.awaitCount(results.size)
            testSubscriber.onComplete()
            testSubscriber.assertResult(*results)
        }

        fun edit_should_return(vararg results: Result<Profile>) {
            testEditSubscriber.awaitCount(results.size)
            testEditSubscriber.assertResult(*results)
        }

        fun should_call_profile(username: String) {
            BDDMockito.then(mockApiRepo).should().profile(username)
        }

        fun should_call_self_profile() {
            BDDMockito.then(mockApiRepo).should().selfProfile()
        }

        fun should_call_edit_profile(bio: String) {
            BDDMockito.then(mockApiRepo).should().editProfile(bio)
        }

        fun should_call_upload_profile_picture(image: String) {
            BDDMockito.then(mockApiRepo).should().uploadProfilePicture(image)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
