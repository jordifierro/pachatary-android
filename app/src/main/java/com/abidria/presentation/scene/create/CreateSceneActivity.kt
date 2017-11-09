package com.abidria.presentation.scene.create

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.MimeTypeMap
import com.abidria.R
import com.abidria.presentation.common.AbidriaApplication
import com.yalantis.ucrop.UCrop
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType.allOf
import com.zhihu.matisse.engine.impl.PicassoEngine
import kotlinx.android.synthetic.main.activity_create_scene.*
import java.io.File
import javax.inject.Inject


class CreateSceneActivity : AppCompatActivity(), CreateSceneView {

    val EDIT_TITLE_AND_DESCRIPTION = 1
    val SELECT_LOCATION = 2
    val SELECT_IMAGE = 3
    val CROP_IMAGE = UCrop.REQUEST_CROP

    @Inject
    lateinit var presenter: CreateScenePresenter

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private val EXPERIENCE_ID = "experienceId"

        fun newIntent(context: Context, experienceId: String): Intent {
            val intent = Intent(context, CreateSceneActivity::class.java)
            intent.putExtra(EXPERIENCE_ID, experienceId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_scene)
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
        else if (requestCode == SELECT_LOCATION && resultCode == Activity.RESULT_OK)
            presenter.onLocationSelected(latitude = data!!.extras.getDouble(SelectLocationActivity.LATITUDE),
                                         longitude = data.extras.getDouble(SelectLocationActivity.LONGITUDE))
        else if (requestCode == SELECT_LOCATION && resultCode == Activity.RESULT_CANCELED)
            presenter.onSelectLocationCanceled()
        else if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK)
            presenter.onImagePicked(Matisse.obtainResult(data)[0].toString())
        else if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_CANCELED)
            presenter.onPickImageCanceled()
        else if (requestCode == CROP_IMAGE && resultCode == Activity.RESULT_OK)
            presenter.onImageCropped(UCrop.getOutput(data!!).toString())
        else if (requestCode == CROP_IMAGE && resultCode == Activity.RESULT_CANCELED)
            presenter.onCropImageCanceled()
    }

    override fun navigateToEditTitleAndDescription() {
        startActivityForResult(EditTitleAndDescriptionActivity.newIntent(this), EDIT_TITLE_AND_DESCRIPTION)
    }

    override fun navigateToSelectLocation() {
        startActivityForResult(SelectLocationActivity.newIntent(this), SELECT_LOCATION)
    }

    override fun navigateToPickImage() {
        Matisse.from(this)
                .choose(allOf())
                .countable(true)
                .maxSelectable(1)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(PicassoEngine())
                .forResult(SELECT_IMAGE)
    }

    override fun navigateToCropImage(selectedImageUriString: String) {
        var extension = File(Uri.parse(selectedImageUriString).path).extension
        if (extension == "") extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(this.getContentResolver().getType(Uri.parse(selectedImageUriString)))

        val outputUri = Uri.fromFile(File.createTempFile("scene", "." + extension, this.cacheDir))
        UCrop.of(Uri.parse(selectedImageUriString), outputUri)
                .withAspectRatio(1.0f, 1.0f)
                .withMaxResultSize(2000, 2000)
                .start(this)
    }

    override fun getLifecycle(): LifecycleRegistry = registry
}
