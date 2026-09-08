package com.jhutchings87.jame360.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jhutchings87.jame360.data.model.ServiceType
import com.jhutchings87.jame360.ui.calendar.CalendarScreen
import com.jhutchings87.jame360.ui.downloads.DownloadsScreen
import com.jhutchings87.jame360.ui.search.SearchScreen
import com.jhutchings87.jame360.ui.servers.AddServerScreen
import com.jhutchings87.jame360.ui.servers.ServerListScreen

object Routes {
    const val DOWNLOADS = "downloads"
    const val SEARCH = "search"
    const val UPCOMING = "upcoming"
    const val SERVERS = "servers"
    const val SERVER_FORM = "serverForm?type={type}&profileId={profileId}"

    fun serverFormNew(type: ServiceType) = "serverForm?type=${type.name}&profileId="
    fun serverFormEdit(profileId: String) = "serverForm?type=&profileId=$profileId"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.DOWNLOADS, "Downloads", Icons.Default.Download),
    Tab(Routes.SEARCH, "Search", Icons.Default.Search),
    Tab(Routes.UPCOMING, "Upcoming", Icons.Default.CalendarMonth),
    Tab(Routes.SERVERS, "Servers", Icons.Default.Dns)
)

@Composable
fun Jame360NavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        // Keep a single tab on the back stack rather than
                                        // stacking every switch, and restore each tab's
                                        // scroll/state when returning to it.
                                        popUpTo(Routes.DOWNLOADS) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DOWNLOADS,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            composable(Routes.DOWNLOADS) { DownloadsScreen() }

            composable(Routes.SEARCH) { SearchScreen() }

            composable(Routes.UPCOMING) { CalendarScreen() }

            composable(Routes.SERVERS) {
                ServerListScreen(
                    onAddServer = { type -> navController.navigate(Routes.serverFormNew(type)) },
                    onEditServer = { profileId -> navController.navigate(Routes.serverFormEdit(profileId)) }
                )
            }

            composable(
                route = Routes.SERVER_FORM,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("profileId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { entry ->
                val typeArg = entry.arguments?.getString("type")?.takeIf { it.isNotBlank() }
                val profileIdArg = entry.arguments?.getString("profileId")?.takeIf { it.isNotBlank() }
                AddServerScreen(
                    initialType = typeArg?.let { ServiceType.valueOf(it) },
                    editingProfileId = profileIdArg,
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}
