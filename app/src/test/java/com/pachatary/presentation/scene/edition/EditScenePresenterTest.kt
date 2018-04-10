package com.pachatary.presentation.scene.edition

import com.pachatary.data.common.Result
import com.pachatary.data.scene.Scene
import com.pachatary.data.scene.SceneRepository
import com.pachatary.presentation.common.injection.scheduler.SchedulerProvider
import com.pachatary.presentation.common.edition.SelectLocationPresenter
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class EditScenePresenterTest {

    @Test
    fun on_create_navigates_requests_scene() {
        given {
            an_scene()
            an_scene_repository_that_returns_scene()
        } whenn {
            presenter_is_created()
        } then {
            presenter_should_request_scene_to_the_repo()
        }
    }

    @Test
    fun on_scene_received_navigates_to_edit_title_and_description() {
        given {
            an_scene()
            an_scene_repository_that_returns_scene()
        } whenn {
            presenter_is_created()
        } then {
            presenter_should_navigate_to_edit_title_and_description_with_initial_scene_values()
        }
    }

    @Test
    fun on_title_and_description_edited_saves_them_and_navigates_to_select_location() {
        given {
            a_title_introduced_by_user()
            a_description_introduced_by_user()
            an_scene()
            an_scene_repository_that_returns_scene()
        } whenn {
            presenter_is_created()
            title_and_description_are_edited()
        } then {
            title_should_be_saved()
            description_should_be_saved()
            presenter_should_navigate_to_select_location_with_scene_initial_position()
        }
    }

    @Test
    fun on_edit_title_and_description_canceled_presenter_should_finsh_view() {
        given {
            nothing()
        } whenn {
            title_and_description_edition_canceled()
        } then {
            presenter_should_finish_view()
        }
    }

    @Test
    fun on_location_selected_should_edit_scene_and_ask_user_to_edit_picture() {
        given {
            a_title_introduced_by_user()
            a_description_introduced_by_user()
            a_latitude_introduced_by_user()
            a_longitude_introduced_by_user()
            an_scene()
            an_scene_repository_that_returns_scene()
            an_scene_repository_that_returns_edited_scene()
        } whenn {
            presenter_is_created()
            title_and_description_are_edited()
            location_is_selected()
        } then {
            presenter_should_edit_scene_on_repo()
            presenter_should_update_received_scene()
            presenter_should_ask_user_if_wants_to_change_picture()
        }
    }

    @Test
    fun on_location_selection_canceled_should_navigate_to_edit_title() {
        given {
            an_scene()
            an_scene_repository_that_returns_scene()
        } whenn {
            presenter_is_created()
            select_location_canceled()
        } then {
            presenter_should_navigate_to_edit_title_and_description_with_initial_scene_values_twice()
        }
    }

    @Test
    fun on_image_picked_presenter_should_navigate_to_crop_image() {
        given {
            an_image_introduced_by_user()
        } whenn {
            image_is_picked()
        } then {
            presenter_should_navigate_to_crop_that_image()
        }
    }

    @Test
    fun on_pick_image_canceled_presenter_should_finsh_view() {
        given {
            nothing()
        } whenn {
            pick_image_is_canceled()
        } then {
            presenter_should_finish_view()
        }
    }

    @Test
    fun on_image_cropped_presenter_should_upload_image_and_finish_view() {
        given {
            a_title_introduced_by_user()
            a_description_introduced_by_user()
            a_latitude_introduced_by_user()
            a_longitude_introduced_by_user()
            an_scene()
            an_scene_repository_that_returns_scene()
            an_scene_repository_that_returns_edited_scene()
            an_image_cropped_by_user()
        } whenn {
            presenter_is_created()
            title_and_description_are_edited()
            location_is_selected()
            image_is_cropped()
        } then {
            presenter_should_upload_that_cropped_image_with_created_scene_id()
            presenter_should_finish_view()
        }
    }

    @Test
    fun on_crop_image_canceled_presenter_should_navigate_to_pick_image() {
        given {
            nothing()
        } whenn {
            crop_image_canceled()
        } then {
            presenter_should_navigate_to_pick_image()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: EditScenePresenter
        @Mock private lateinit var mockView: EditSceneView
        @Mock lateinit var mockRepository: SceneRepository
        val experienceId = "4"
        val sceneId = "3"
        lateinit var scene: Scene
        var userTitle = ""
        var userDescription = ""
        var userLatitude = 0.0
        var userLongitude = 0.0
        var image = ""
        var croppedImage = ""
        var receivedEditedScene: Scene? = null

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = EditScenePresenter(mockRepository, testSchedulerProvider)
            presenter.setView(mockView, experienceId, sceneId)

            return this
        }

        fun nothing() {}

        fun a_title_introduced_by_user() {
            userTitle = "Some title"
        }

        fun a_description_introduced_by_user() {
            userDescription = "Some description"
        }

        fun a_latitude_introduced_by_user() {
            userLatitude = 1.4
        }

        fun a_longitude_introduced_by_user() {
            userLongitude = -0.3
        }

        fun an_image_introduced_by_user() {
            image = "image_uri_string"
        }

        fun an_image_cropped_by_user() {
            croppedImage = "another_iamge_uri_string"
        }

        fun an_scene() {
            scene = Scene(id = sceneId, title = "Title", description = "description",
                    latitude = 1.0, longitude = 2.0, experienceId = experienceId, picture = null)
        }

        fun an_edited_scene() {
            receivedEditedScene = Scene(id = "1", title = userTitle, description = userDescription,
                    latitude = userLatitude, longitude = userLongitude,
                    experienceId = experienceId, picture = null)
        }

        fun an_scene_repository_that_returns_edited_scene() {
            val sentEditedScene = Scene(id = sceneId, title = userTitle, description = userDescription,
                                     latitude = userLatitude, longitude = userLongitude,
                                     experienceId = experienceId, picture = null)
            an_edited_scene()

            BDDMockito.given(mockRepository.editScene(scene = sentEditedScene))
                    .willReturn(Flowable.just(Result<Scene>(receivedEditedScene!!)))
        }

        fun an_scene_repository_that_returns_scene() {
            BDDMockito.given(mockRepository.sceneFlowable(experienceId, sceneId))
                    .willReturn(Flowable.just(Result(scene)))
        }

        fun presenter_is_created() {
            presenter.create()
        }

        fun title_and_description_are_edited() {
            presenter.onTitleAndDescriptionEdited(title = userTitle, description = userDescription)
        }

        fun title_and_description_edition_canceled() {
            presenter.onEditTitleAndDescriptionCanceled()
        }

        fun location_is_selected() {
            presenter.onLocationSelected(userLatitude, userLongitude)
        }

        fun select_location_canceled() {
            presenter.onSelectLocationCanceled()
        }

        fun image_is_picked() {
            presenter.onImagePicked(image)
        }

        fun pick_image_is_canceled() {
            presenter.onPickImageCanceled()
        }

        fun image_is_cropped() {
            presenter.onImageCropped(croppedImage)
        }

        fun crop_image_canceled() {
            presenter.onCropImageCanceled()
        }


        fun presenter_should_edit_scene_on_repo() {
            val editedScene = Scene(id = sceneId, title = userTitle, description = userDescription, picture = null,
                                    latitude = userLatitude, longitude = userLongitude, experienceId = experienceId)
            BDDMockito.then(mockRepository).should().editScene(editedScene)
        }

        fun presenter_should_update_received_scene() {
            Assert.assertEquals(receivedEditedScene, presenter.scene)
        }

        fun presenter_should_ask_user_if_wants_to_change_picture() {
            BDDMockito.then(mockView).should().askUserToEditPicture()
        }

        fun title_should_be_saved() {
            Assert.assertEquals(userTitle, presenter.scene.title)
        }

        fun description_should_be_saved() {
            Assert.assertEquals(userDescription, presenter.scene.description)
        }

        fun presenter_should_navigate_to_select_location_with_scene_initial_position() {
            BDDMockito.then(mockView).should().navigateToSelectLocation(scene.latitude, scene.longitude,
                                                                        SelectLocationPresenter.LocationType.SPECIFIC)
        }

        fun presenter_should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        fun presenter_should_navigate_to_pick_image() {
            BDDMockito.then(mockView).should().navigateToPickImage()
        }

        fun presenter_should_navigate_to_crop_that_image() {
            BDDMockito.then(mockView).should().navigateToCropImage(image)
        }

        fun presenter_should_upload_that_cropped_image_with_created_scene_id() {
            BDDMockito.then(mockRepository).should().uploadScenePicture(receivedEditedScene!!.id, croppedImage)
        }

        fun presenter_should_request_scene_to_the_repo() {
            BDDMockito.then(mockRepository).should().sceneFlowable(experienceId, sceneId)
        }

        fun presenter_should_navigate_to_edit_title_and_description_with_initial_scene_values() {
            BDDMockito.then(mockView).should().navigateToEditTitleAndDescription(scene.title, scene.description)
        }

        fun presenter_should_navigate_to_edit_title_and_description_with_initial_scene_values_twice() {
            BDDMockito.then(mockView).should(Mockito.times(2))
                    .navigateToEditTitleAndDescription(scene.title, scene.description)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}