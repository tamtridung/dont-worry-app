package com.dontworry.app.ui.search

import com.dontworry.app.domain.model.Response

data class SearchListItem(
    val identity: String,
    val title: String,
    val excerpt: String,
    val threadLink: String?,
    val threadContent: String?,
    val responses: List<Response>
)

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val promptMessage: String? = null,
    val results: List<SearchListItem> = emptyList(),
    val currentPage: Int = 1,
    val pageSize: Int = 10
) {
    val totalPages: Int
        get() = if (results.isEmpty()) 0 else ((results.size - 1) / pageSize) + 1

    val currentPageResults: List<SearchListItem>
        get() {
            if (results.isEmpty()) return emptyList()
            val safePage = currentPage.coerceIn(1, totalPages)
            val fromIndex = (safePage - 1) * pageSize
            val toIndex = minOf(fromIndex + pageSize, results.size)
            return results.subList(fromIndex, toIndex)
        }
}
