package com.abidria.data.experience

import com.abidria.data.common.Identifiable
import com.abidria.data.picture.Picture

data class Experience(override val id: String, val title: String,
                      val description: String, val picture: Picture?,
                      val isMine: Boolean = false, val isSaved: Boolean = false) : Identifiable
