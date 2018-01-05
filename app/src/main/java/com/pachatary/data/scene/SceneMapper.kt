package com.pachatary.data.scene

import com.pachatary.data.common.ToDomainMapper
import com.pachatary.data.picture.PictureMapper

data class SceneMapper(val id: String, val title: String, val description: String, val picture: PictureMapper?,
                       val latitude: Double, val longitude: Double, val experienceId: String) : ToDomainMapper<Scene> {

    override fun toDomain() = Scene(id = id, title = title, description = description, picture = picture?.toDomain(),
                                    latitude = latitude, longitude = longitude, experienceId = experienceId)
}
