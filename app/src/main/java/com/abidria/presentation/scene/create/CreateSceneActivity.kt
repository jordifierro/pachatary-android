package com.abidria.presentation.scene.create

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.abidria.R
import com.yalantis.ucrop.UCrop
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.MimeType.*
import com.zhihu.matisse.engine.impl.PicassoEngine
import java.io.File


class CreateSceneActivity : AppCompatActivity() {

    val EDIT_TITLE_AND_DESCRIPTION = 1
    val SELECT_LOCATION = 2
    val SELECT_IMAGE = 3
    val CROP_IMAGE = UCrop.REQUEST_CROP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_scene)
        // val editTitleAndDescriptionIntent = EditTitleAndDescriptionActivity.newIntent(this)
        // val selectLocationIntent = SelectLocationActivity.newIntent(this)
        // startActivityForResult(selectLocationIntent, SELECT_LOCATION)

        Matisse.from(this)
                .choose(allOf())
                .countable(true)
                .maxSelectable(1)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(PicassoEngine())
                .forResult(SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EDIT_TITLE_AND_DESCRIPTION)
            Toast.makeText(this, "title: " + data!!.extras.getString(EditTitleAndDescriptionActivity.TITLE)
                + ", desc: " + data.extras.getString(EditTitleAndDescriptionActivity.DESCRIPTION), Toast.LENGTH_LONG)
                    .show()
        else if (requestCode == SELECT_LOCATION)
            Toast.makeText(this, "latitude: " + data!!.extras.getDouble(SelectLocationActivity.LATITUDE).toString()
                    + ", longitude: " + data.extras.getDouble(SelectLocationActivity.LONGITUDE).toString(), Toast.LENGTH_LONG)
                    .show()
        else if (requestCode == SELECT_IMAGE) {
            val selectedImageUri = Matisse.obtainResult(data)[0]
            Toast.makeText(this, "selected image: " + selectedImageUri, Toast.LENGTH_LONG).show()
            val outputDir = this.cacheDir
            val outputFile = File.createTempFile("scene", "", outputDir)
            val outputUri = Uri.fromFile(outputFile)
            UCrop.of(selectedImageUri, outputUri)
                    .withAspectRatio(1.0f, 1.0f)
                    .withMaxResultSize(2000, 2000)
                    .start(this)
        } else if (requestCode == CROP_IMAGE) {
            val resultUri = UCrop.getOutput(data!!)
            Toast.makeText(this, "cropped image: " + resultUri, Toast.LENGTH_LONG).show()
        }
    }
}
