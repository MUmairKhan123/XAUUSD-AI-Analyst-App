package com.xauusd.analyst

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xauusd.analyst.ui.DashboardScreen
import com.xauusd.analyst.ui.DetailScreen
import com.xauusd.analyst.ui.Gold
import com.xauusd.analyst.ui.HistoryScreen
import com.xauusd.analyst.ui.Navy
import com.xauusd.analyst.ui.NavyCard
import com.xauusd.analyst.ui.PromptScreen
import com.xauusd.analyst.ui.ResultScreen
import com.xauusd.analyst.ui.SettingsScreen
import com.xauusd.analyst.ui.TextMuted
import com.xauusd.analyst.ui.TextPrimary
import com.xauusd.analyst.ui.UploadScreen
import com.xauusd.analyst.ui.XauTheme

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("dashboard", "Home", Icons.Default.Home),
    Tab("upload", "Charts", Icons.Default.Add),
    Tab("prompt", "Prompt", Icons.Default.Create),
    Tab("result", "Result", Icons.Default.CheckCircle),
    Tab("history", "History", Icons.Default.DateRange)
)

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { XauTheme { AppRoot(vm) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot(vm: MainViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "dashboard"
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.messages.collect { snack.showSnackbar(it) } }

    fun go(r: String) {
        nav.navigate(r) {
            popUpTo("dashboard") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val title = when {
        route == "dashboard" -> "XAUUSD AI Analyst"
        route == "upload" -> "Upload Charts"
        route == "prompt" -> "ChatGPT Prompt"
        route == "result" -> "Analysis Result"
        route == "history" -> "History"
        route == "settings" -> "Settings"
        else -> "Analysis"
    }
    val showBack = route == "settings" || route.startsWith("detail")

    Scaffold(
        containerColor = Navy,
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (showBack) IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!showBack) IconButton(onClick = { go("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy, titleContentColor = Gold,
                    actionIconContentColor = Gold, navigationIconContentColor = Gold
                )
            )
        },
        bottomBar = {
            if (!route.startsWith("detail")) {
                NavigationBar(containerColor = NavyCard) {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = { go(t.route) },
                            icon = { Icon(t.icon, contentDescription = t.label) },
                            label = { Text(t.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Navy, selectedTextColor = Gold,
                                indicatorColor = Gold,
                                unselectedIconColor = TextMuted, unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        }
    ) { inner ->
        NavHost(nav, startDestination = "dashboard", modifier = Modifier.padding(inner)) {
            composable("dashboard") { DashboardScreen(vm) { r -> if (r.startsWith("detail")) nav.navigate(r) else go(r) } }
            composable("upload") { UploadScreen(vm) { go("prompt") } }
            composable("prompt") { PromptScreen(vm) { go("result") } }
            composable("result") { ResultScreen(vm, onSaved = { go("history") }, onSettings = { go("settings") }) }
            composable("history") { HistoryScreen(vm) { id -> nav.navigate("detail/$id") } }
            composable("settings") { SettingsScreen(vm) }
            composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { be ->
                DetailScreen(vm, be.arguments?.getLong("id") ?: -1L) { nav.popBackStack() }
            }
        }
    }
}
