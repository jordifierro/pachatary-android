package com.abidria.data.scene

import com.abidria.data.picture.PictureMapper

data class SceneMapper(val id: String, val title: String, val description: String, val picture: PictureMapper?,
                       val latitude: Double, val longitude: Double, val experienceId: String) {

    fun toDomain() = Scene(id = id, title = title, description = description, picture = picture?.toDomain(),
                           latitude = latitude, longitude = longitude, experienceId = experienceId)
}
