package com.abidria.presentation.scene.create

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.abidria.R

class CreateSceneActivity : AppCompatActivity() {

    val EDIT_TITLE_AND_DESCRIPTION = 1
    val SELECT_LOCATION = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_scene)
        // val editTitleAndDescriptionIntent = EditTitleAndDescriptionActivity.newIntent(this)
        val selectLocationIntent = SelectLocationActivity.newIntent(this)
        startActivityForResult(selectLocationIntent, SELECT_LOCATION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EDIT_TITLE_AND_DESCRIPTION)
            Toast.makeText(this, "title: " + data!!.extras.getString(EditTitleAndDescriptionActivity.TITLE)
                + ", desc: " + data.extras.getString(EditTitleAndDescriptionActivity.DESCRIPTION), Toast.LENGTH_LONG)
                    .show()
        if (requestCode == SELECT_LOCATION)
            Toast.makeText(this, "latitude: " + data!!.extras.getDouble(SelectLocationActivity.LATITUDE).toString()
                    + ", longitude: " + data.extras.getDouble(SelectLocationActivity.LONGITUDE).toString(), Toast.LENGTH_LONG)
                    .show()
    }
}
