package com.pachatary.presentation.experience.edition

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.edition.PickAndCropImageActivity
import com.pachatary.presentation.common.view.SnackbarUtils
import com.pachatary.presentation.common.view.ToolbarUtils
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import javax.inject.Inject


class CreateExperienceActivity : AppCompatActivity(), CreateExperienceView {

    companion object {
        const val SELECT_IMAGE = 1

        fun newIntent(context: Context) = Intent(context, CreateExperienceActivity::class.java)
    }

    @Inject
    lateinit var presenter: CreateExperiencePresenter

    private lateinit var rootView: CoordinatorLayout
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var editPictureLayout: RelativeLayout
    private lateinit var pictureImageView: ImageView
    private lateinit var createButton: Button
    private lateinit var loaderView: ProgressBar
    private var selectedImage: String? = null

    private val registry: LifecycleRegistry = LifecycleRegistry(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experience_edition)

        ToolbarUtils.setUp(this, getString(R.string.activity_create_experience_title), true)

        rootView = findViewById(R.id.root)
        titleEditText = findViewById(R.id.experience_edition_title_edittext)
        descriptionEditText = findViewById(R.id.experience_edition_description_edittext)
        pictureImageView = findViewById(R.id.experience_edition_picture)
        editPictureLayout = findViewById(R.id.experience_edition_picture_layout)
        editPictureLayout.setOnClickListener { navigateToSelectImage() }
        createButton = findViewById(R.id.experience_edition_button)
        createButton.setOnClickListener { presenter.onCreateButtonClick() }
        loaderView = findViewById(R.id.experience_edition_progressbar)

        PachataryApplication.injector.inject(this)
        presenter.view = this
        registry.addObserver(presenter)
    }

    override fun title() = titleEditText.text.toString()
    override fun description() = descriptionEditText.text.toString()
    override fun picture() = selectedImage

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImage = PickAndCropImageActivity.getImageUriFrom(data!!)
            val d = this.resources.displayMetrics.density
            Picasso.with(this)
                    .load(selectedImage)
                    .transform(RoundedCornersTransformation((23 * d).toInt(), 0))
                    .into(pictureImageView)
        }
    }

    private fun navigateToSelectImage() {
        startActivityForResult(PickAndCropImageActivity.newIntent(this), SELECT_IMAGE)
    }

    override fun showTitleError() {
        SnackbarUtils.showError(rootView, this, getString(R.string.activity_experience_edition_title_error))
    }

    override fun showDescriptionError() {
        SnackbarUtils.showError(rootView, this,
                                getString(R.string.activity_experience_edition_description_error))
    }

    override fun showPictureError() {
        SnackbarUtils.showError(rootView, this,
                                getString(R.string.activity_experience_edition_picture_error))
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
