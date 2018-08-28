package com.pachatary.presentation.common.edition

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PointF
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.pachatary.BuildConfig
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.edition.SelectLocationPresenter.LocationType
import com.pachatary.presentation.common.location.LocationUtils
import com.pachatary.presentation.common.view.ToolbarUtils
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.IOException
import java.util.*
import javax.inject.Inject


class SelectLocationActivity : AppCompatActivity(), SelectLocationView {

    @Inject
    lateinit var presenter: SelectLocationPresenter

    lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var doneButton: Button
    private lateinit var searchButton: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var locateButton: FloatingActionButton

    private lateinit var initialLocationType: SelectLocationPresenter.LocationType
    private var initialLatitude = 0.0
    private var initialLongitude = 0.0

    private val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        const val PERMISSIONS_DIALOG = 1

        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"

        const val INITIAL_LATITUDE = "initial_latitude"
        const val INITIAL_LONGITUDE = "initial_longitude"
        const val INITIAL_LOCATION_TYPE = "initial_location_type"

        fun newIntent(context: Context, initialLatitude: Double = 0.0,
                      initialLongitude: Double = 0.0,
                      initialType: LocationType = LocationType.UNKNWON): Intent {
            val intent = Intent(context, SelectLocationActivity::class.java)
            intent.putExtra(INITIAL_LATITUDE, initialLatitude)
            intent.putExtra(INITIAL_LONGITUDE, initialLongitude)
            intent.putExtra(INITIAL_LOCATION_TYPE, initialType)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, BuildConfig.MAPBOX_ACCESS_TOKEN)
        setContentView(R.layout.activity_select_location)

        ToolbarUtils.setUp(this, title.toString(), true)

        initialLatitude = intent.getDoubleExtra(INITIAL_LATITUDE, 0.0)
        initialLongitude = intent.getDoubleExtra(INITIAL_LONGITUDE, 0.0)
        initialLocationType = intent.getSerializableExtra(INITIAL_LOCATION_TYPE) as LocationType

        mapView = findViewById(R.id.select_location_mapview)
        mapView.onCreate(savedInstanceState)

        doneButton = findViewById(R.id.select_location_done_button)
        doneButton.setOnClickListener { presenter.doneButtonClick() }
        searchButton = findViewById(R.id.select_location_search_button)
        searchEditText = findViewById(R.id.select_location_search_edittext)
        searchButton.setOnClickListener {
            val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken,
                                                 InputMethodManager.HIDE_NOT_ALWAYS)
            presenter.searchButtonClick(searchEditText.text.toString())
        }
        searchEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                presenter.searchButtonClick(searchEditText.text.toString())
                searchEditText.clearFocus()
                val inm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                return@OnEditorActionListener true
            }
            false
        })
        locateButton = findViewById(R.id.select_location_locate_button)
        locateButton.setOnClickListener { presenter.locateClick() }

        PachataryApplication.injector.inject(this)
        presenter.setViewAndInitialLocation(view = this, initialLatitude = initialLatitude,
                                            initialLongitude = initialLongitude,
                                            initialLocationType = initialLocationType)
        registry.addObserver(presenter)
    }

    override fun setInitialLocation(latitude: Double, longitude: Double,
                                    zoomLevel: SelectLocationView.ZoomLevel) {
        mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            this.mapboxMap.cameraPosition = CameraPosition.Builder()
                                                                .target(LatLng(latitude, longitude))
                                                                .zoom(zoomLevel.zoom)
                                                                .build()
        }
    }

    fun center(): LatLng? {
        if (!::mapboxMap.isInitialized) return null

        val centerPointF = PointF(mapView.width.toFloat()/2, mapView.height.toFloat()/2)
        return mapboxMap.projection.fromScreenLocation(centerPointF)
    }

    override fun latitude() = center()?.latitude
    override fun longitude() = center()?.longitude

    override fun finishWith(latitude: Double, longitude: Double) {
        val returnIntent = Intent()
        returnIntent.putExtra(LATITUDE, latitude)
        returnIntent.putExtra(LONGITUDE, longitude)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun geocodeAddress(address: String): Flowable<Pair<Double, Double>> {
        return Flowable.create({ emitter ->
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val latLongList = geocoder.getFromLocationName(address, 1)
                if (latLongList.size > 0)
                    emitter.onNext(Pair(latLongList[0].latitude, latLongList[0].longitude))
                emitter.onComplete()
            } catch (ioe: IOException) { emitter.onComplete()
            } catch (e: Exception) { emitter.onError(e) }
        }, BackpressureStrategy.LATEST)
    }

    override fun moveMapToPoint(latitude: Double, longitude: Double) {
        mapView.getMapAsync { _ ->
            val newCameraPosition = CameraPosition.Builder()
                                        .target(LatLng(latitude, longitude))
                                        .build()
            this.mapboxMap.animateCamera { _ -> newCameraPosition }
        }
    }

    override fun hasLocationPermission() = LocationUtils.checkLocationPermission(this)

    override fun askLocation() {
        LocationUtils.addListenerToLocation(this) { location: Location? ->
            if (location != null)
                presenter.onLocationFound(location.latitude, location.longitude)
        }
    }

    @SuppressLint("NewApi")
    override fun askLocationPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSIONS_DIALOG)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_DIALOG -> {
                if ((grantResults.isNotEmpty()
                                && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    presenter.onLocationPermissionAccepted()
                else presenter.onLocationPermissionDenied()
                return
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState!!)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun getLifecycle(): LifecycleRegistry = registry
}
