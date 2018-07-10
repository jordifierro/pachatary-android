package com.pachatary.presentation.common.edition

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import javax.inject.Inject

class PickAndCropImagePresenter @Inject constructor(): LifecycleObserver {

    lateinit var view: PickAndCropImageView

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun create() {
        if (view.hasStoragePermissions()) view.showPickImage()
        else if (view.shouldShowExplanation()) view.showPermissionsExplanationDialog()
        else view.askStoragePermissions()
    }

    fun onPermissionsAccepted() {
        view.showPickImage()
    }

    fun onPermissionsDenied() {
        if (view.shouldShowExplanation()) view.showPermissionsExplanationDialog()
        else view.showSettingsRecover()
    }

    fun onPermissionsExplanationDialogAccept() {
        view.askStoragePermissions()
    }

    fun onPermissionsExplanationDialogCancel() {
        view.finishWithResultCancel()
    }

    fun onSettingsRecoverCancel() {
        view.finishWithResultCancel()
    }

    fun onSettingsClick() {
        view.navigateToSettings()
    }

    fun onSettingsClosed() {
        if (view.hasStoragePermissions()) view.showPickImage()
        else view.showSettingsRecover()
    }

    fun onPickImageSuccess(selectedImageUriString: String) {
        view.showCropImage(selectedImageUriString)
    }

    fun onPickImageCancel() {
        view.finishWithResultCancel()
    }

    fun onCropImageSuccess(croppedImageUriString: String) {
        view.finishWithResultImage(croppedImageUriString)
    }

    fun onCropImageCancel() {
        view.showPickImage()
    }
}
