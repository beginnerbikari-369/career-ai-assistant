package com.careerai.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.careerai.presentation.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerAINavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navigationItems = listOf(
        NavigationItem(
            title = "Home",
            icon = Icons.Default.Home,
            route = "home"
        ),
        NavigationItem(
            title = "AI Chat",
            icon = Icons.Default.AccountBox,
            route = "chat"
        ),
        NavigationItem(
            title = "Career",
            icon = Icons.Default.Person,
            route = "career"
        ),
        NavigationItem(
            title = "Routine",
            icon = Icons.Default.DateRange,
            route = "routine"
        ),
        NavigationItem(
            title = "Settings",
            icon = Icons.Default.Settings,
            route = "settings"
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                navigationItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen()
            }
            composable("chat") {
                ChatScreen()
            }
            composable("career") {
                CareerScreen()
            }
            composable("routine") {
                RoutineScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}