package com.abidria.data.scene

import com.abidria.data.common.Identifiable
import com.abidria.data.picture.Picture

data class Scene(override val id: String,
                 val title: String, val description: String, val picture: Picture?,
                 val latitude: Double, val longitude: Double, val experienceId: String) : Identifiable
