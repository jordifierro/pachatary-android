package com.pachatary.presentation.scene.edition

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.pachatary.R
import com.pachatary.data.scene.Scene
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.edition.PickAndCropImageActivity
import com.pachatary.presentation.common.edition.SelectLocationActivity
import com.pachatary.presentation.common.edition.SelectLocationPresenter
import com.pachatary.presentation.common.view.PictureDeviceCompat
import com.pachatary.presentation.common.view.SnackbarUtils
import com.pachatary.presentation.common.view.ToolbarUtils
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import javax.inject.Inject

class EditSceneActivity : AppCompatActivity(), EditSceneView {

    companion object {
        private const val SELECT_LOCATION = 1
        private const val SELECT_IMAGE = 2

        private const val EXPERIENCE_ID = "experience_id"
        private const val SCENE_ID = "scene_id"

        fun newIntent(context: Context, experienceId: String, sceneId: String): Intent {
            val intent = Intent(context, EditSceneActivity::class.java)
            intent.putExtra(EXPERIENCE_ID, experienceId)
            intent.putExtra(SCENE_ID, sceneId)
            return intent
        }
    }

    @Inject
    lateinit var presenter: EditScenePresenter
    @Inject
    lateinit var pictureDeviceCompat: PictureDeviceCompat

    private lateinit var rootView: CoordinatorLayout
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var editPictureLayout: RelativeLayout
    private lateinit var editLocationLayout: RelativeLayout
    private lateinit var locationIcon: ImageView
    private lateinit var pictureImageView: ImageView
    private lateinit var updateButton: Button
    private lateinit var loaderView: ProgressBar
    private var selectedImage: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    private val registry: LifecycleRegistry = LifecycleRegistry(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scene_edition)

        ToolbarUtils.setUp(this, getString(R.string.activity_edit_experience_title), true)

        rootView = findViewById(R.id.root)
        titleEditText = findViewById(R.id.scene_edition_title_edittext)
        descriptionEditText = findViewById(R.id.scene_edition_description_edittext)
        pictureImageView = findViewById(R.id.scene_edition_picture)
        editPictureLayout = findViewById(R.id.scene_edition_picture_layout)
        editPictureLayout.setOnClickListener { navigateToSelectImage() }
        editLocationLayout = findViewById(R.id.scene_edition_location_layout)
        editLocationLayout.setOnClickListener { navigateToSelectLocation() }
        locationIcon = findViewById(R.id.scene_edition_location_icon)
        updateButton = findViewById(R.id.scene_edition_button)
        updateButton.setOnClickListener { presenter.onUpdateButtonClick() }
        updateButton.text = getString(R.string.activity_edit_scene_button)
        loaderView = findViewById(R.id.scene_edition_progressbar)

        PachataryApplication.injector.inject(this)
        presenter.setViewExperienceIdAndSceneId(this,
                intent.getStringExtra(EXPERIENCE_ID), intent.getStringExtra(SCENE_ID))
        registry.addObserver(presenter)
    }

    override fun showScene(scene: Scene) {
        titleEditText.setText(scene.title)
        descriptionEditText.setText(scene.description)
        if (scene.picture != null) {
            val d = this.resources.displayMetrics.density
            Picasso.with(this)
                    .load(pictureDeviceCompat.convert(scene.picture)?.halfScreenSizeUrl)
                    .transform(RoundedCornersTransformation((23 * d).toInt(), 0))
                    .into(pictureImageView)
        }
        latitude = scene.latitude
        longitude = scene.longitude
        locationIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
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

    override fun showLocationError() {
        SnackbarUtils.showError(rootView, this, getString(R.string.activity_scene_edition_location_error))
    }

    override fun enableUpdateButton() {
        updateButton.isEnabled = true
    }

    override fun disableUpdateButton() {
        updateButton.isEnabled = false
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

