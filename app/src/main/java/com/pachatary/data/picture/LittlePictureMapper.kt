package com.pachatary.data.picture

import com.pachatary.data.common.ToDomainMapper

data class LittlePictureMapper(val tinyUrl: String, val smallUrl: String, val mediumUrl: String)
                                                                   : ToDomainMapper<LittlePicture> {

    override fun toDomain() = LittlePicture(tinyUrl = this.tinyUrl,
                                            smallUrl = this.smallUrl,
                                            mediumUrl = this.mediumUrl)
}
