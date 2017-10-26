package com.abidria.presentation.scene.create

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.abidria.R
import com.abidria.presentation.common.AbidriaApplication
import kotlinx.android.synthetic.main.activity_edit_title_and_description.*
import javax.inject.Inject


class EditTitleAndDescriptionActivity : AppCompatActivity(), EditTitleAndDescriptionView {

    @Inject
    lateinit var presenter: EditTitleAndDescriptionPresenter

    lateinit var titleEditText: EditText
    lateinit var descriptionEditText: EditText
    lateinit var doneButton: Button

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        val TITLE = "title"
        val DESCRIPTION = "description"

        fun newIntent(context: Context): Intent = Intent(context, EditTitleAndDescriptionActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_title_and_description)
        setSupportActionBar(toolbar)

        titleEditText = findViewById<EditText>(R.id.edit_title_and_description_title_edittext)
        descriptionEditText = findViewById<EditText>(R.id.edit_title_and_description_description_edittext)
        doneButton = findViewById<Button>(R.id.edit_title_and_description_done_button)
        doneButton.setOnClickListener { presenter.doneButtonClick() }

        AbidriaApplication.injector.inject(this)
        presenter.view = this
        registry.addObserver(presenter)
    }

    override fun title(): String = titleEditText.text.toString()
    override fun description(): String = descriptionEditText.text.toString()

    override fun finishWith(title: String, description: String) {
        val returnIntent = Intent()
        returnIntent.putExtra(TITLE, title)
        returnIntent.putExtra(DESCRIPTION, description)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun showTitleLengthError() {
        Toast.makeText(this, "Title must be between 1 and 30 chars", Toast.LENGTH_LONG).show()
    }

    override fun getLifecycle(): LifecycleRegistry = registry
}
