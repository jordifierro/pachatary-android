package com.abidria.data.picture

data class PictureMapper(val small: String, val medium: String, val large: String) {

    fun toDomain(): Picture {
        return Picture(small = this.small, medium = this.medium, large = this.large)
    }
}
