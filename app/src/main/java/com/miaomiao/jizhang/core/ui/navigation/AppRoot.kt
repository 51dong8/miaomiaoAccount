package com.miaomiao.jizhang.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miaomiao.jizhang.feature.add.AddScreen
import com.miaomiao.jizhang.feature.cat.CatScreen
import com.miaomiao.jizhang.feature.manage.AccountManageScreen
import com.miaomiao.jizhang.feature.manage.CategoryManageScreen
import com.miaomiao.jizhang.feature.recurring.RecurringManageScreen
import com.miaomiao.jizhang.feature.settings.SettingsScreen

/** 应用导航根。 */
@Composable
fun AppRoot() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainScaffold(
                onOpenAdd = { navController.navigate(Routes.ADD) },
                onOpenCat = { navController.navigate(Routes.CAT) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                onOpenAccounts = { navController.navigate(Routes.ACCOUNTS) },
                onOpenRecurring = { navController.navigate(Routes.RECURRING) },
                onEditTransaction = { id -> navController.navigate(Routes.addEdit(id)) },
                onCopyTransaction = { id -> navController.navigate(Routes.addCopy(id)) }
            )
        }
        composable(Routes.ADD) {
            AddScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.ADD_EDIT,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) {
            AddScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.ADD_COPY,
            arguments = listOf(navArgument("copyId") { type = NavType.LongType })
        ) {
            AddScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CAT) {
            CatScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CATEGORIES) {
            CategoryManageScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ACCOUNTS) {
            AccountManageScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.RECURRING) {
            RecurringManageScreen(onBack = { navController.popBackStack() })
        }
    }
}
