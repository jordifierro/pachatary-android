package com.pachatary.presentation.scene.edition

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.edition.PickAndCropImageActivity
import com.pachatary.presentation.common.edition.SelectLocationActivity
import com.pachatary.presentation.common.edition.SelectLocationPresenter
import com.pachatary.presentation.common.location.LocationUtils
import com.pachatary.presentation.common.view.SnackbarUtils
import com.pachatary.presentation.common.view.ToolbarUtils
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import javax.inject.Inject


class CreateSceneActivity : AppCompatActivity(), CreateSceneView {

    @Inject
    lateinit var presenter: CreateScenePresenter

    private lateinit var rootView: CoordinatorLayout
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var editPictureLayout: RelativeLayout
    private lateinit var editLocationLayout: RelativeLayout
    private lateinit var locationIcon: ImageView
    private lateinit var pictureImageView: ImageView
    private lateinit var createButton: Button
    private lateinit var loaderView: ProgressBar
    private var selectedImage: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private const val SELECT_LOCATION = 1
        private const val SELECT_IMAGE = 2

        private const val EXPERIENCE_ID = "experience_id"

        fun newIntent(context: Context, experienceId: String): Intent {
            val intent = Intent(context, CreateSceneActivity::class.java)
            intent.putExtra(EXPERIENCE_ID, experienceId)
            return intent
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene_edition)

        ToolbarUtils.setUp(this, getString(R.string.activity_create_scene_title), true)

        LocationUtils.addListenerToLocation(this) { location: Location? ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude
            }
        }

        rootView = findViewById(R.id.root)
        titleEditText = findViewById(R.id.scene_edition_title_edittext)
        descriptionEditText = findViewById(R.id.scene_edition_description_edittext)
        pictureImageView = findViewById(R.id.scene_edition_picture)
        editPictureLayout = findViewById(R.id.scene_edition_picture_layout)
        editPictureLayout.setOnClickListener { navigateToSelectImage() }
        editLocationLayout = findViewById(R.id.scene_edition_location_layout)
        editLocationLayout.setOnClickListener { navigateToSelectLocation() }
        locationIcon = findViewById(R.id.scene_edition_location_icon)
        createButton = findViewById(R.id.scene_edition_button)
        createButton.setOnClickListener { presenter.onCreateButtonClick() }
        loaderView = findViewById(R.id.scene_edition_progressbar)

        PachataryApplication.injector.inject(this)
        presenter.setViewAndExperienceId(this, intent.getStringExtra(EXPERIENCE_ID))
        registry.addObserver(presenter)
    }

    override fun title() = titleEditText.text.toString()
    override fun description() = descriptionEditText.text.toString()
    override fun picture() = selectedImage
    override fun latitude() = latitude
    override fun longitude() = longitude

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_LOCATION && resultCode == Activity.RESULT_OK) {
            latitude = data!!.extras.getDouble(SelectLocationActivity.LATITUDE)
            longitude = data.extras.getDouble(SelectLocationActivity.LONGITUDE)
            locationIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
        }
        else if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImage = PickAndCropImageActivity.getImageUriFrom(data!!)
            val d = this.resources.displayMetrics.density
            Picasso.with(this)
                    .load(selectedImage)
                    .transform(RoundedCornersTransformation((23 * d).toInt(), 0))
                    .into(pictureImageView)
        }
    }

    private fun navigateToSelectLocation() {
        if (latitude != null && longitude != null)
            startActivityForResult(SelectLocationActivity.newIntent(this, latitude!!, longitude!!,
                                   SelectLocationPresenter.LocationType.APROX), SELECT_LOCATION)
        else if (currentLatitude != null)
            startActivityForResult(SelectLocationActivity.newIntent(this, currentLatitude!!,
                currentLongitude!!, SelectLocationPresenter.LocationType.APROX), SELECT_LOCATION)
        else startActivityForResult(SelectLocationActivity.newIntent(this), SELECT_LOCATION)
    }

    private fun navigateToSelectImage() {
        startActivityForResult(PickAndCropImageActivity.newIntent(this), SELECT_IMAGE)
    }

    override fun showTitleError() {
        SnackbarUtils.showError(rootView, this, getString(R.string.activity_scene_edition_title_error))
    }

    override fun showDescriptionError() {
        SnackbarUtils.showError(rootView, this,
                getString(R.string.activity_scene_edition_description_error))
    }

    override fun showPictureError() {
        SnackbarUtils.showError(rootView, this,
                getString(R.string.activity_scene_edition_picture_error))
    }

    override fun showLocationError() {
        SnackbarUtils.showError(rootView, this, getString(R.string.activity_scene_edition_location_error))
    }

    override fun enableCreateButton() {
        createButton.isEnabled = true
    }

    override fun disableCreateButton() {
        createButton.isEnabled = false
    }

    override fun showError() {
        SnackbarUtils.showError(rootView, this)
    }

    override fun showLoader() {
        loaderView.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        loaderView.visibility = View.INVISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun getLifecycle(): LifecycleRegistry = registry
}
