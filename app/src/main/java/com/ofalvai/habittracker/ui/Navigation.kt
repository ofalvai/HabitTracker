/*
 * Copyright 2022 Olivér Falvai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalAnimationApi::class)

package com.ofalvai.habittracker.ui

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.navigation.*
import com.google.accompanist.navigation.animation.composable
import com.ofalvai.habittracker.core.ui.theme.AppTransition

sealed class Screen constructor(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList(),
    val enterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition = { AppTransition.defaultEnter },
    val exitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition = { AppTransition.defaultExit },
    val popEnterTransition: AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition = enterTransition,
    val popExitTransition: AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition = exitTransition
)

fun NavGraphBuilder.appDestination(
    screen: Screen,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = screen.route,
        arguments = screen.arguments,
        enterTransition = screen.enterTransition,
        exitTransition = screen.exitTransition,
        popEnterTransition = screen.popEnterTransition,
        popExitTransition = screen.popExitTransition,
        content = content
    )
}

fun NavController.navigate(destination: Screen) {
    navigate(destination.route)
}

object Destination {
    object Dashboard : Screen(
        route = "dashboard",
        enterTransition = {
            when (initialState.destination.route) {
                Insights.route -> AppTransition.fadeThroughEnter
                Settings.route, Archive.route, AddHabit.route, HabitDetails.route -> AppTransition.sharedZAxisEnterBackward
                else -> AppTransition.defaultEnter
            }
        },
        exitTransition = {
            when (targetState.destination.route) {
                Insights.route -> AppTransition.fadeThroughExit
                Settings.route, Archive.route, HabitDetails.route -> AppTransition.sharedZAxisExitForward
                else -> AppTransition.defaultExit
            }
        }
    )

    object Insights : Screen(
        route = "insights",
        enterTransition = {
            when (initialState.destination.route) {
                Dashboard.route -> AppTransition.fadeThroughEnter
                Settings.route, Archive.route, HabitDetails.route -> AppTransition.sharedZAxisEnterBackward
                else -> AppTransition.defaultEnter
            }
        },
        exitTransition = {
            when (targetState.destination.route) {
                Dashboard.route -> AppTransition.fadeThroughExit
                Settings.route, Archive.route, HabitDetails.route -> AppTransition.sharedZAxisExitForward
                else -> AppTransition.defaultExit
            }
        }
    )

    object AddHabit : Screen(
        route = "add_habit",
        enterTransition = { AppTransition.sharedZAxisEnterForward },
        exitTransition = { AppTransition.sharedZAxisExitBackward }
    )

    object Settings : Screen(
        route = "settings",
        enterTransition = {
            when (initialState.destination.route) {
                Dashboard.route, Insights.route -> AppTransition.sharedZAxisEnterForward
                else -> AppTransition.defaultEnter
            }
        },
        exitTransition = {
            when (targetState.destination.route) {
                Dashboard.route, Insights.route -> AppTransition.sharedZAxisExitBackward
                else -> AppTransition.defaultExit
            }
        }
    )

    object HabitDetails : Screen(
        route = "habit_details/{id}",
        arguments = listOf(navArgument("id") {
            type = NavType.IntType
        }),
        enterTransition = { AppTransition.sharedZAxisEnterForward },
        exitTransition = { AppTransition.sharedZAxisExitBackward }
    ) {
        fun idFrom(arguments: Bundle?): Int {
            return arguments?.getInt("id")!!
        }

        fun buildRoute(habitId: Int) = "habit_details/$habitId"
    }

    object Licenses : Screen(
        route = "licenses"
    )

    object Archive : Screen(
        route = "archive",
        enterTransition = { AppTransition.sharedZAxisEnterForward },
        exitTransition = { AppTransition.sharedZAxisExitBackward }
    )

    object Export : Screen(
        route = "export",
        enterTransition = { AppTransition.sharedZAxisEnterForward },
        exitTransition = { AppTransition.sharedZAxisExitBackward }
    )
}
