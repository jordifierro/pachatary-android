package com.pachatary.presentation.scene.edition

import com.pachatary.data.DummyResultError
import com.pachatary.data.DummyScene
import com.pachatary.data.DummySceneResultSuccess
import com.pachatary.data.common.Result
import com.pachatary.data.common.ResultInProgress
import com.pachatary.data.scene.Scene
import com.pachatary.data.scene.SceneRepository
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class EditScenePresenterTest {

    @Test
    fun test_empty_title() {
        given {
            an_experience_and_scene_id("7", "12")
            a_repo_that_on_get_scene(forExperienceId = "7", forSceneId = "12",
                                     returns = DummySceneResultSuccess("9"))
            a_title("")
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_scene(DummyScene("9"))
            should_show_title_error()
        }
    }

    @Test
    fun test_empty_description() {
        given {
            an_experience_and_scene_id("7", "12")
            a_repo_that_on_get_scene(forExperienceId = "7", forSceneId = "12",
                    returns = DummySceneResultSuccess("9"))
            a_title("title")
            a_description("")
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_scene(DummyScene("9"))
            should_show_description_error()
        }
    }

    @Test
    fun test_empty_location() {
        given {
            an_experience_and_scene_id("7", "12")
            a_repo_that_on_get_scene(forExperienceId = "7", forSceneId = "12",
                    returns = DummySceneResultSuccess("9"))
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_latitude_and_longitude(null, null)
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_scene(DummyScene("9"))
            should_show_location_error()
        }
    }

    @Test
    fun test_inprogress() {
        given {
            an_experience_and_scene_id("7", "12")
            a_repo_that_on_get_scene(forExperienceId = "7", forSceneId = "12",
                    returns = DummySceneResultSuccess("9", "5"))
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_latitude_and_longitude(2.0, -3.5)
            a_repo_that(forParams = Scene("9", "title", "desc", null, 2.0, -3.5, "5"),
                    returns = ResultInProgress())
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_scene(DummyScene("9", "5"))
            should_show_loader()
            should_disable_button()
        }
    }

    @Test
    fun test_error() {
        given {
            an_experience_and_scene_id("7", "12")
            a_repo_that_on_get_scene(forExperienceId = "7", forSceneId = "12",
                    returns = DummySceneResultSuccess("9", "5"))
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_latitude_and_longitude(2.0, -3.5)
            a_repo_that(forParams = Scene("9", "title", "desc", null, 2.0, -3.5, "5"),
                    returns = DummyResultError())
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_scene(DummyScene("9", "5"))
            should_hide_loader()
            should_enable_button()
            should_show_error()
        }
    }

    @Test
    fun test_success_without_picture() {
        given {
            an_experience_and_scene_id("7", "12")
            a_repo_that_on_get_scene(forExperienceId = "7", forSceneId = "12",
                    returns = DummySceneResultSuccess("9", "5"))
            a_title("title")
            a_description("desc")
            a_picture(null)
            a_latitude_and_longitude(2.0, -3.5)
            a_repo_that(forParams = Scene("9", "title", "desc", null, 2.0, -3.5, "5"),
                    returns = DummySceneResultSuccess("4"))
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_scene(DummyScene("9", "5"))
            should_hide_loader()
            should_finish_view()
        }
    }

    @Test
    fun test_success_with_picture() {
        given {
            an_experience_and_scene_id("7", "12")
            a_repo_that_on_get_scene(forExperienceId = "7", forSceneId = "12",
                    returns = DummySceneResultSuccess("9", "5"))
            a_title("title")
            a_description("desc")
            a_picture("pic")
            a_latitude_and_longitude(2.0, -3.5)
            a_repo_that(forParams = Scene("9", "title", "desc", null, 2.0, -3.5, "5"),
                    returns = DummySceneResultSuccess("4"))
        } whenn {
            create_presenter()
            update_click()
        } then {
            should_show_scene(DummyScene("9", "5"))
            should_hide_loader()
            should_call_upload_picture("9", "pic")
            should_finish_view()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: EditScenePresenter
        @Mock private lateinit var mockView: EditSceneView
        @Mock private lateinit var mockRepository: SceneRepository

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            presenter = EditScenePresenter(mockRepository, Schedulers.trampoline())

            return this
        }

        fun an_experience_and_scene_id(experienceId: String, sceneId: String) {
            presenter.setViewExperienceIdAndSceneId(mockView, experienceId, sceneId)
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

        fun a_repo_that_on_get_scene(forExperienceId: String, forSceneId: String,
                                     returns: Result<Scene>) {
            BDDMockito.given(mockRepository.sceneFlowable(forExperienceId, forSceneId))
                    .willReturn(Flowable.just(returns))
        }

        fun a_repo_that(forParams: Scene, returns: Result<Scene>) {
            BDDMockito.given(mockRepository.editScene(forParams))
                    .willReturn(Flowable.just(returns))
        }

        fun create_presenter() {
            presenter.create()
        }

        fun update_click() {
            presenter.onUpdateButtonClick()
        }

        fun should_show_scene(scene: Scene) {
            BDDMockito.then(mockView).should().showScene(scene)
        }

        fun should_show_title_error() {
            BDDMockito.then(mockView).should().showTitleError()
        }

        fun should_show_description_error() {
            BDDMockito.then(mockView).should().showDescriptionError()
        }

        fun should_show_location_error() {
            BDDMockito.then(mockView).should().showLocationError()
        }

        fun should_show_loader() {
            BDDMockito.then(mockView).should().showLoader()
        }

        fun should_disable_button() {
            BDDMockito.then(mockView).should().disableUpdateButton()
        }

        fun should_hide_loader() {
            BDDMockito.then(mockView).should().hideLoader()
        }

        fun should_enable_button() {
            BDDMockito.then(mockView).should().enableUpdateButton()
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
