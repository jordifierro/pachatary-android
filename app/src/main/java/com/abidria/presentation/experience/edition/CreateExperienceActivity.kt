package com.abidria.presentation.experience.edition

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.abidria.R
import com.abidria.presentation.common.AbidriaApplication
import com.abidria.presentation.common.view.edition.CropImageActivity
import com.abidria.presentation.common.view.edition.PickImageActivity
import com.abidria.presentation.common.view.edition.EditTitleAndDescriptionActivity
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_create_experience.*
import javax.inject.Inject


class CreateExperienceActivity : AppCompatActivity(), CreateExperienceView {

    val EDIT_TITLE_AND_DESCRIPTION = 1
    val PICK_IMAGE = 2
    val CROP_IMAGE = UCrop.REQUEST_CROP

    @Inject
    lateinit var presenter: CreateExperiencePresenter

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private val EXPERIENCE_ID = "experienceId"

        fun newIntent(context: Context) = Intent(context, CreateExperienceActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_experience)
        setSupportActionBar(toolbar)

        AbidriaApplication.injector.inject(this)
        presenter.view = this
        registry.addObserver(presenter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EDIT_TITLE_AND_DESCRIPTION && resultCode == Activity.RESULT_OK)
            presenter.onTitleAndDescriptionEdited(
                    title = data!!.extras.getString(EditTitleAndDescriptionActivity.TITLE),
                    description = data.extras.getString(EditTitleAndDescriptionActivity.DESCRIPTION))
        else if (requestCode == EDIT_TITLE_AND_DESCRIPTION && resultCode == Activity.RESULT_CANCELED)
            presenter.onEditTitleAndDescriptionCanceled()
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

    override fun navigateToPickImage() {
        PickImageActivity.startActivityForResult(this, PICK_IMAGE)
    }

    override fun navigateToCropImage(selectedImageUriString: String) {
        CropImageActivity.startActivityForResult(this, selectedImageUriString)
    }

    override fun getLifecycle(): LifecycleRegistry = registry
}
