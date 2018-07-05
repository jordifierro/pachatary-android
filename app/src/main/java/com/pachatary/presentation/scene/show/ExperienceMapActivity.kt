package com.pachatary.presentation.scene.show

import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.pachatary.BuildConfig
import com.pachatary.R
import com.pachatary.data.scene.Scene
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.scene.edition.CreateSceneActivity
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.ReplaySubject
import kotlinx.android.synthetic.main.activity_experience_map.*
import java.util.*
import javax.inject.Inject


class ExperienceMapActivity : AppCompatActivity(), ExperienceMapView {

    @Inject
    lateinit var presenter: ExperienceMapPresenter

    lateinit var mapView: MapView
    lateinit var mapboxMap: MapboxMap
    lateinit var progressBar: ProgressBar
    lateinit var createSceneButton: FloatingActionButton
    lateinit var saveExperienceButton: FloatingActionButton
    lateinit var unsaveExperienceButton: FloatingActionButton

    val mapLoadedReplaySubject: ReplaySubject<Any> = ReplaySubject.create()

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    val markersHashMap: HashMap<Long, String> = HashMap()

    companion object {
        private val EXPERIENCE_ID = "experienceId"

        fun newIntent(context: Context, experienceId: String): Intent {
            val intent = Intent(context, ExperienceMapActivity::class.java)
            intent.putExtra(EXPERIENCE_ID, experienceId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, BuildConfig.MAPBOX_ACCESS_TOKEN)
        setContentView(R.layout.activity_experience_map)
        setSupportActionBar(toolbar)

        progressBar = findViewById(R.id.scenes_progressbar)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        createSceneButton = findViewById(R.id.create_new_scene_button)
        createSceneButton.setOnClickListener { presenter.onCreateSceneClick() }
        saveExperienceButton = findViewById(R.id.save_experience_button)
        saveExperienceButton.setOnClickListener { presenter.onSaveExperienceClick() }
        unsaveExperienceButton = findViewById(R.id.unsave_experience_button)
        unsaveExperienceButton.setOnClickListener { presenter.onUnsaveExperienceClick() }

        PachataryApplication.injector.inject(this)
        presenter.setView(this, intent.getStringExtra(EXPERIENCE_ID))
        registry.addObserver(presenter)

        mapView.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapLoadedReplaySubject.onNext(true)
            mapLoadedReplaySubject.onComplete()
        }
    }

    override fun mapLoadedFlowable(): Flowable<Any> = mapLoadedReplaySubject.toFlowable(BackpressureStrategy.LATEST)

    private fun getBitmap(drawableRes: Int): Bitmap {
        val drawable = getResources().getDrawable(drawableRes)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight())
        drawable.draw(canvas)

        return bitmap
    }

    override fun showScenesOnMap(scenes: List<Scene>) {
        if (scenes.isNotEmpty()) {
            markersHashMap.clear()
            mapboxMap.clear()
            val latLngBoundsBuilder: LatLngBounds.Builder = LatLngBounds.Builder()

            for (scene in scenes) {
                val latLng = LatLng(scene.latitude, scene.longitude)
                val markerViewOptions = MarkerViewOptions().position(latLng)
                markerViewOptions.title(scene.title)

                val icon = IconFactory.getInstance(this).fromBitmap(getBitmap(R.drawable.map_marker_icon))
                markerViewOptions.icon(icon)

                mapboxMap.addMarker(markerViewOptions)
                markersHashMap.put(markerViewOptions.marker.id, scene.id)

                latLngBoundsBuilder.include(latLng)
            }

            if (scenes.size == 1) {
                val latLng1 = LatLng(scenes[0].latitude + 0.002, scenes[0].longitude + 0.002)
                val latLng2 = LatLng(scenes[0].latitude + 0.002, scenes[0].longitude - 0.002)
                val latLng3 = LatLng(scenes[0].latitude - 0.002, scenes[0].longitude + 0.002)
                val latLng4 = LatLng(scenes[0].latitude - 0.002, scenes[0].longitude - 0.002)
                latLngBoundsBuilder.includes(Arrays.asList(latLng1, latLng2, latLng3, latLng4))
            }

            mapboxMap.setOnInfoWindowClickListener {
                marker ->  presenter.onSceneClick(markersHashMap.get(marker.id)!!)
                false
            }
            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBoundsBuilder.build(), 50, 150, 50, 150))
        }
        mapView.visibility = View.VISIBLE
    }

    override fun setTitle(title: String) {
        supportActionBar?.title = title
    }

    override fun navigateToScene(experienceId: String, isExperienceMine: Boolean, sceneId: String) {
        startActivity(SceneListActivity.newIntent(context = this, experienceId = experienceId,
                                                   selectedSceneId = sceneId, isMine = isExperienceMine))
    }

    override fun navigateToCreateScene(experienceId: String) {
        startActivity(CreateSceneActivity.newIntent(context = this, experienceId = experienceId))
    }

    override fun showEditButton() {
        createSceneButton.visibility = View.VISIBLE
        saveExperienceButton.visibility = View.INVISIBLE
        unsaveExperienceButton.visibility = View.INVISIBLE
    }

    override fun showSaveButton(isSaved: Boolean) {
        createSceneButton.visibility = View.INVISIBLE
        if (isSaved) {
            unsaveExperienceButton.visibility = View.VISIBLE
            saveExperienceButton.visibility = View.INVISIBLE
        }
        else {
            saveExperienceButton.visibility = View.VISIBLE
            unsaveExperienceButton.visibility = View.INVISIBLE
        }
    }

    override fun showUnsaveDialog() {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        else builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_title_unsave_experience)
                .setMessage(R.string.dialog_question_unsave_experience)
                .setPositiveButton(android.R.string.yes, { _, _ -> presenter.onConfirmUnsaveExperience() })
                .setNegativeButton(android.R.string.no, { _, _ -> presenter.onCancelUnsaveExperience() })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    override fun showSavedMessage() {
        val message = this.resources.getString(R.string.message_experience_saved)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        progressBar.visibility = View.GONE
    }

    override fun getLifecycle(): LifecycleRegistry = registry

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
}
