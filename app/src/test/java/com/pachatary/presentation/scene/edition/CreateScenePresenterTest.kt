package com.pachatary.presentation.scene.edition

import com.pachatary.data.common.ResultSuccess
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
import org.mockito.MockitoAnnotations

class CreateScenePresenterTest {

    @Test
    fun on_create_navigates_to_edit_title_and_description() {
        given {
            nothing()
        } whenn {
            presenter_is_created()
        } then {
            presenter_should_navigate_to_edit_title_and_description()
        }
    }

    @Test
    fun on_title_and_description_edited_saves_them_and_navigates_to_select_location() {
        given {
            a_title()
            a_description()
        } whenn {
            title_and_description_are_edited()
        } then {
            title_should_be_saved()
            description_should_be_saved()
            presenter_should_navigate_to_select_location_initially_unknown()
        }
    }

    @Test
    fun on_last_location_found_it_sets_an_initial_location_when_select() {
        given {
            a_title()
            a_description()
            a_person_latitude()
            a_person_longitude()
        } whenn {
            last_location_found()
            title_and_description_are_edited()
        } then {
            title_should_be_saved()
            description_should_be_saved()
            presenter_should_navigate_to_select_location_with_initial_aproximation()
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
    fun on_location_selected_should_navigate_to_pick_image() {
        given {
            a_title()
            a_description()
            a_latitude()
            a_longitude()
        } whenn {
            title_and_description_are_edited()
            location_is_selected()
        } then {
            presenter_should_navigate_to_select_image()
        }
    }

    @Test
    fun on_location_selection_canceled_should_navigate_to_edit_title() {
        given {
            a_title()
            a_description()
            nothing()
        } whenn {
            title_and_description_are_edited()
            select_location_canceled()
        } then {
            presenter_should_navigate_to_edit_title_and_description_with_previous_title_and_description()
        }
    }

    @Test
    fun on_image_picked_presenter_should_create_scene_and_upload_picture() {
        given {
            a_title()
            a_description()
            a_latitude()
            a_longitude()
            an_scene_repository_that_returns_created_scene()
            an_image()
        } whenn {
            title_and_description_are_edited()
            location_is_selected()
            image_is_selected()
        } then {
            presenter_should_create_scene()
            presenter_should_upload_that_selected_image_with_created_scene_id()
            presenter_should_finish_view()
        }
    }

    @Test
    fun on_pick_image_canceled_should_navigate_to_select_location() {
        given {
            a_latitude()
            a_longitude()
        } whenn {
            location_is_selected()
            select_image_is_canceled()
        } then {
            presenter_should_navigate_to_select_location_with_previous_location()
        }
    }

    private fun given(func: ScenarioMaker.() -> Unit) = ScenarioMaker().given(func)

    class ScenarioMaker {
        private lateinit var presenter: CreateScenePresenter
        @Mock private lateinit var mockView: CreateSceneView
        @Mock lateinit var mockRepository: SceneRepository
        val experienceId = "4"
        var title = ""
        var description = ""
        var latitude = 0.0
        var longitude = 0.0
        var personLastKnowLatitude = 0.0
        var personLastKnowLongitude = 0.0
        var image = ""
        var sentCreatedScene: Scene? = null
        var receivedCreatedScene: Scene? = null

        fun buildScenario(): ScenarioMaker {
            MockitoAnnotations.initMocks(this)
            val testSchedulerProvider = SchedulerProvider(Schedulers.trampoline(), Schedulers.trampoline())
            presenter = CreateScenePresenter(mockRepository, testSchedulerProvider)
            presenter.setView(mockView, experienceId)

            return this
        }

        fun nothing() {}

        fun a_title() {
            title = "Some title"
        }

        fun a_description() {
            description = "Some description"
        }

        fun a_latitude() {
            latitude = 1.4
        }

        fun a_longitude() {
            longitude = -0.3
        }

        fun a_person_latitude() {
            personLastKnowLatitude = 1.7
        }

        fun a_person_longitude() {
            personLastKnowLongitude = -4.3
        }

        fun an_image() {
            image = "image_uri_string"
        }

        fun a_created_scene() {
            receivedCreatedScene = Scene(id = "1", title = title, description = description,
                    latitude = latitude, longitude = longitude,
                    experienceId = experienceId, picture = null)
        }

        fun an_scene_repository_that_returns_created_scene() {
            sentCreatedScene = Scene(id = "", title = title, description = description,
                                     latitude = latitude, longitude = longitude,
                                     experienceId = experienceId, picture = null)
            a_created_scene()

            BDDMockito.given(mockRepository.createScene(scene = sentCreatedScene!!))
                    .willReturn(Flowable.just(ResultSuccess(receivedCreatedScene!!)))
        }

        fun presenter_is_created() {
            presenter.create()
        }

        fun title_and_description_are_edited() {
            presenter.onTitleAndDescriptionEdited(title = title, description = description)
        }

        fun title_and_description_edition_canceled() {
            presenter.onEditTitleAndDescriptionCanceled()
        }

        fun last_location_found() {
            presenter.onLastLocationFound(personLastKnowLatitude, personLastKnowLongitude)
        }

        fun location_is_selected() {
            presenter.onLocationSelected(latitude, longitude)
        }

        fun select_location_canceled() {
            presenter.onSelectLocationCanceled()
        }

        fun image_is_selected() {
            presenter.onSelectImageSuccess(image)
        }

        fun select_image_is_canceled() {
            presenter.onSelectImageCanceled()
        }

        fun presenter_should_navigate_to_edit_title_and_description() {
            BDDMockito.then(mockView).should().navigateToEditTitleAndDescription()
        }

        fun presenter_should_navigate_to_edit_title_and_description_with_previous_title_and_description() {
            BDDMockito.then(mockView).should().navigateToEditTitleAndDescription(title, description)
        }

        fun title_should_be_saved() {
            Assert.assertEquals(title, presenter.title)
        }

        fun description_should_be_saved() {
            Assert.assertEquals(description, presenter.description)
        }

        fun presenter_should_navigate_to_select_location_initially_unknown() {
            BDDMockito.then(mockView).should().navigateToSelectLocation(personLastKnowLatitude, personLastKnowLongitude,
                                                                        SelectLocationPresenter.LocationType.UNKNWON)
        }

        fun presenter_should_navigate_to_select_location_with_initial_aproximation() {
            BDDMockito.then(mockView).should().navigateToSelectLocation(personLastKnowLatitude, personLastKnowLongitude,
                                                                        SelectLocationPresenter.LocationType.APROX)
        }

        fun presenter_should_navigate_to_select_location_with_previous_location() {
            BDDMockito.then(mockView).should().navigateToSelectLocation(latitude, longitude,
                    SelectLocationPresenter.LocationType.APROX)
        }

        fun presenter_should_finish_view() {
            BDDMockito.then(mockView).should().finish()
        }

        fun presenter_should_create_scene() {
            BDDMockito.then(mockRepository).should().createScene(sentCreatedScene!!)
        }

        fun presenter_should_save_received_scene() {
            Assert.assertEquals(receivedCreatedScene, presenter.createdScene)
        }

        fun presenter_should_navigate_to_select_image() {
            BDDMockito.then(mockView).should().navigateToSelectImage()
        }

        fun presenter_should_upload_that_selected_image_with_created_scene_id() {
            BDDMockito.then(mockRepository).should()
                    .uploadScenePicture(receivedCreatedScene!!.id, image)
        }

        infix fun given(func: ScenarioMaker.() -> Unit) = buildScenario().apply(func)
        infix fun whenn(func: ScenarioMaker.() -> Unit) = apply(func)
        infix fun then(func: ScenarioMaker.() -> Unit) = apply(func)
    }
}