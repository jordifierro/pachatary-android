package com.pachatary.data.profile

import com.pachatary.data.common.ToDomainMapper
import com.pachatary.data.picture.LittlePictureMapper

data class ProfileMapper(val username: String, val bio: String,
                         val picture: LittlePictureMapper?, val isMe: Boolean)
                                                                         : ToDomainMapper<Profile> {

    override fun toDomain() = Profile(username = this.username, bio = this.bio,
                                      picture = this.picture?.toDomain(), isMe = this.isMe)
}
