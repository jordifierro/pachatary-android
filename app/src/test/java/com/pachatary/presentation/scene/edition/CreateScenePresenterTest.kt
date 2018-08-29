package com.pachatary.presentation.scene.edition

import com.pachatary.data.DummyResultError
import com.pachatary.data.DummySceneResultSuccess
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultInProgress
import com.pachatary.data.scene.Scene
import com.pachatary.data.scene.SceneRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CreateScenePresenterTest {

    @Test
    fun test_empty_title() {
        given {
            an_experience_id("7")
            a_title("")
        } whenn {
            create_click()
        } then {
            should_show_title_error()
        }
    }

    @Test
    fun test_empty_description() {
        given {
            an_experience_id("7")
            a_title("title")
            a_description("")
        } whenn {
            create_click()
        } then {
            should_show_description_error()
        }
    }

    @Test
    fun test_empty_picture() {
        given {
            an_experience_id("7")
            a_title("title")
            a_description("desc")
            a_picture(null)
        } whenn {
            create_click()
        } then {
            should_show_picture_error()
        }
    }

    @Test
    fun test_empty_location() {
        given {
            an_experience_id("7")
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_latitude_and_longitude(null, null)
        } whenn {
            create_click()
        } then {
            should_show_location_error()
        }
    }

    @Test
    fun test_inprogress() {
        given {
            an_experience_id("7")
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_latitude_and_longitude(2.0, -3.5)
            a_repo_that(forParams = Scene("", "title", "desc", null, 2.0, -3.5, "7"),
                    returns = ResultInProgress())
        } whenn {
            create_click()
        } then {
            should_show_loader()
            should_disable_button()
        }
    }

    @Test
    fun test_error() {
        given {
            an_experience_id("7")
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_latitude_and_longitude(2.0, -3.5)
            a_repo_that(forParams = Scene("", "title", "desc", null, 2.0, -3.5, "7"),
                    returns = DummyResultError())
        } whenn {
            create_click()
        } then {
            should_hide_loader()
            should_enable_button()
            should_show_error()
        }
    }

    @Test
    fun test_success() {
        given {
            an_experience_id("7")
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_latitude_and_longitude(2.0, -3.5)
            a_repo_that(forParams = Scene("", "title", "desc", null, 2.0, -3.5, "7"),
                    returns = DummySceneResultSuccess("4"))
        } whenn {
            create_click()
        } then {
            should_hide_loader()
            should_call_upload_picture("4", "pic")
            should_finish_view()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: CreateScenePresenter
        @Mock private lateinit var mockView: CreateSceneView
        @Mock private lateinit var mockRepository: SceneRepository

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = CreateScenePresenter(mockRepository, Schedulers.trampoline())

            return this
        }

        fun an_experience_id(experienceId: String) {
            presenter.setViewAndExperienceId(mockView, experienceId)
        }

        fun a_title(title: String) {
            BDDMockito.given(mockView.title()).willReturn(title)
        }

        fun a_description(description: String) {
            BDDMockito.given(mockView.description()).willReturn(description)
        }

        fun a_picture(picture: String?) {
            BDDMockito.given(mockView.picture()).willReturn(picture)
        }

        fun a_latitude_and_longitude(latitude: Double?, longitude: Double?) {
            BDDMockito.given(mockView.latitude()).willReturn(latitude)
            BDDMockito.given(mockView.longitude()).willReturn(longitude)
        }

        fun a_repo_that(forParams: Scene, returns: Result<Scene>) {
            BDDMockito.given(mockRepository.createScene(forParams))
                    .willReturn(Flowable.just(returns))
        }

        fun create_click() {
            presenter.onCreateButtonClick()
        }

        fun should_show_title_error() {
            BDDMockito.then(mockView).should().showTitleError()
        }

        fun should_show_description_error() {
            BDDMockito.then(mockView).should().showDescriptionError()
        }

        fun should_show_picture_error() {
            BDDMockito.then(mockView).should().showPictureError()
        }

        fun should_show_location_error() {
            BDDMockito.then(mockView).should().showLocationError()
        }

        fun should_show_loader() {
            BDDMockito.then(mockView).should().showLoader()
        }

        fun should_disable_button() {
            BDDMockito.then(mockView).should().disableCreateButton()
        }

        fun should_hide_loader() {
            BDDMockito.then(mockView).should().hideLoader()
        }

        fun should_enable_button() {
            BDDMockito.then(mockView).should().enableCreateButton()
        }

        fun should_show_error() {
            BDDMockito.then(mockView).should().showError()
        }

        fun should_call_upload_picture(sceneId: String, picture: String) {
            BDDMockito.then(mockRepository).should().uploadScenePicture(sceneId, picture)
        }

        fun should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}
