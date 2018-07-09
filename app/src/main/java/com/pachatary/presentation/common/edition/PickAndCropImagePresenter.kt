package com.pachatary.presentation.common.edition

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import javax.inject.Inject

class PickAndCropImagePresenter @Inject constructor(): LifecycleObserver {

    lateinit var view: PickAndCropImageView

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        if (view.hasStoragePermissions()) view.showPickImage()
        else {
            if (view.canAskStoragePermissions()) view.askStoragePermissions()
            else view.showSettingsRecover()
        }
    }

    fun onPermissionsAccepted() {
        view.showPickImage()
    }

    fun onPermissionsDenied() {
        if (view.canAskStoragePermissions()) view.showPermissionsExplanationDialog()
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
