package com.pachatary.data.picture

import com.pachatary.data.common.ToDomainMapper

data class BigPictureMapper(val smallUrl: String, val mediumUrl: String, val largeUrl: String)
                                                                      : ToDomainMapper<BigPicture> {

    override fun toDomain() = BigPicture(smallUrl = this.smallUrl,
                                         mediumUrl = this.mediumUrl,
                                         largeUrl = this.largeUrl)
}
