package com.dontworry.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dontworry.app.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onOpenThread: (threadIdentity: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshingSuggestions,
        onRefresh = viewModel::refreshSuggestedThreads
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Nhập từ khoá:") },
                singleLine = true
            )
            Button(onClick = viewModel::submitSearch) {
                Text("Tìm bài viết!")
            }
        }

        if (uiState.isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator()
                Text(text = "Loading data...")
            }
        }

        uiState.promptMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (uiState.results.isNotEmpty()) {
            Text(
                text = "Showing ${uiState.results.size} results • Page ${uiState.currentPage}/${uiState.totalPages}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (uiState.totalPages > 1) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedButton(
                        onClick = { viewModel.onPageSelected(uiState.currentPage - 1) },
                        enabled = uiState.currentPage > 1
                    ) {
                        Text("<<")
                    }
                }

                items((1..uiState.totalPages).toList()) { page ->
                    if (page == uiState.currentPage) {
                        Button(onClick = {}, enabled = false) {
                            Text(page.toString())
                        }
                    } else {
                        OutlinedButton(onClick = { viewModel.onPageSelected(page) }) {
                            Text(page.toString())
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { viewModel.onPageSelected(uiState.currentPage + 1) },
                        enabled = uiState.currentPage < uiState.totalPages
                    ) {
                        Text(">>")
                    }
                }
            }
        }

        Box(
            modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
                .pullRefresh(pullRefreshState)
        ) {
            if (uiState.results.isEmpty() && uiState.suggestedThreads.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Gợi ý:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.suggestedThreads, key = { it.identity }) { item ->
                            SearchResultRow(item = item) { selected ->
                                onOpenThread(selected.identity)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.currentPageResults, key = { it.identity }) { item ->
                        SearchResultRow(item = item) { selected ->
                            onOpenThread(selected.identity)
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isRefreshingSuggestions,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        Text(
            text = stringResource(id = R.string.donate_text),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}
