package com.pachatary.presentation.scene.edition

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.edition.CropImageActivity
import com.pachatary.presentation.common.edition.PickImageActivity
import com.pachatary.presentation.common.edition.EditTitleAndDescriptionActivity
import com.pachatary.presentation.common.edition.SelectLocationActivity
import com.pachatary.presentation.common.edition.SelectLocationPresenter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pachatary.presentation.common.location.LocationUtils
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_create_scene.*
import javax.inject.Inject


class CreateSceneActivity : AppCompatActivity(), CreateSceneView {

    private val EDIT_TITLE_AND_DESCRIPTION = 1
    private val SELECT_LOCATION = 2
    private val PICK_IMAGE = 3
    private val CROP_IMAGE = UCrop.REQUEST_CROP

    @Inject
    lateinit var presenter: CreateScenePresenter

    private val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private val EXPERIENCE_ID = "experienceId"

        fun newIntent(context: Context, experienceId: String): Intent {
            val intent = Intent(context, CreateSceneActivity::class.java)
            intent.putExtra(EXPERIENCE_ID, experienceId)
            return intent
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_scene)
        setSupportActionBar(toolbar)

        LocationUtils.addListenerToLocation(this, { location: Location ->
            presenter.onLastLocationFound(location.latitude, location.longitude) })

        PachataryApplication.injector.inject(this)
        presenter.setView(this, intent.getStringExtra(EXPERIENCE_ID))
        registry.addObserver(presenter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EDIT_TITLE_AND_DESCRIPTION && resultCode == Activity.RESULT_OK)
            presenter.onTitleAndDescriptionEdited(
                title = data!!.extras.getString(EditTitleAndDescriptionActivity.TITLE),
                description = data.extras.getString(EditTitleAndDescriptionActivity.DESCRIPTION))
        else if (requestCode == EDIT_TITLE_AND_DESCRIPTION
                 && resultCode == Activity.RESULT_CANCELED)
            presenter.onEditTitleAndDescriptionCanceled()
        else if (requestCode == SELECT_LOCATION && resultCode == Activity.RESULT_OK)
            presenter.onLocationSelected(
                    latitude = data!!.extras.getDouble(SelectLocationActivity.LATITUDE),
                    longitude = data.extras.getDouble(SelectLocationActivity.LONGITUDE))
        else if (requestCode == SELECT_LOCATION && resultCode == Activity.RESULT_CANCELED)
            presenter.onSelectLocationCanceled()
        else if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK)
            presenter.onImagePicked(PickImageActivity.getPickedImageUriStringFromResultData(data!!))
        else if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_CANCELED)
            presenter.onPickImageCanceled()
        else if (requestCode == CROP_IMAGE && resultCode == Activity.RESULT_OK)
            presenter.onImageCropped(
                    CropImageActivity.getCroppedImageUriStringFromResultData(data!!))
        else if (requestCode == CROP_IMAGE && resultCode == Activity.RESULT_CANCELED)
            presenter.onCropImageCanceled()
    }

    override fun navigateToEditTitleAndDescription(initialTitle: String,
                                                   initialDescription: String) {
        startActivityForResult(
                EditTitleAndDescriptionActivity.newIntent(this, initialTitle, initialDescription),
                EDIT_TITLE_AND_DESCRIPTION)
    }

    override fun navigateToSelectLocation(latitude: Double, longitude: Double,
                                          locationType: SelectLocationPresenter.LocationType) {
        startActivityForResult(
                SelectLocationActivity.newIntent(this, initialLatitude = latitude,
                        initialLongitude = longitude, initialType = locationType), SELECT_LOCATION)
    }

    override fun navigateToPickImage() {
        PickImageActivity.startActivityForResult(this, PICK_IMAGE)
    }

    override fun navigateToCropImage(selectedImageUriString: String) {
        CropImageActivity.startActivityForResult(this, selectedImageUriString)
    }

    override fun getLifecycle(): LifecycleRegistry = registry
}
