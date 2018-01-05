package com.pachatary.data.experience

import com.pachatary.data.common.Identifiable
import com.pachatary.data.picture.Picture

data class Experience(override val id: String, val title: String,
                      val description: String, val picture: Picture?,
                      val isMine: Boolean = false, val isSaved: Boolean = false) : Identifiable
