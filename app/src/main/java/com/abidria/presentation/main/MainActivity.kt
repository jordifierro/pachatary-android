package com.abidria.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import com.abidria.R
import com.abidria.presentation.common.AbidriaApplication
import com.abidria.presentation.experience.show.ExploreFragment
import com.abidria.presentation.experience.show.MyExperiencesFragment
import com.abidria.presentation.experience.show.SavedFragment
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainView {

    val fragmentManager = getSupportFragmentManager()
    lateinit var myExperiencesFragment: MyExperiencesFragment
    lateinit var savedFragment: SavedFragment
    lateinit var exploreFragment: ExploreFragment
    lateinit var progressBar: ProgressBar

    @Inject
    lateinit var presenter: MainPresenter

    companion object {
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.experiences_progressbar)

        AbidriaApplication.injector.inject(this)
        presenter.view = this
        lifecycle.addObserver(presenter)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_explore
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

    private fun navigateToFragment(fragment: Fragment) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(fragment.toString())
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        fragmentTransaction.commit()
    }

    override fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        progressBar.visibility = View.INVISIBLE
    }

    override fun showView(viewType: MainView.ExperiencesViewType) {
        when (viewType) {
            MainView.ExperiencesViewType.MY_EXPERIENCES -> {
                if (!this::myExperiencesFragment.isInitialized)
                    myExperiencesFragment = MyExperiencesFragment.newInstance()
                navigateToFragment(myExperiencesFragment) }
            MainView.ExperiencesViewType.SAVED -> {
                if (!this::savedFragment.isInitialized)
                    savedFragment = SavedFragment.newInstance()
                navigateToFragment(savedFragment)
            }
            MainView.ExperiencesViewType.EXPLORE -> {
                if (!this::exploreFragment.isInitialized)
                    exploreFragment = ExploreFragment.newInstance()
                navigateToFragment(exploreFragment)
            }
        }
    }

    override fun showTabs(visible: Boolean) {
        if (visible) navigation.visibility = View.VISIBLE
        else  navigation.visibility = View.INVISIBLE
    }
}
