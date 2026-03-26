package com.dontworry.app.ui.detail

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dontworry.app.ui.search.SearchListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThreadDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ThreadDetailUiState())
    val uiState: StateFlow<ThreadDetailUiState> = _uiState.asStateFlow()

    fun loadFromSearchItem(item: SearchListItem?) {
        if (item == null) {
            _uiState.value = ThreadDetailUiState(
                isLoading = false,
                notFoundMessage = "Thread not found."
            )
            return
        }

        val link = normalizeThreadLink(item.threadLink)
        val canOpen = link != null
        _uiState.value = ThreadDetailUiState(
            isLoading = false,
            title = item.title,
            content = item.threadContent ?: "No content available.",
            responses = item.responses,
            threadLink = link,
            canOpenLink = canOpen,
            notFoundMessage = null
        )
    }

    private fun normalizeThreadLink(rawLink: String?): String? {
        val trimmed = rawLink?.trim()?.ifEmpty { null } ?: return null
        val linkWithScheme = if (
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "https://$trimmed"
        }

        val uri = Uri.parse(linkWithScheme)
        val hasValidScheme = uri.scheme.equals("http", ignoreCase = true) ||
            uri.scheme.equals("https", ignoreCase = true)
        if (!hasValidScheme || uri.host.isNullOrBlank()) {
            return null
        }
        return uri.toString()
    }
}

class ThreadDetailViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThreadDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThreadDetailViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
