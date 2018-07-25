package com.pachatary.presentation.experience.show

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.edition.SelectLocationActivity
import com.pachatary.presentation.common.edition.SelectLocationPresenter
import com.pachatary.presentation.common.location.LocationUtils
import com.pachatary.presentation.common.view.PictureDeviceCompat
import com.pachatary.presentation.experience.show.view.ExtendedListAdapter
import com.pachatary.presentation.main.MainActivity
import com.pachatary.presentation.profile.ProfileActivity
import com.pachatary.presentation.scene.show.ExperienceScenesActivity
import javax.inject.Inject

class ExploreFragment : Fragment(), ExploreView {

    companion object {
        fun newInstance() = ExploreFragment()
    }

    private val PERMISSIONS_DIALOG = 1
    private val SELECT_LOCATION_ACTIVITY = 2

    @Inject
    lateinit var presenter: ExplorePresenter
    @Inject
    lateinit var pictureDeviceCompat: PictureDeviceCompat

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var retryIcon: ImageView
    private lateinit var searchButton: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var locationButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PachataryApplication.injector.inject(this)
        presenter.view = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        progressBar = view.findViewById(R.id.experiences_progressbar)
        retryIcon = view.findViewById(R.id.experiences_retry)
        retryIcon.setOnClickListener { presenter.onRetryClick() }
        recyclerView = view.findViewById(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(activity, 1)
        recyclerView.adapter = ExtendedListAdapter(
                layoutInflater, pictureDeviceCompat, listOf(), false,
                { id -> presenter.onExperienceClick(id) },
                { username -> presenter.onUsernameClicked(username) },
                { presenter.lastExperienceShown() })
        searchEditText = view.findViewById(R.id.experiences_search_edittext)
        searchButton = view.findViewById(R.id.experiences_search_button)
        searchButton.setOnClickListener { presenter.searchClick(searchEditText.text.toString()) }
        locationButton = view.findViewById(R.id.experiences_location_button)
        locationButton.setOnClickListener { presenter.locationClick() }

        presenter.create()
        return view
    }

    override fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        progressBar.visibility = View.GONE
    }

    override fun showRetry() {
        retryIcon.visibility = View.VISIBLE
    }

    override fun hideRetry() {
        retryIcon.visibility = View.GONE
    }

    override fun showPaginationLoader() {
        (recyclerView.adapter as ExtendedListAdapter).inProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hidePaginationLoader() {
        (recyclerView.adapter as ExtendedListAdapter).inProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showExperienceList(experienceList: List<Experience>) {
        (recyclerView.adapter as ExtendedListAdapter).experienceList = experienceList
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(ExperienceScenesActivity.newIntent(activity!!.applicationContext, experienceId))
    }

    override fun hasLocationPermission() = LocationUtils.checkLocationPermission(activity!!)

    override fun askLastKnownLocation() {
        LocationUtils.addListenerToLocation((activity as MainActivity)) { location: Location ->
            presenter.onLastLocationFound(location.latitude, location.longitude)
        }
    }

    override fun askPermissions() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSIONS_DIALOG)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_DIALOG -> {
                if ((grantResults.isNotEmpty()
                                && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    presenter.onPermissionsAccepted()
                else presenter.onPermissionsDenied()
                return
            }
        }
    }

    override fun navigateToPersonsExperiences(username: String) {
        startActivity(ProfileActivity.newIntent(activity!!.applicationContext, username))
    }

    override fun navigateToSelectLocation(latitude: Double?, longitude: Double?) {
        val intent: Intent
        if (latitude != null && longitude != null)
            intent = SelectLocationActivity.newIntent(activity!!.applicationContext, latitude,
                    longitude, SelectLocationPresenter.LocationType.APROX)
        else intent = SelectLocationActivity.newIntent(activity!!.applicationContext)
        startActivityForResult(intent, SELECT_LOCATION_ACTIVITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_LOCATION_ACTIVITY && resultCode == Activity.RESULT_OK) {
            val latitude = data!!.getDoubleExtra(SelectLocationActivity.LATITUDE, 0.0)
            val longitude = data.getDoubleExtra(SelectLocationActivity.LONGITUDE, 0.0)
            presenter.onLocationSelected(latitude, longitude)
        }
    }
}
