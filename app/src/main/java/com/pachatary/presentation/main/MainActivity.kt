package com.pachatary.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.experience.show.ExploreFragment
import com.pachatary.presentation.experience.show.MyExperiencesFragment
import com.pachatary.presentation.experience.show.SavedFragment
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainView {

    private lateinit var myExperiencesFragment: MyExperiencesFragment
    private lateinit var savedFragment: SavedFragment
    private lateinit var exploreFragment: ExploreFragment
    private lateinit var progressBar: ProgressBar

    @Inject
    lateinit var presenter: MainPresenter

    companion object {
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.experiences_progressbar)

        PachataryApplication.injector.inject(this)
        presenter.view = this
        lifecycle.addObserver(presenter)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun selectTab(type: MainView.ExperiencesViewType) {
        when (type) {
            MainView.ExperiencesViewType.MY_EXPERIENCES ->
                navigation.selectedItemId = R.id.navigation_mine
            MainView.ExperiencesViewType.SAVED ->
                navigation.selectedItemId = R.id.navigation_saved
            MainView.ExperiencesViewType.EXPLORE ->
                navigation.selectedItemId = R.id.navigation_explore
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_mine -> {
                presenter.onTabClick(MainView.ExperiencesViewType.MY_EXPERIENCES)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_saved -> {
                presenter.onTabClick(MainView.ExperiencesViewType.SAVED)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_explore -> {
                presenter.onTabClick(MainView.ExperiencesViewType.EXPLORE)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        progressBar.visibility = View.INVISIBLE
    }

    override fun showView(viewType: MainView.ExperiencesViewType) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        when (viewType) {
            MainView.ExperiencesViewType.MY_EXPERIENCES -> {
                if (::savedFragment.isInitialized && savedFragment.isVisible)
                    fragmentTransaction.hide(savedFragment)
                if (::exploreFragment.isInitialized && exploreFragment.isVisible)
                    fragmentTransaction.hide(exploreFragment)

                if (!this::myExperiencesFragment.isInitialized) {
                    myExperiencesFragment = MyExperiencesFragment.newInstance()
                    fragmentTransaction.add(R.id.fragment_container, myExperiencesFragment)
                }
                else fragmentTransaction.show(myExperiencesFragment)
            }
            MainView.ExperiencesViewType.SAVED -> {
                if (::myExperiencesFragment.isInitialized && myExperiencesFragment.isVisible)
                    fragmentTransaction.hide(myExperiencesFragment)
                if (::exploreFragment.isInitialized && exploreFragment.isVisible)
                    fragmentTransaction.hide(exploreFragment)

                if (!this::savedFragment.isInitialized) {
                    savedFragment = SavedFragment.newInstance()
                    fragmentTransaction.add(R.id.fragment_container, savedFragment)
                }
                else fragmentTransaction.show(savedFragment)
            }
            MainView.ExperiencesViewType.EXPLORE -> {
                if (::myExperiencesFragment.isInitialized && myExperiencesFragment.isVisible)
                    fragmentTransaction.hide(myExperiencesFragment)
                if (::savedFragment.isInitialized && savedFragment.isVisible)
                    fragmentTransaction.hide(savedFragment)

                if (!this::exploreFragment.isInitialized) {
                    exploreFragment = ExploreFragment.newInstance()
                    fragmentTransaction.add(R.id.fragment_container, exploreFragment)
                }
                else fragmentTransaction.show(exploreFragment)
            }
        }
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        fragmentTransaction.commit()
    }

    override fun showTabs(visible: Boolean) {
        if (visible) navigation.visibility = View.VISIBLE
        else  navigation.visibility = View.INVISIBLE
    }

    override fun onBackPressed() {
        presenter.onBackPressed()
    }
}
