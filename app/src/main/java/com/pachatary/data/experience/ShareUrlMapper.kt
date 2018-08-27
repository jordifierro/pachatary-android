package com.pachatary.data.experience

import com.pachatary.data.common.ToDomainMapper

data class ShareUrlMapper(val shareUrl: String) : ToDomainMapper<String> {

    override fun toDomain() = shareUrl
}
