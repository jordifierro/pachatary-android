package com.pachatary.data.profile

import com.pachatary.data.picture.LittlePicture

data class Profile(val username: String, val bio: String,
                   val picture: LittlePicture?, val isMe: Boolean)