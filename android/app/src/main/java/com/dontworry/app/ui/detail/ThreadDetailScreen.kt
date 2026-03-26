package com.dontworry.app.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ThreadDetailScreen(
    uiState: ThreadDetailUiState,
    onBack: () -> Unit,
    onOpenLink: (String) -> Unit
) {
    if (uiState.isLoading) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator()
            Text(text = "Loading thread...")
        }
        return
    }

    uiState.notFoundMessage?.let { message ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = message)
            Button(onClick = onBack) { Text("Back") }
        }
        return
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val questionMaxHeight = maxHeight * 0.35f

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = uiState.title, style = MaterialTheme.typography.titleLarge)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = questionMaxHeight)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = uiState.content, style = MaterialTheme.typography.bodyMedium)
            }

            if (uiState.canOpenLink) {
                Text(
                    text = uiState.threadLink.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        onOpenLink(uiState.threadLink.orEmpty())
                    }
                )
            }

            Text(text = "Nội dung trò chuyện:", style = MaterialTheme.typography.titleMedium)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.responses) { response ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = response.responser.ifBlank { "Anonymous" },
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(text = response.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Quay lại")
            }
        }
    }
}
