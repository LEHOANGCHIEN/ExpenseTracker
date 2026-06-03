package com.expensetracker.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.expensetracker.app.feature.auth.screen.LoginScreen
import com.expensetracker.app.feature.auth.screen.RegisterScreen

@Composable
fun AuthNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Login,
        enterTransition = { slideInHorizontally { it / 3 } + fadeIn() },
        exitTransition = { slideOutHorizontally { -it / 3 } + fadeOut() },
        popEnterTransition = { slideInHorizontally { -it / 3 } + fadeIn() },
        popExitTransition = { slideOutHorizontally { it / 3 } + fadeOut() },
    ) {
        composable<Login> {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Register) },
            )
        }
        composable<Register> {
            RegisterScreen(
                onNavigateToLogin = { navController.navigateUp() },
            )
        }
    }
}
