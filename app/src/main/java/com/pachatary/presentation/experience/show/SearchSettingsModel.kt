package com.pachatary.presentation.experience.show

data class SearchSettingsModel(var searchText: String, var locationOption: LocationOption,
                               var currentLatitude: Double, var currentLongitude: Double,
                               var selectedLatitude: Double, var selectedLongitude: Double) {
    enum class LocationOption { CURRENT, SELECTED }
}