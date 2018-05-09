package com.pachatary.data.auth

import com.pachatary.data.common.ToDomainMapper

data class PairPersonAuthTokenMapper(val person: PersonMapper, val authToken: AuthTokenMapper)
                                                        : ToDomainMapper<Pair<Person, AuthToken>> {

    override fun toDomain() = Pair(person.toDomain(), authToken.toDomain())
}
