package com.pachatary.presentation.experience.show

import android.arch.lifecycle.LifecycleObserver
import com.pachatary.data.common.Request
import com.pachatary.data.experience.ExperienceRepoSwitch
import com.pachatary.data.experience.ExperienceRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Named

class SearchSettingsPresenter @Inject constructor() : LifecycleObserver {

    lateinit var view: SearchSettingsView
    lateinit var searchSettingsModel: SearchSettingsModel

    fun setViewAndModel(view: SearchSettingsView, model: SearchSettingsModel) {
        this.view = view
        this.searchSettingsModel = model
    }

    fun create() {
        view.setSearchText(searchSettingsModel.searchText)
        view.setLocationOptionSelected(searchSettingsModel.locationOption)
    }

    fun onSearchTextChanged(searchText: String) {
        this.searchSettingsModel.searchText = searchText
    }

    fun onLocationOptionClick(locationOption: SearchSettingsModel.LocationOption) {
        when (locationOption) {
            SearchSettingsModel.LocationOption.CURRENT -> {
                this.searchSettingsModel.locationOption = SearchSettingsModel.LocationOption.CURRENT
                view.setLocationOptionSelected(SearchSettingsModel.LocationOption.CURRENT) }
            SearchSettingsModel.LocationOption.SELECTED -> {
                view.navigateToSelectLocation(searchSettingsModel.selectedLatitude,
                                              searchSettingsModel.selectedLongitude) }
        }
    }

    fun onLocationSelected(latitude: Double, longitude: Double) {
        this.searchSettingsModel.locationOption = SearchSettingsModel.LocationOption.SELECTED
        this.searchSettingsModel.selectedLatitude = latitude
        this.searchSettingsModel.selectedLongitude = longitude
        view.setLocationOptionSelected(SearchSettingsModel.LocationOption.SELECTED)
    }

    fun onGoClick() {
        view.finishViewWithResult(this.searchSettingsModel)
    }
}
