package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditDocumentScreen
import com.example.ui.screens.ViewDocumentScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: DocumentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard"
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToView = { docId ->
                                    navController.navigate("view/$docId")
                                },
                                onNavigateToCreate = { folderId ->
                                    navController.navigate("edit/-1/$folderId")
                                }
                            )
                        }
                        composable(
                            route = "view/{docId}",
                            arguments = listOf(navArgument("docId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val docId = backStackEntry.arguments?.getInt("docId") ?: -1
                            ViewDocumentScreen(
                                docId = docId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToEdit = { id ->
                                    navController.navigate("edit/$id/-1")
                                }
                            )
                        }
                        composable(
                            route = "edit/{docId}/{folderId}",
                            arguments = listOf(
                                navArgument("docId") { type = NavType.IntType },
                                navArgument("folderId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val docId = backStackEntry.arguments?.getInt("docId") ?: -1
                            val folderId = backStackEntry.arguments?.getInt("folderId") ?: 1
                            EditDocumentScreen(
                                docId = docId,
                                folderId = folderId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onSaveSuccess = { savedDocId ->
                                    // Smooth pop to maintain state flow
                                    navController.popBackStack()
                                    if (docId == -1) {
                                        navController.navigate("view/$savedDocId")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
