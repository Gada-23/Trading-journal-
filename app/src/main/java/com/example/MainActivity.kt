package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainContainerScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TradeDetailsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TradingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel: TradingViewModel = viewModel()

                val currentUser by viewModel.currentUser.collectAsState()
                val allTrades by viewModel.allTrades.collectAsState()
                val filteredTrades by viewModel.filteredTrades.collectAsState()
                val filterParams by viewModel.filterParams.collectAsState()
                val allNotes by viewModel.allNotes.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. SPLASH SCREEN
                    composable("splash") {
                        SplashScreen(
                            isLoggedIn = currentUser != null,
                            onNavigateNext = { route ->
                                navController.navigate(route) {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 2. AUTHENTICATION (Login / Registration / Passcode bypass)
                    composable("auth") {
                        val isLoginLoading by viewModel.isLoginLoading.collectAsState()
                        val loginError by viewModel.loginError.collectAsState()

                        AuthScreen(
                            onLogin = { email, onSuccess ->
                                viewModel.login(email, onSuccess)
                            },
                            onRegister = { email, username, balance, currency, onSuccess ->
                                viewModel.register(email, username, balance, currency, onSuccess)
                            },
                            loginError = loginError,
                            isLoading = isLoginLoading,
                            onAuthSuccess = {
                                navController.navigate("main") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 3. MAIN TRADING CONTAINER (Dashboard, Analytics, Calendar, Log lists, Reflections)
                    composable("main") {
                        currentUser?.let { user ->
                            MainContainerScreen(
                                user = user,
                                trades = allTrades,
                                filteredTrades = filteredTrades,
                                filterParams = filterParams,
                                allNotes = allNotes,
                                onFilterChange = { params ->
                                    viewModel.updateFilters(
                                        query = params.query,
                                        asset = params.asset,
                                        strategy = params.strategy,
                                        timeframe = params.timeframe,
                                        outcome = params.outcome,
                                        sortBy = params.sortBy
                                    )
                                },
                                onAddTrade = { title, asset, isBuy, entry, sl, tp, exit, size, risk, pnl, comp, strat, tf, cond, mood, mistake, lesson, notes, fav, tags, imgs ->
                                    viewModel.insertTrade(
                                        title, asset, isBuy, entry, sl, tp, exit, size, risk, pnl, comp, strat, tf, cond, mood, mistake, lesson, notes, fav, tags, imgs
                                    )
                                },
                                onDeleteTrade = { trade ->
                                    viewModel.deleteTrade(trade)
                                },
                                onSelectTrade = { trade ->
                                    viewModel.selectTrade(trade)
                                },
                                onAddNote = { type, title, content, mood, date ->
                                    viewModel.insertNote(type, title, content, mood, date)
                                },
                                onDeleteNote = { note ->
                                    viewModel.deleteNote(note)
                                },
                                onUpdateProfile = { username, currency ->
                                    viewModel.updateProfile(username, currency)
                                },
                                onResetBalance = { d ->
                                    viewModel.updateProfile(currentUser?.username ?: "DemoTrader", currentUser?.currency ?: "USD")
                                    // Set hardcoded state
                                },
                                onLogout = {
                                    viewModel.logout {
                                        navController.navigate("auth") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    }
                                },
                                onNavigateToDetails = {
                                    navController.navigate("trade_details")
                                }
                            )
                        } ?: run {
                            // Safe placeholder during logout transition
                            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize())
                        }
                    }

                    // 4. DETAILED TRADE ANALYTICS (Pinch, Zoom, Timeline, and Gemini audits)
                    composable("trade_details") {
                        val selectedTrade by viewModel.selectedTrade.collectAsState()
                        val aiCritique by viewModel.aiCritique.collectAsState()
                        val isAiLoading by viewModel.isAiLoading.collectAsState()

                        selectedTrade?.let { trade ->
                            TradeDetailsScreen(
                                trade = trade,
                                aiCritique = aiCritique,
                                isAiLoading = isAiLoading,
                                onBack = { navController.popBackStack() },
                                onToggleFavorite = { t ->
                                    viewModel.updateTrade(t.copy(isFavorite = !t.isFavorite))
                                },
                                onDeleteTrade = { t ->
                                    viewModel.deleteTrade(t)
                                    navController.popBackStack()
                                }
                            )
                        } ?: run {
                            Text("Oops, trade detail could not be loaded!")
                        }
                    }
                }
            }
        }
    }
}
