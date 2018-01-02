package com.abidria.presentation.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.abidria.R
import com.abidria.presentation.common.AbidriaApplication
import com.abidria.presentation.experience.show.ExploreFragment
import com.abidria.presentation.experience.show.MyExperiencesFragment
import com.abidria.presentation.experience.show.SavedFragment
import com.abidria.presentation.main.MainView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_register.*
import javax.inject.Inject
import android.text.Editable
import android.widget.Toast


class RegisterActivity : AppCompatActivity(), RegisterView {

    lateinit var progressBar: ProgressBar
    lateinit var usernameEditText: EditText
    lateinit var emailEditText: EditText
    lateinit var doneButton: Button

    @Inject
    lateinit var presenter: RegisterPresenter

    companion object {
        fun newIntent(context: Context) = Intent(context, RegisterActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setSupportActionBar(toolbar)

        progressBar = findViewById(R.id.register_progressbar)
        usernameEditText = findViewById(R.id.register_edit_username_edittext)
        emailEditText = findViewById(R.id.register_edit_email_edittext)
        doneButton = findViewById(R.id.register_done_button)
        doneButton.setOnClickListener { presenter.doneButtonClick() }

        AbidriaApplication.injector.inject(this)
        presenter.view = this
        lifecycle.addObserver(presenter)
    }

    override fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        progressBar.visibility = View.INVISIBLE
    }

    override fun blockDoneButton(block: Boolean) {
        doneButton.isEnabled = !block
    }

    override fun getUsername() = usernameEditText.editableText.toString()
    override fun getEmail() = emailEditText.editableText.toString()

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
