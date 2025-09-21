package com.mikufans

import Weekly
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mikufans.ui.nav.BottomNavigationItem
import com.mikufans.ui.page.Index
import com.mikufans.ui.page.Me
import com.mikufans.ui.page.Subscribe

import com.mikufans.ui.theme.MikufansTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MikufansTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController();
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    val screens = listOf(
        BottomNavigationItem.Index,
        BottomNavigationItem.Weekly,
        BottomNavigationItem.Subscribe,
        BottomNavigationItem.Me
    )
    val title = when (currentDestination) {
        BottomNavigationItem.Index.route -> "首页"
        BottomNavigationItem.Weekly.route -> "周更表"
        BottomNavigationItem.Subscribe.route -> "追番"
        BottomNavigationItem.Me.route -> "我的"
        else -> "应用标题"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(100.dp)
            ) {
                screens.forEach { screen ->
                    NavigationBarItem(

                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        selected = currentDestination == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
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
        },
        content =
            { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = BottomNavigationItem.Index.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(BottomNavigationItem.Index.route) { Index() }
                    composable(BottomNavigationItem.Weekly.route) { Weekly() }
                    composable(BottomNavigationItem.Subscribe.route) { Subscribe() }
                    composable(BottomNavigationItem.Me.route) { Me() }
                }
            }

    )
}
