package com.pachatary.presentation.experience.show

interface SearchSettingsView {
    fun setSearchText(searchText: String)
    fun setLocationOptionSelected(locationOption: SearchSettingsModel.LocationOption)
    fun navigateToSelectLocation(selectedLatitude: Double, selectedLongitude: Double)
    fun finishViewWithResult(searchSettingsModel: SearchSettingsModel)
}