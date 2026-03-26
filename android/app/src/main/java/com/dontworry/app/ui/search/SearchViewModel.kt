package com.dontworry.app.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dontworry.app.data.repo.ThreadRepository
import com.dontworry.app.data.repo.SynonymsRepository
import com.dontworry.app.domain.search.SearchService
import com.dontworry.app.domain.thread.ThreadIdentity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val MAX_RESULTS = 100
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchService: SearchService? = null

    init {
        loadData()
    }

    fun onQueryChanged(newValue: String) {
        _uiState.value = _uiState.value.copy(
            query = newValue,
            promptMessage = null,
            currentPage = 1
        )
    }

    fun onPageSelected(page: Int) {
        val state = _uiState.value
        if (state.totalPages == 0) return
        val safePage = page.coerceIn(1, state.totalPages)
        if (safePage != state.currentPage) {
            _uiState.value = state.copy(currentPage = safePage)
        }
    }

    fun submitSearch() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(promptMessage = "Please enter a keyword.")
            return
        }

        val service = searchService ?: run {
            _uiState.value = _uiState.value.copy(promptMessage = "Dataset is still loading.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch(Dispatchers.Default) {
            val results = service.search(query, limit = MAX_RESULTS).map {
                SearchListItem(
                    identity = ThreadIdentity.fromParts(
                        threadId = it.thread.threadId,
                        threadLink = it.thread.threadLink,
                        threadTitle = it.thread.threadTitle
                    ),
                    title = it.thread.threadTitle,
                    excerpt = it.excerpt,
                    threadLink = it.thread.threadLink,
                    threadContent = it.thread.threadContent,
                    responses = it.thread.responses
                )
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                promptMessage = if (results.isEmpty()) "No results found." else null,
                results = results,
                currentPage = 1
            )
        }
    }

    private fun loadData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch(Dispatchers.Default) {
            val threads = ThreadRepository(getApplication()).loadThreads()
            val synonymsResult = SynonymsRepository(getApplication()).load()
            searchService = SearchService(threads, synonymsResult.groups)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                promptMessage = if (synonymsResult.enabled) {
                    "Type a keyword to search."
                } else {
                    "Type a keyword to search. (Synonyms disabled)"
                }
            )
        }
    }
}

class SearchViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
