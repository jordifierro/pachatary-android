package com.pachatary.presentation.experience.edition

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.edition.EditTitleAndDescriptionActivity
import com.pachatary.presentation.common.edition.PickAndCropImageActivity
import com.pachatary.presentation.common.edition.PickImageActivity
import kotlinx.android.synthetic.main.activity_create_experience.*
import javax.inject.Inject


class CreateExperienceActivity : AppCompatActivity(), CreateExperienceView {

    val EDIT_TITLE_AND_DESCRIPTION = 1
    val SELECT_IMAGE = 2

    @Inject
    lateinit var presenter: CreateExperiencePresenter

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        fun newIntent(context: Context) = Intent(context, CreateExperienceActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_experience)
        setSupportActionBar(toolbar)

        PachataryApplication.injector.inject(this)
        presenter.view = this
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
        else if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK)
            presenter.onImageSelectSuccess(PickAndCropImageActivity.getImageUriFrom(data!!))
        else if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_CANCELED)
            presenter.onImageSelectCancel()
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

    override fun getLifecycle(): LifecycleRegistry = registry
}
