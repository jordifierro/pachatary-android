package com.pachatary.data.scene

import com.pachatary.data.common.Identifiable
import com.pachatary.data.picture.Picture

data class Scene(override val id: String,
                 val title: String, val description: String, val picture: Picture?,
                 val latitude: Double, val longitude: Double, val experienceId: String) : Identifiable
