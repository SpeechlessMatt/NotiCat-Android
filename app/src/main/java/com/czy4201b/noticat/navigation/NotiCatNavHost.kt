package com.czy4201b.noticat.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.czy4201b.noticat.core.navigation.Route
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.czy4201b.noticat.features.main.MainView
import com.czy4201b.noticat.features.main.MainViewViewModel
import com.czy4201b.noticat.features.main.MainViewViewModelFactory
import com.czy4201b.noticat.NotiCatApplication
import com.czy4201b.noticat.features.edit.EditClientView
import com.czy4201b.noticat.features.edit.EditClientViewViewModelFactory
import com.czy4201b.noticat.features.globalfilters.GlobalFiltersEditView
import com.czy4201b.noticat.features.globalfilters.GlobalFiltersEditViewViewModelFactory
import com.czy4201b.noticat.features.login.LoginCard
import com.czy4201b.noticat.features.main.ReadmeWeb
import com.czy4201b.noticat.features.update.UpdateEvent
import com.czy4201b.noticat.features.update.UpdateViewModel
import com.czy4201b.noticat.features.update.ui.UpdateDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NotiCatNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val updateVm: UpdateViewModel = viewModel()
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = Route.Main.route
    ) {
        composable(
            route = Route.Main.route,
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) }
        ) {
            val context = NotiCatApplication.instance
            val factory = MainViewViewModelFactory(
                serverDao = context.database.serverDao
            )
            val viewModel: MainViewViewModel = viewModel(factory = factory)

            MainView(vm = viewModel, navController = navController)
            LaunchedEffect(Unit) {
                launch {
                    updateVm.events.collect { event ->
                        when (event) {
                            is UpdateEvent.ShowUpdateDialog -> {
                                Log.d("Update", "ShowUpdateDialog!")
                                navController.navigate(Route.UpdateDialog.route)
                            }

                            is UpdateEvent.ShowError -> {}
                        }
                    }
                }
                delay(10000)
                updateVm.checkUpdate("SpeechlessMatt", "NotiCat-Android")
            }
        }

        composable(
            route = Route.Edit.route,
            arguments = listOf(
                navArgument("client") {
                    type = NavType.StringType
                },
                navArgument("subscriptionId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400)
                )
            }
        ) { backStackEntry ->
            val client = backStackEntry.arguments?.getString("client") ?: ""
            val subscriptionId = backStackEntry.arguments?.getInt("subscriptionId") ?: -1

            val factory = EditClientViewViewModelFactory(
                client = client,
                subscriptionId = subscriptionId,
            )
            EditClientView(
                onBack = { navController.popBackStack(Route.Main.route, inclusive = false) },
                vm = viewModel(factory = factory)
            )
        }

        composable(
            route = Route.GlobalFilters.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400)
                )
            }
        ) {
            val context = NotiCatApplication.instance
            val factory = GlobalFiltersEditViewViewModelFactory(
                globalFilterDao = context.database.globalFilterDao
            )
            GlobalFiltersEditView(
                onBack = { navController.popBackStack(Route.Main.route, inclusive = false) },
                vm = viewModel(factory = factory)
            )
        }

        composable(
            route = Route.Readme.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400)
                )
            }
        ) {
            ReadmeWeb(
                onBack = { navController.popBackStack(Route.Main.route, inclusive = false) },
            )
        }

        dialog(
            route = Route.Login.route,
        ) {
            LoginCard(
                onClose = { navController.popBackStack(Route.Main.route, inclusive = false) },
                vm = viewModel()
            )
        }

        dialog(Route.UpdateDialog.route) { _ ->
            Log.d("Update", "route to dialog now")
            UpdateDialog(updateVm) {
                navController.popBackStack()
            }
        }
    }
}