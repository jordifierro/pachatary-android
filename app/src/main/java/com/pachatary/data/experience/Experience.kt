package com.pachatary.data.experience

import com.pachatary.data.common.Identifiable
import com.pachatary.data.picture.Picture

data class Experience(override val id: String, val title: String,
                      val description: String, val picture: Picture?,
                      val isMine: Boolean = false, val isSaved: Boolean = false,
                      val authorUsername: String = "", val savesCount: Int = 0) : Identifiable {

    fun builder() = Builder(id, title, description, picture,
                            isMine, isSaved, authorUsername, savesCount)

    class Builder(val id: String, val title: String,
                  val description: String, val picture: Picture?,
                  val isMine: Boolean = false, var isSaved: Boolean = false,
                  val authorUsername: String = "", val savesCount: Int = 0) {

        fun isSaved(isSaved: Boolean): Builder {
            this.isSaved = isSaved
            return this
        }

        fun build() = Experience(id, title, description, picture,
                                 isMine, isSaved, authorUsername, savesCount)
    }
}
