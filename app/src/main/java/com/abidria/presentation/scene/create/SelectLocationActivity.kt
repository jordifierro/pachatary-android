package com.abidria.presentation.scene.create

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.abidria.BuildConfig
import com.abidria.R
import com.abidria.presentation.common.AbidriaApplication
import com.mapbox.mapboxsdk.Mapbox
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

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        val LATITUDE = "latitude"
        val LONGITUDE = "longitude"

        fun newIntent(context: Context): Intent = Intent(context, SelectLocationActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, BuildConfig.MAPBOX_ACCESS_TOKEN)
        setContentView(R.layout.activity_select_location)
        setSupportActionBar(toolbar)

        mapView = findViewById<MapView>(R.id.select_location_mapview)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap -> this.mapboxMap = mapboxMap }

        doneButton = findViewById<Button>(R.id.select_location_done_button)
        doneButton.setOnClickListener { presenter.doneButtonClick() }

        AbidriaApplication.injector.inject(this)
        presenter.view = this
        registry.addObserver(presenter)
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
