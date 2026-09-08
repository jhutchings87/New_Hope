package com.jhutchings87.jame360.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jhutchings87.jame360.data.model.ServiceType
import com.jhutchings87.jame360.ui.calendar.CalendarScreen
import com.jhutchings87.jame360.ui.dashboard.DashboardScreen
import com.jhutchings87.jame360.ui.servers.AddServerScreen
import com.jhutchings87.jame360.ui.servers.ServerListScreen

object Routes {
    const val SERVERS = "servers"
    const val CALENDAR = "calendar"
    const val DASHBOARD = "dashboard/{profileId}"
    const val SERVER_FORM = "serverForm?type={type}&profileId={profileId}"

    fun dashboard(profileId: String) = "dashboard/$profileId"
    fun serverFormNew(type: ServiceType) = "serverForm?type=${type.name}&profileId="
    fun serverFormEdit(profileId: String) = "serverForm?type=&profileId=$profileId"
}

@Composable
fun Jame360NavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.SERVERS) {

        composable(Routes.SERVERS) {
            ServerListScreen(
                onOpenDashboard = { profileId -> navController.navigate(Routes.dashboard(profileId)) },
                onOpenCalendar = { navController.navigate(Routes.CALENDAR) },
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
        ) { backStackEntry ->
            val typeArg = backStackEntry.arguments?.getString("type")
            val profileIdArg = backStackEntry.arguments?.getString("profileId")?.takeIf { it.isNotBlank() }
            AddServerScreen(
                initialType = typeArg?.takeIf { it.isNotBlank() }?.let { ServiceType.valueOf(it) },
                editingProfileId = profileIdArg,
                onDone = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.DASHBOARD,
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId").orEmpty()
            DashboardScreen(profileId = profileId, onBack = { navController.popBackStack() })
        }

        composable(Routes.CALENDAR) {
            CalendarScreen(onBack = { navController.popBackStack() })
        }
    }
}
