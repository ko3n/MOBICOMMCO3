package com.mobdeve.s16.group6

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobdeve.s16.group6.ui.theme.ChoreoUITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChoreoUITheme {
                val navController = rememberNavController()

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
                            onCreateHouseholdClicked = {
                                navController.navigate("signup")
                            },
                            onJoinHouseholdClicked = {
                                navController.navigate("login")
                            }
                        )
                    }

                    composable("login") {
                        LoginScreen(
                            onBackClicked = { navController.popBackStack() },
                            onLoginClicked = { household, password ->
                                Log.d("LoginAttempt", "Household: $household, Password: $password")
                                // TODO: Handle login
                            }
                        )
                    }

                    // ADD THE NEW SIGNUP SCREEN ROUTE
                    composable("signup") {
                        SignUpScreen(
                            onBackClicked = { navController.popBackStack() },
                            onRegisterClicked = { household, email, password, confirm ->
                                // TODO: Add your registration logic here
                                Log.d("SignUpAttempt", "Household: $household, Email: $email, Pass: $password, Confirm: $confirm")
                                // If signup is successful, navigate to home screen,
                                // navController.navigate("home")
                            }
                        )
                    }
                }
            }
        }
    }
}