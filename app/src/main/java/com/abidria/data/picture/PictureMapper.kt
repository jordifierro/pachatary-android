package com.abidria.data.picture

import com.abidria.data.common.ToDomainMapper

data class PictureMapper(val smallUrl: String, val mediumUrl: String, val largeUrl: String) : ToDomainMapper<Picture> {

    override fun toDomain() = Picture(smallUrl = this.smallUrl, mediumUrl = this.mediumUrl, largeUrl = this.largeUrl)
}
