package com.abidria.presentation.common.edition

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import com.abidria.BuildConfig
import com.yalantis.ucrop.UCrop
import java.io.File

class CropImageActivity {

    companion object {
        fun startActivityForResult(activity: Activity, imageUriString: String) {
            var extension = File(Uri.parse(imageUriString).path).extension
            if (extension == "") extension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(activity.getContentResolver().getType(Uri.parse(imageUriString)))

            val outputUri = Uri.fromFile(File.createTempFile("scene", "." + extension, activity.cacheDir))
            UCrop.of(Uri.parse(imageUriString), outputUri)
                    .withAspectRatio(1.0f, 1.0f)
                    .withMaxResultSize(BuildConfig.MAX_IMAGE_SIZE, BuildConfig.MAX_IMAGE_SIZE)
                    .start(activity)
        }

        fun getCroppedImageUriStringFromResultData(data: Intent) = UCrop.getOutput(data).toString()
    }
}
