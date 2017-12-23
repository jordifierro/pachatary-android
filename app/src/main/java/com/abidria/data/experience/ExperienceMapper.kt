package com.abidria.data.experience

import com.abidria.data.picture.PictureMapper
import com.abidria.data.scene.SceneMapper

data class ExperienceMapper(val id: String, val title: String, val description: String, val picture: PictureMapper?) {

    fun toDomain(isMine: Boolean = false, isSaved: Boolean = false): Experience =
            Experience(id = this.id, title = this.title, description = this.description,
                    picture = this.picture?.toDomain(), isMine = isMine, isSaved = isSaved)
}
