package com.pachatary.data.experience

import com.pachatary.data.common.ToDomainMapper
import com.pachatary.data.picture.BigPictureMapper
import com.pachatary.data.profile.ProfileMapper

data class ExperienceMapper(val id: String, val title: String, val description: String,
                            val picture: BigPictureMapper?,
                            val isMine: Boolean, val isSaved: Boolean,
                            val authorProfile: ProfileMapper, val savesCount: Int)
                                                                      : ToDomainMapper<Experience> {

    override fun toDomain() = Experience(id = this.id, title = this.title,
            description = this.description, picture = this.picture?.toDomain(),
            isMine = this.isMine, isSaved = this.isSaved,
            authorProfile = this.authorProfile.toDomain(),
            savesCount = this.savesCount)
}
