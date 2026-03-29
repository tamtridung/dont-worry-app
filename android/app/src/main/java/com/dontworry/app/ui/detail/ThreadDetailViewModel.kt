package com.dontworry.app.ui.detail

import android.app.Application
import android.util.Patterns
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

        val link = item.threadLink?.trim()?.ifEmpty { null }
        val canOpen = link != null && Patterns.WEB_URL.matcher(link).matches()
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
