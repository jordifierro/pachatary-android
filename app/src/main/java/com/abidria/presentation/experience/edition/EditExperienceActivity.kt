package com.abidria.presentation.experience.edition

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.abidria.R
import com.abidria.presentation.common.AbidriaApplication
import com.abidria.presentation.common.view.edition.CropImageActivity
import com.abidria.presentation.common.view.edition.EditTitleAndDescriptionActivity
import com.abidria.presentation.common.view.edition.PickImageActivity
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_create_experience.*
import javax.inject.Inject


class EditExperienceActivity : AppCompatActivity(), EditExperienceView {

    val EDIT_TITLE_AND_DESCRIPTION = 1
    val PICK_IMAGE = 2
    val CROP_IMAGE = UCrop.REQUEST_CROP

    @Inject
    lateinit var presenter: EditExperiencePresenter

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private val EXPERIENCE_ID = "experience_id"

        fun newIntent(context: Context, experienceId: String): Intent {
            val intent = Intent(context, EditExperienceActivity::class.java)
            intent.putExtra(EXPERIENCE_ID, experienceId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_experience)
        setSupportActionBar(toolbar)

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

    override fun askUserToEditPicture() {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        else builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_title_experience_edited)
                .setMessage(R.string.dialog_question_edit_experience_picture)
                .setPositiveButton(android.R.string.yes,
                        { _, _ -> presenter.onAskUserEditPictureResponse(userWantsToEditPicture = true) })
                .setNegativeButton(android.R.string.no,
                        { _, _ -> presenter.onAskUserEditPictureResponse(userWantsToEditPicture = false) })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    override fun getLifecycle(): LifecycleRegistry = registry
}
