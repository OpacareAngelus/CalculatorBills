package com.calculatorofthebills

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calculatorofthebills.screens.screenOne.ScreenOne
import com.calculatorofthebills.screens.screenTwo.ScreenTwo
import com.calculatorofthebills.ui.theme.CalculatorofthebillsTheme
import com.calculatorofthebills.util.KeysNavigatorDestinations

val LocalNavControllerProvider =
    staticCompositionLocalOf<NavHostController?> { null }

class CalculatorMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOrientationPortrait()
        setContent {
            CalculatorofthebillsTheme {
                val navHostController = rememberNavController()

                CompositionLocalProvider(LocalNavControllerProvider provides navHostController) {
                    NavHost(
                        navController = navHostController,
                        startDestination = KeysNavigatorDestinations.screenOne,
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None }
                    ) {
                        composable(KeysNavigatorDestinations.screenOne) {
                            ScreenOne()
                        }
                        composable(KeysNavigatorDestinations.screenTwo) {
                            ScreenTwo()
                        }
                    }
                }
            }
        }
    }

    // Set the screen orientation to portrait mode, as the app will not work correctly in landscape mode.
    // It would be better if we changed the first screen for this, because currently, the list looks too small.
    // It might be better if the screen is split into two parts.
    // The left side shows the balance, with an option to increase the balance and a button to "Add transaction".
    // The right side displays the list of transactions.
    // There is no information about that in the task, so I mocked the portrait mode.
    private fun setOrientationPortrait() {
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}