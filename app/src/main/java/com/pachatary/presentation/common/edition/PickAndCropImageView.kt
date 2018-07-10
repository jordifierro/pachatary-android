package com.pachatary.presentation.common.edition

interface PickAndCropImageView {

    fun hasStoragePermissions(): Boolean
    fun shouldShowExplanation(): Boolean
    fun askStoragePermissions()
    fun showSettingsRecover()
    fun showPermissionsExplanationDialog()
    fun navigateToSettings()
    fun showPickImage()
    fun showCropImage(selectedImageUriString: String)
    fun finishWithResultCancel()
    fun finishWithResultImage(croppedImageUriString: String)
}