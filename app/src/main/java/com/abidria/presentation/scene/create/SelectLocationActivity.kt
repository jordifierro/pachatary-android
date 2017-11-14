package com.abidria.presentation.scene.create

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.abidria.BuildConfig
import com.abidria.R
import com.abidria.presentation.common.AbidriaApplication
import com.abidria.presentation.scene.create.SelectLocationPresenter.LocationType
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.activity_select_location.*
import javax.inject.Inject


class SelectLocationActivity : AppCompatActivity(), SelectLocationView {

    @Inject
    lateinit var presenter: SelectLocationPresenter

    lateinit var mapView: MapView
    lateinit var mapboxMap: MapboxMap
    lateinit var doneButton: Button

    lateinit var initialLocationType: SelectLocationPresenter.LocationType
    var initialLatitude = 0.0
    var initialLongitude = 0.0

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        val LATITUDE = "latitude"
        val LONGITUDE = "longitude"

        val INITIAL_LATITUDE = "initial_latitude"
        val INITIAL_LONGITUDE = "initial_longitude"
        val INITIAL_LOCATION_TYPE = "initial_location_type"

        fun newIntent(context: Context, initialLatitude: Double = 0.0,
                      initialLongitude: Double = 0.0, initialType: LocationType = LocationType.UNKNWON): Intent {
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
        setSupportActionBar(toolbar)

        initialLatitude = intent.getDoubleExtra(INITIAL_LATITUDE, 0.0)
        initialLongitude = intent.getDoubleExtra(INITIAL_LONGITUDE, 0.0)
        initialLocationType = intent.getSerializableExtra(INITIAL_LOCATION_TYPE) as LocationType

        mapView = findViewById<MapView>(R.id.select_location_mapview)
        mapView.onCreate(savedInstanceState)

        doneButton = findViewById<Button>(R.id.select_location_done_button)
        doneButton.setOnClickListener { presenter.doneButtonClick() }

        AbidriaApplication.injector.inject(this)
        presenter.setViewAndInitialLocation(view = this, initialLatitude = initialLatitude,
                                            initialLongitude = initialLongitude,
                                            initialLocationType = initialLocationType)
        registry.addObserver(presenter)
    }

    override fun setInitialLocation(latitude: Double, longitude: Double, zoomLevel: SelectLocationView.ZoomLevel) {
        mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            this.mapboxMap.cameraPosition = CameraPosition.Builder()
                                                                .target(LatLng(latitude, longitude))
                                                                .zoom(zoomLevel.zoom)
                                                                .build()
        }
    }

    fun center(): LatLng {
        val centerPointF = PointF(mapView.width.toFloat()/2, mapView.height.toFloat()/2)
        return mapboxMap.projection.fromScreenLocation(centerPointF)
    }

    override fun latitude() = center().latitude
    override fun longitude() = center().longitude

    override fun finishWith(latitude: Double, longitude: Double) {
        val returnIntent = Intent()
        returnIntent.putExtra(LATITUDE, latitude)
        returnIntent.putExtra(LONGITUDE, longitude)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
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
