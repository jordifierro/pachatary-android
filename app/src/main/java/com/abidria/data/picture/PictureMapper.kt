package com.abidria.data.picture

data class PictureMapper(val smallUrl: String, val mediumUrl: String, val largeUrl: String) {

    fun toDomain() = Picture(smallUrl = this.smallUrl, mediumUrl = this.mediumUrl, largeUrl = this.largeUrl)
}
