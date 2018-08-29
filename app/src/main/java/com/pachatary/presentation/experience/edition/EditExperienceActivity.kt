package com.pachatary.presentation.experience.edition

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
import com.pachatary.presentation.common.edition.EditTitleAndDescriptionActivity
import com.pachatary.presentation.common.edition.PickAndCropImageActivity
import javax.inject.Inject


class EditExperienceActivity : AppCompatActivity(), EditExperienceView {

    val EDIT_TITLE_AND_DESCRIPTION = 1
    val SELECT_IMAGE = 2

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
        setContentView(R.layout.activity_experience_edition)

        PachataryApplication.injector.inject(this)
        presenter.setView(this, intent.getStringExtra(EXPERIENCE_ID))
        registry.addObserver(presenter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EDIT_TITLE_AND_DESCRIPTION && resultCode == Activity.RESULT_OK)
            presenter.onTitleAndDescriptionEdited(
                    title = data!!.extras.getString(EditTitleAndDescriptionActivity.TITLE),
                    description =
                        data.extras.getString(EditTitleAndDescriptionActivity.DESCRIPTION))
        else if (requestCode == EDIT_TITLE_AND_DESCRIPTION &&
                 resultCode == Activity.RESULT_CANCELED)
            presenter.onEditTitleAndDescriptionCanceled()
        else if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK)
            presenter.onSelectImageSuccess(PickAndCropImageActivity.getImageUriFrom(data!!))
        else if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_CANCELED)
            presenter.onSelectImageCancel()
    }

    override fun navigateToEditTitleAndDescription(initialTitle: String,
                                                   initialDescription: String) {
        startActivityForResult(
                EditTitleAndDescriptionActivity.newIntent(this, initialTitle, initialDescription),
                EDIT_TITLE_AND_DESCRIPTION)
    }

    override fun navigateToSelectImage() {
        startActivityForResult(PickAndCropImageActivity.newIntent(this), SELECT_IMAGE)
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
