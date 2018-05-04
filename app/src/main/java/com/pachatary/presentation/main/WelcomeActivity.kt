package com.pachatary.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.experience.show.ExploreFragment
import com.pachatary.presentation.experience.show.MyExperiencesFragment
import com.pachatary.presentation.experience.show.SavedFragment
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class WelcomeActivity : AppCompatActivity(), WelcomeView {

    private lateinit var progressBar: ProgressBar
    private lateinit var startButton: Button

    @Inject
    lateinit var presenter: WelcomePresenter

    companion object {
        fun newIntent(context: Context) = Intent(context, WelcomeActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        progressBar = findViewById(R.id.welcome_progressbar)
        startButton = findViewById(R.id.welcome_start_button)
        startButton.setOnClickListener { presenter.onStartClick() }

        PachataryApplication.injector.inject(this)
        presenter.view = this
        lifecycle.addObserver(presenter)
    }

    override fun navigateToMain() {
        startActivity(MainActivity.newIntent(this))
    }

    override fun disableStartButton() {
        startButton.isEnabled = false
    }

    override fun enableStartButton() {
        startButton.isEnabled = true
    }

    override fun showErrorMessage() {
        Toast.makeText(this, "Some error has occurred", Toast.LENGTH_SHORT).show()
    }

    override fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        progressBar.visibility = View.INVISIBLE
    }
}
