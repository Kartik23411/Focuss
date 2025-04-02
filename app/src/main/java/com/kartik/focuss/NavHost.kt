package com.kartik.focuss

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kartik.focuss.presentation.ui.screen.MainScreen
import com.kartik.focuss.presentation.ui.screen.StartScreen

@Composable
fun FocussNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "start"
    ) {
        composable(
            "start",
            enterTransition = {
                fadeIn(
                    tween(durationMillis = 500, easing = LinearOutSlowInEasing)
                )
            },
            exitTransition = {
                fadeOut(
                    tween(durationMillis = 500, easing = FastOutLinearInEasing)
                )
            }
        ) {
            StartScreen(
                modifier = modifier,
                navigateToNext = { navController.navigate("main") }
            )
        }

        composable(
            "main"
        ) {
            MainScreen({ onStart() }, { onStop() }, modifier)
        }
    }
}