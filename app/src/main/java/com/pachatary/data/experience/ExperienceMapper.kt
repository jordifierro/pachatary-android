package com.pachatary.data.experience

import com.pachatary.data.common.ToDomainMapper
import com.pachatary.data.picture.PictureMapper

data class ExperienceMapper(val id: String, val title: String, val description: String, val picture: PictureMapper?,
                            val isMine: Boolean, val isSaved: Boolean)
    : ToDomainMapper<Experience> {

    override fun toDomain() = Experience(id = this.id, title = this.title, description = this.description,
                    picture = this.picture?.toDomain(), isMine = this.isMine, isSaved = this.isSaved)
}