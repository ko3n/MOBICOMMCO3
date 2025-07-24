package com.mobdeve.s16.group6

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobdeve.s16.group6.ui.theme.ChoreoUITheme


class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChoreoUITheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    authViewModel.loginErrorMessage.collect { message ->
                        message?.let {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    authViewModel.signupErrorMessage.collect { message ->
                        message?.let {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
                LaunchedEffect(isAuthenticated) {
                    if (isAuthenticated) {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }

                val navigateToSetup: () -> Unit = {
                    navController.navigate("setup") {
                        popUpTo("onboarding1") { inclusive = true }
                    }
                }

                NavHost(navController = navController, startDestination = "onboarding1") {
                    composable("onboarding1") {
                        Onboarding1(
                            onContinueClicked = { navController.navigate("onboarding2") },
                            onSkipClicked = navigateToSetup
                        )
                    }
                    composable("onboarding2") {
                        Onboarding2(
                            onBackClicked = { navController.popBackStack() },
                            onContinueClicked = { navController.navigate("onboarding3") },
                            onSkipClicked = navigateToSetup
                        )
                    }
                    composable("onboarding3") {
                        Onboarding3(
                            onBackClicked = { navController.popBackStack() },
                            onGetStartedClicked = navigateToSetup,
                            onSkipClicked = navigateToSetup
                        )
                    }
                    composable("setup") {
                        SetupScreen(
                            onCreateHouseholdClicked = { navController.navigate("signup") },
                            onJoinHouseholdClicked = { navController.navigate("login") }
                        )
                    }
                    composable("login") {
                        // Pass the showErrorToast lambda
                        LoginScreen(
                            onBackClicked = { navController.popBackStack() },
                            onLoginClicked = { household, password ->
                                authViewModel.login(household, password) { success ->

                                }
                            },
                            showErrorToast = { message ->

                            }
                        )

                    }
                    composable("signup") {
                        SignUpScreen(
                            onBackClicked = { navController.popBackStack() },
                            onRegisterClicked = { household, email, password, confirm ->
                                if (password != confirm) {
                                    authViewModel.setSignupError("Passwords do not match")
                                    return@SignUpScreen
                                }
                                authViewModel.register(household, email, password) { success ->

                                }
                            }
                        )

                    }
                    composable("home") {
                        PeopleTab()
                    }
                }
            }
        }
    }
}