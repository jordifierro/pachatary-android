package com.pachatary.data.experience

import com.pachatary.data.common.Identifiable
import com.pachatary.data.picture.BigPicture
import com.pachatary.data.profile.Profile

data class Experience(override val id: String, val title: String,
                      val description: String, val picture: BigPicture? = null,
                      val isMine: Boolean = false, val isSaved: Boolean = false,
                      val authorProfile: Profile =
                              Profile(username = "", bio = "", picture = null, isMe = false),
                      val savesCount: Int = 0) : Identifiable {

    fun builder() = Builder(id, title, description, picture,
                            isMine, isSaved, authorProfile, savesCount)

    class Builder(val id: String, val title: String,
                  val description: String, val picture: BigPicture?,
                  val isMine: Boolean = false, var isSaved: Boolean = false,
                  val authorProfile: Profile, var savesCount: Int = 0) {

        fun isSaved(isSaved: Boolean): Builder {
            this.isSaved = isSaved
            return this
        }

        fun savesCount(savesCount: Int): Builder {
            this.savesCount = savesCount
            return this
        }

        fun build() = Experience(id, title, description, picture,
                                 isMine, isSaved, authorProfile, savesCount)
    }
}
