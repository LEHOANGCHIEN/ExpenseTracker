package com.expensetracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expensetracker.app.core.designsystem.theme.ExpenseTrackerTheme
import com.expensetracker.app.domain.repository.PreferencesRepository
import com.expensetracker.app.feature.shell.AppShell
import com.expensetracker.app.navigation.Home
import com.expensetracker.app.navigation.Onboarding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefsState = preferencesRepository.preferences.collectAsStateWithLifecycle(
                initialValue = null,
            )
            val prefs = prefsState.value
            // Wait for prefs to load; splash screen covers the blank frame
            if (prefs != null) {
                ExpenseTrackerTheme(
                    themeMode = prefs.themeMode,
                    dynamicColor = prefs.dynamicColorEnabled,
                ) {
                    val startDestination: Any = if (prefs.hasCompletedOnboarding) Home else Onboarding
                    AppShell(startDestination = startDestination)
                }
            }
        }
    }
}
