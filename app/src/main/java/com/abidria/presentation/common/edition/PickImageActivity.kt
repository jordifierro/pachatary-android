package com.abidria.presentation.common.edition

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.MimeType.JPEG
import com.zhihu.matisse.MimeType.PNG
import com.zhihu.matisse.engine.impl.PicassoEngine

class PickImageActivity {

    companion object {
        fun startActivityForResult(activity: Activity, resultCode: Int) {
            Matisse.from(activity)
                    .choose(MimeType.of(JPEG, PNG))
                    .countable(false)
                    .maxSelectable(1)
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .thumbnailScale(0.85f)
                    .imageEngine(PicassoEngine())
                    .forResult(resultCode)
        }

        fun getPickedImageUriStringFromResultData(data: Intent) = Matisse.obtainResult(data)[0].toString()
    }
}
