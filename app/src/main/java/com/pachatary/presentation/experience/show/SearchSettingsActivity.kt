package com.pachatary.presentation.experience.show

import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.edition.SelectLocationActivity
import com.pachatary.presentation.common.edition.SelectLocationPresenter
import javax.inject.Inject


class SearchSettingsActivity : AppCompatActivity(), SearchSettingsView {

    lateinit var searchEditText: EditText
    lateinit var currentLocationButton: Button
    lateinit var selectLocationButton: Button
    lateinit var goButton: Button

    @Inject
    lateinit var presenter: SearchSettingsPresenter

    private val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private val SEARCH_TEXT = "search_text"
        private val LOCATION_OPTION = "location_option"
        private val CURRENT_LOCATION_LATITUDE = "current_location_latitude"
        private val CURRENT_LOCATION_LONGITUDE = "current_location_longitude"
        private val SELECTED_LOCATION_LATITUDE = "selected_location_latitude"
        private val SELECTED_LOCATION_LONGITUDE = "selected_location_longitude"
        private val SELECT_LOCATION_ACTIVITY = 1

        fun newIntent(context: Context, searchSettingsModel: SearchSettingsModel): Intent {
            val intent = Intent(context, SearchSettingsActivity::class.java)
            intent.putExtra(SEARCH_TEXT, searchSettingsModel.searchText)
            intent.putExtra(LOCATION_OPTION, searchSettingsModel.locationOption)
            intent.putExtra(CURRENT_LOCATION_LATITUDE, searchSettingsModel.currentLatitude)
            intent.putExtra(CURRENT_LOCATION_LONGITUDE, searchSettingsModel.currentLongitude)
            intent.putExtra(SELECTED_LOCATION_LATITUDE, searchSettingsModel.selectedLatitude)
            intent.putExtra(SELECTED_LOCATION_LONGITUDE, searchSettingsModel.selectedLongitude)
            return intent
        }

        fun getSearchSettingsModelFromIntent(intent: Intent): SearchSettingsModel {
            val searchText = intent.extras.getString(SEARCH_TEXT)
            val locationOption =
                    intent.extras.getSerializable(LOCATION_OPTION) as SearchSettingsModel.LocationOption
            val currentLatitude = intent.extras.getDouble(CURRENT_LOCATION_LATITUDE)
            val currentLongitude = intent.extras.getDouble(CURRENT_LOCATION_LONGITUDE)
            val selectedLatitude = intent.extras.getDouble(SELECTED_LOCATION_LATITUDE)
            val selectedLongitude = intent.extras.getDouble(SELECTED_LOCATION_LONGITUDE)
            return SearchSettingsModel(searchText, locationOption, currentLatitude,
                    currentLongitude, selectedLatitude, selectedLongitude)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_settings)
        setFinishOnTouchOutside(true)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        searchEditText = findViewById(R.id.search_search_edittext)
        searchEditText.addTextChangedListener( object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter.onSearchTextChanged(searchEditText.text.toString())
            }
        })

        currentLocationButton = findViewById(R.id.search_current_location_button)
        currentLocationButton.setOnClickListener {
            presenter.onLocationOptionClick(SearchSettingsModel.LocationOption.CURRENT) }
        selectLocationButton = findViewById(R.id.search_select_location_button)
        selectLocationButton.setOnClickListener {
            presenter.onLocationOptionClick(SearchSettingsModel.LocationOption.SELECTED) }
        goButton = findViewById(R.id.search_go_button)
        goButton.setOnClickListener { presenter.onGoClick() }

        PachataryApplication.injector.inject(this)
        presenter.setViewAndModel(this, getSearchSettingsModelFromIntent(intent))
        presenter.create()
        registry.addObserver(presenter)
    }

    override fun setSearchText(searchText: String) {
        searchEditText.setText(searchText)
    }

    override fun setLocationOptionSelected(locationOption: SearchSettingsModel.LocationOption) {
        when (locationOption) {
            SearchSettingsModel.LocationOption.CURRENT -> {
                currentLocationButton.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                selectLocationButton.setBackgroundColor(resources.getColor(R.color.grey)) }
            SearchSettingsModel.LocationOption.SELECTED -> {
                currentLocationButton.setBackgroundColor(resources.getColor(R.color.grey))
                selectLocationButton.setBackgroundColor(resources.getColor(R.color.colorPrimary)) }
        }
    }

    override fun navigateToSelectLocation(selectedLatitude: Double, selectedLongitude: Double) {
        startActivityForResult(SelectLocationActivity.newIntent( this.applicationContext,
                                                        selectedLatitude, selectedLongitude,
                                                        SelectLocationPresenter.LocationType.APROX),
                SELECT_LOCATION_ACTIVITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_LOCATION_ACTIVITY && resultCode == Activity.RESULT_OK)
            presenter.onLocationSelected(
                    data!!.extras.getDouble(SelectLocationActivity.LATITUDE),
                    data.extras.getDouble(SelectLocationActivity.LONGITUDE))
    }

    override fun finishViewWithResult(searchSettingsModel: SearchSettingsModel) {
        val returnIntent = Intent()
        returnIntent.putExtra(SEARCH_TEXT, searchSettingsModel.searchText)
        returnIntent.putExtra(LOCATION_OPTION, searchSettingsModel.locationOption)
        returnIntent.putExtra(CURRENT_LOCATION_LATITUDE, searchSettingsModel.currentLatitude)
        returnIntent.putExtra(CURRENT_LOCATION_LONGITUDE, searchSettingsModel.currentLongitude)
        returnIntent.putExtra(SELECTED_LOCATION_LATITUDE, searchSettingsModel.selectedLatitude)
        returnIntent.putExtra(SELECTED_LOCATION_LONGITUDE, searchSettingsModel.selectedLongitude)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun getLifecycle(): LifecycleRegistry = registry
}
