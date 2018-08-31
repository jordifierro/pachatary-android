package com.pachatary.presentation.common.edition

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.webkit.MimeTypeMap
import com.pachatary.BuildConfig
import com.pachatary.R
import com.pachatary.presentation.common.PachataryApplication
import com.yalantis.ucrop.UCrop
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.MimeType.JPEG
import com.zhihu.matisse.MimeType.PNG
import com.zhihu.matisse.engine.impl.PicassoEngine
import java.io.File
import javax.inject.Inject


class PickAndCropImageActivity : AppCompatActivity(), PickAndCropImageView {

    private val REQUEST_READ_WRITE_EXTERNAL_STORAGE_PERMISSIONS = 1
    private val REQUEST_SETTINGS = 2
    private val PICK_IMAGE_ACTIVITY_INTENT = 3
    private val CROP_IMAGE_ACTIVITY_INTENT = UCrop.REQUEST_CROP

    @Inject
    lateinit var presenter: PickAndCropImagePresenter

    val registry: LifecycleRegistry = LifecycleRegistry(this)

    companion object {
        private const val RESULT_IMAGE_URI = "result_image_uri"
        fun newIntent(context: Context) = Intent(context, PickAndCropImageActivity::class.java)
        fun getImageUriFrom(data: Intent) = data.getStringExtra(RESULT_IMAGE_URI)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PachataryApplication.injector.inject(this)
        presenter.view = this
        registry.addObserver(presenter)
    }

    override fun hasStoragePermissions(): Boolean {
        return (checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") ==
                PackageManager.PERMISSION_GRANTED &&
                checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE") ==
                PackageManager.PERMISSION_GRANTED)
    }

    override fun shouldShowExplanation(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
        return true
    }

    override fun askStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_WRITE_EXTERNAL_STORAGE_PERMISSIONS)
        }
        else throw Exception()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_READ_WRITE_EXTERNAL_STORAGE_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                presenter.onPermissionsAccepted()
            }
            else presenter.onPermissionsDenied()
        }
        else if (requestCode == REQUEST_SETTINGS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                presenter.onPermissionsAccepted()
            }
            else if (this.shouldShowExplanation()) presenter.onPermissionsDenied()
            else presenter.onPermissionsDenied()
        }
        else throw Exception(requestCode.toString())
    }

    override fun showSettingsRecover() {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        else builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.activity_pick_and_crop_image_recover_dialog_title)
                .setMessage(R.string.activity_pick_and_crop_image_recover_dialog_text)
                .setPositiveButton(android.R.string.yes, { _, _ -> presenter.onSettingsClick() })
                .setNegativeButton(android.R.string.no, { _, _ -> presenter.onSettingsRecoverCancel() })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    override fun showPermissionsExplanationDialog() {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        else builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.activity_pick_and_crop_image_explanation_dialog_title)
                .setMessage(R.string.activity_pick_and_crop_image_explanation_dialog_text)
                .setPositiveButton(android.R.string.yes) {
                    _, _ -> presenter.onPermissionsExplanationDialogAccept() }
                .setNegativeButton(android.R.string.no) {
                    _, _ -> presenter.onPermissionsExplanationDialogCancel() }
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    override fun navigateToSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivityForResult(intent, REQUEST_SETTINGS)
    }

    override fun showPickImage() {
        Matisse.from(this)
                .choose(MimeType.of(JPEG, PNG))
                .countable(false)
                .maxSelectable(1)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(PicassoEngine())
                .forResult(PICK_IMAGE_ACTIVITY_INTENT)
    }

    override fun showCropImage(selectedImageUriString: String) {
        var extension = File(Uri.parse(selectedImageUriString).path).extension
        if (extension == "") extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(
                this.getContentResolver().getType(Uri.parse(selectedImageUriString)))

        val outputUri = Uri.fromFile(
                File.createTempFile("pachatary", "." + extension, this.cacheDir))
        UCrop.of(Uri.parse(selectedImageUriString), outputUri)
                .withAspectRatio(1.0f, 1.0f)
                .withMaxResultSize(BuildConfig.MAX_IMAGE_SIZE, BuildConfig.MAX_IMAGE_SIZE)
                .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE_ACTIVITY_INTENT) {
            if (resultCode == Activity.RESULT_OK)
                presenter.onPickImageSuccess(Matisse.obtainResult(data)[0].toString())
            else if (resultCode == Activity.RESULT_CANCELED) presenter.onPickImageCancel()
            else throw Exception(resultCode.toString())
        }
        else if (requestCode == CROP_IMAGE_ACTIVITY_INTENT) {
            if (resultCode == Activity.RESULT_OK)
                presenter.onCropImageSuccess(UCrop.getOutput(data!!).toString())
            else if (resultCode == Activity.RESULT_CANCELED) presenter.onCropImageCancel()
            else throw Exception(resultCode.toString())
        }
        else if (requestCode == REQUEST_SETTINGS) {
            presenter.onSettingsClosed()
        }
        else throw Exception(requestCode.toString())
    }

    override fun finishWithResultCancel() {
        setResult(Activity.RESULT_CANCELED, Intent())
        finish()
    }

    override fun finishWithResultImage(croppedImageUriString: String) {
        val resultIntent = Intent()
        resultIntent.putExtra(RESULT_IMAGE_URI, croppedImageUriString)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun getLifecycle(): LifecycleRegistry = registry
}
