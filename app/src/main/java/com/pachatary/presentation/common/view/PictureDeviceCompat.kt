package com.pachatary.presentation.common.view

import com.pachatary.data.picture.BigPicture
import com.pachatary.data.picture.LittlePicture

class PictureDeviceCompat(private val deviceWidth: Int) {

    data class LittleCompatPicture(val iconSizeUrl: String, val halfScreenSizeUrl: String)

    fun convert(littlePicture: LittlePicture?): LittleCompatPicture? {
        if (littlePicture == null) return null
        if (deviceWidth < 1280) return LittleCompatPicture(littlePicture.tinyUrl,
                                                           littlePicture.smallUrl)
        else return LittleCompatPicture(littlePicture.smallUrl, littlePicture.mediumUrl)
    }

    data class BigCompatPicture(val halfScreenSizeUrl: String, val fullScreenSizeUrl: String)

    fun convert(bigPicture: BigPicture?): BigCompatPicture? {
        if (bigPicture == null) return null
        if (deviceWidth < 1280) return BigCompatPicture(bigPicture.smallUrl, bigPicture.mediumUrl)
        else return BigCompatPicture(bigPicture.mediumUrl, bigPicture.largeUrl)
    }
}