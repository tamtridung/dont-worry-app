package com.dontworry.app.ui.detail

import com.dontworry.app.domain.model.Response

data class ThreadDetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val content: String = "",
    val responses: List<Response> = emptyList(),
    val threadLink: String? = null,
    val canOpenLink: Boolean = false,
    val notFoundMessage: String? = null
)
