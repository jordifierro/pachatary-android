package com.abidria.presentation.scene.edition

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.abidria.R
import com.abidria.presentation.common.AbidriaApplication
import com.abidria.presentation.common.view.edition.CropImageActivity
import com.abidria.presentation.common.view.edition.PickImageActivity
import com.abidria.presentation.common.view.edition.EditTitleAndDescriptionActivity
import com.abidria.presentation.common.view.edition.SelectLocationActivity
import com.abidria.presentation.common.view.edition.SelectLocationPresenter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_create_scene.*
import javax.inject.Inject


class CreateSceneActivity : AppCompatActivity(), CreateSceneView {

    val EDIT_TITLE_AND_DESCRIPTION = 1
    val SELECT_LOCATION = 2
    val PICK_IMAGE = 3
    val CROP_IMAGE = UCrop.REQUEST_CROP

    @Inject
    lateinit var presenter: CreateScenePresenter

    lateinit var fusedLocationClient: FusedLocationProviderClient

    val registry: LifecycleRegistry = LifecycleRegistry(this)

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

        if (checkLocationPermission()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) presenter.onLastLocationFound(location.latitude, location.longitude)
            }
        }

        AbidriaApplication.injector.inject(this)
        presenter.setView(this, intent.getStringExtra(EXPERIENCE_ID))
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

    override fun getLifecycle(): LifecycleRegistry = registry

    private fun checkLocationPermission() =
            this.checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") ==
                PackageManager.PERMISSION_GRANTED
}
