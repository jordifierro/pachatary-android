package com.pachatary.data.common

data class PaginatedListMapper<T, U: ToDomainMapper<T>>(val results: List<U>,
                                  val nextUrl: String)