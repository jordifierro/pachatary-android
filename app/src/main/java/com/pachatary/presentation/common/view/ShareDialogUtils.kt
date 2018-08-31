package com.pachatary.presentation.common.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.pachatary.R


class ShareDialogUtils {
    companion object {
        fun shareUrl(appCompatActivity: AppCompatActivity, url: String) {
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_TEXT, url)
            appCompatActivity.startActivity(Intent.createChooser(i,
                    appCompatActivity.resources.getString(R.string.dialog_share_url_title)))

        }
    }
}