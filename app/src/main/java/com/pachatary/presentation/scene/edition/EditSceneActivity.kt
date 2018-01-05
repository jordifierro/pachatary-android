package com.pachatary.presentation.scene.edition

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.edition.CropImageActivity
import com.pachatary.presentation.common.edition.PickImageActivity
import com.pachatary.presentation.common.edition.EditTitleAndDescriptionActivity
import com.pachatary.presentation.common.edition.SelectLocationActivity
import com.pachatary.presentation.common.edition.SelectLocationPresenter
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_create_scene.*
import javax.inject.Inject


class EditSceneActivity : AppCompatActivity(), EditSceneView {

    val EDIT_TITLE_AND_DESCRIPTION = 1
    val SELECT_LOCATION = 2
    val PICK_IMAGE = 3
    val CROP_IMAGE = UCrop.REQUEST_CROP

    @Inject
    lateinit var presenter: EditScenePresenter

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private val EXPERIENCE_ID = "experience_id"
        private val SCENE_ID = "scene_id"

        fun newIntent(context: Context, experienceId: String, sceneId: String): Intent {
            val intent = Intent(context, EditSceneActivity::class.java)
            intent.putExtra(EXPERIENCE_ID, experienceId)
            intent.putExtra(SCENE_ID, sceneId)
            return intent
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_scene)
        setSupportActionBar(toolbar)

        PachataryApplication.injector.inject(this)
        presenter.setView(this, intent.getStringExtra(EXPERIENCE_ID), intent.getStringExtra(SCENE_ID))
        registry.addObserver(presenter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EDIT_TITLE_AND_DESCRIPTION && resultCode == Activity.RESULT_OK)
            presenter.onTitleAndDescriptionEdited(
                    title = data!!.extras.getString(EditTitleAndDescriptionActivity.TITLE),
                    description = data.extras.getString(EditTitleAndDescriptionActivity.DESCRIPTION))
        else if (requestCode == EDIT_TITLE_AND_DESCRIPTION && resultCode == Activity.RESULT_CANCELED)
            presenter.onEditTitleAndDescriptionCanceled()
        else if (requestCode == SELECT_LOCATION && resultCode == Activity.RESULT_OK)
            presenter.onLocationSelected(latitude = data!!.extras.getDouble(SelectLocationActivity.LATITUDE),
                                         longitude = data.extras.getDouble(SelectLocationActivity.LONGITUDE))
        else if (requestCode == SELECT_LOCATION && resultCode == Activity.RESULT_CANCELED)
            presenter.onSelectLocationCanceled()
        else if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK)
            presenter.onImagePicked(PickImageActivity.getPickedImageUriStringFromResultData(data!!))
        else if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_CANCELED)
            presenter.onPickImageCanceled()
        else if (requestCode == CROP_IMAGE && resultCode == Activity.RESULT_OK)
            presenter.onImageCropped(CropImageActivity.getCroppedImageUriStringFromResultData(data!!))
        else if (requestCode == CROP_IMAGE && resultCode == Activity.RESULT_CANCELED)
            presenter.onCropImageCanceled()
    }

    override fun navigateToEditTitleAndDescription(initialTitle: String, initialDescription: String) {
        startActivityForResult(
                EditTitleAndDescriptionActivity.newIntent(this, initialTitle, initialDescription),
                EDIT_TITLE_AND_DESCRIPTION)
    }

    override fun navigateToSelectLocation(latitude: Double, longitude: Double,
                                          locationType: SelectLocationPresenter.LocationType) {
        startActivityForResult(
                SelectLocationActivity.newIntent(this, initialLatitude = latitude, initialLongitude = longitude,
                                                 initialType = locationType), SELECT_LOCATION)
    }

    override fun navigateToPickImage() {
        PickImageActivity.startActivityForResult(this, PICK_IMAGE)
    }

    override fun navigateToCropImage(selectedImageUriString: String) {
        CropImageActivity.startActivityForResult(this, selectedImageUriString)
    }

    override fun askUserToEditPicture() {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        else builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_title_scene_edited)
                .setMessage(R.string.dialog_question_edit_scene_picture)
                .setPositiveButton(android.R.string.yes,
                        { _, _ -> presenter.onAskUserEditPictureResponse(userWantsToEditPicture = true) })
                .setNegativeButton(android.R.string.no,
                        { _, _ -> presenter.onAskUserEditPictureResponse(userWantsToEditPicture = false) })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    override fun getLifecycle(): LifecycleRegistry = registry
}
