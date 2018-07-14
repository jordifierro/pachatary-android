package com.pachatary.data.profile

import com.pachatary.data.common.ToDomainMapper
import com.pachatary.data.picture.LittlePicture

data class ProfileMapper(val username: String, val bio: String,
                         val picture: LittlePicture?, val isMe: Boolean) : ToDomainMapper<Profile> {

    override fun toDomain() = Profile(username = this.username, bio = this.bio,
                                      picture = this.picture, isMe = this.isMe)
}
