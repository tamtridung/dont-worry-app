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
    val results: List<SearchListItem> = emptyList()
)
