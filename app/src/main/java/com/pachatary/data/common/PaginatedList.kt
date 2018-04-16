package com.pachatary.data.common

data class PaginatedList<T>(val results: List<T>,
                            val nextUrl: String)