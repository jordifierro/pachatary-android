package com.abidria.presentation.main

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import com.abidria.R
import com.abidria.presentation.experience.show.ExploreFragment
import com.abidria.presentation.experience.show.MyExperiencesFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val fragmentManager = getSupportFragmentManager()
    lateinit var exploreFragment: ExploreFragment
    lateinit var myExperiencesFragment: MyExperiencesFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_explore
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_mine -> {
                if (!this::myExperiencesFragment.isInitialized)
                    myExperiencesFragment = MyExperiencesFragment.newInstance()
                navigateToFragment(myExperiencesFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_saved -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_explore -> {
                if (!this::exploreFragment.isInitialized)
                    exploreFragment = ExploreFragment.newInstance()
                navigateToFragment(exploreFragment)
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
}
