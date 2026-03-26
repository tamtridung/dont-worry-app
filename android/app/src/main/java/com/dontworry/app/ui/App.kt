package com.dontworry.app.ui

import android.app.Application
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dontworry.app.ui.detail.ThreadDetailScreen
import com.dontworry.app.ui.detail.ThreadDetailViewModel
import com.dontworry.app.ui.detail.ThreadDetailViewModelFactory
import com.dontworry.app.ui.detail.openThreadLink
import com.dontworry.app.ui.search.SearchScreen
import com.dontworry.app.ui.search.SearchViewModel
import com.dontworry.app.ui.search.SearchViewModelFactory

@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val context = LocalContext.current
        val application = context.applicationContext as Application
        val searchViewModel: SearchViewModel = viewModel(
            factory = SearchViewModelFactory(application)
        )

        NavHost(navController = navController, startDestination = "search") {
            composable("search") {
                SearchScreen(
                    viewModel = searchViewModel,
                    onOpenThread = { identity ->
                        navController.navigate("detail/${Uri.encode(identity)}")
                    }
                )
            }

            composable(
                route = "detail/{threadId}",
                arguments = listOf(navArgument("threadId") { type = NavType.StringType })
            ) {
                val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
                val detailViewModel: ThreadDetailViewModel = viewModel(
                    factory = ThreadDetailViewModelFactory(application)
                )
                val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
                val threadId = Uri.decode(it.arguments?.getString("threadId").orEmpty())
                val matched = (searchState.results + searchState.suggestedThreads)
                    .firstOrNull { item -> item.identity == threadId }

                LaunchedEffect(threadId, matched) {
                    detailViewModel.loadFromSearchItem(matched)
                }

                ThreadDetailScreen(
                    uiState = detailState,
                    onBack = { navController.popBackStack() },
                    onOpenLink = { link -> openThreadLink(context, link) }
                )
            }
        }
    }
}
