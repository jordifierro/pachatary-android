package com.pachatary.data.experience

import com.pachatary.data.common.ToDomainMapper

data class ExperienceIdMapper(val experienceId: String) : ToDomainMapper<String> {

    override fun toDomain() = experienceId
}
