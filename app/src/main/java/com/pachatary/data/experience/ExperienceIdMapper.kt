package com.pachatary.data.experience

import com.pachatary.data.common.ToDomainMapper
import com.pachatary.data.picture.PictureMapper

data class ExperienceIdMapper(val experienceId: String) : ToDomainMapper<String> {

    override fun toDomain() = experienceId
}
