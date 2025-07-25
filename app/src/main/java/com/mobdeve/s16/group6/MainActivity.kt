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
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.mobdeve.s16.group6.ui.theme.ChoreoUITheme

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val peopleViewModel: PeopleViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()

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
                            authViewModel.clearLoginError()
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    authViewModel.signupErrorMessage.collect { message ->
                        message?.let {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            authViewModel.clearSignupError()
                        }
                    }
                }

                val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
                val currentHousehold by authViewModel.currentHousehold.collectAsState()

                LaunchedEffect(isAuthenticated, currentHousehold) {
                    if (isAuthenticated && currentHousehold != null) {
                        currentHousehold?.let { household ->
                            peopleViewModel.setHousehold(household.name, household.email)
                        }
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
                        LoginScreen(
                            onBackClicked = { navController.popBackStack() },
                            onLoginClicked = { householdName, password ->
                                authViewModel.login(householdName, password) { success ->
                                    // Logic handled by LaunchedEffect
                                }
                            },

                            )
                    }
                    composable("signup") {
                        SignUpScreen(
                            onBackClicked = { navController.popBackStack() },
                            onRegisterClicked = { householdName, email, password, confirm ->
                                if (password != confirm) {
                                    authViewModel.setSignupError("Passwords do not match")
                                    return@SignUpScreen
                                }
                                authViewModel.register(householdName, email, password) { success ->
                                    // Logic handled by LaunchedEffect
                                }
                            }
                        )
                    }
                    composable("home") {
                        PeopleTab(
                            peopleViewModel = peopleViewModel,
                            authViewModel = authViewModel,
                            navController = navController
                        )
                    }

                    composable(
                        "tasks/{personId}/{personName}/{householdName}/{householdEmail}",
                        arguments = listOf(
                            navArgument("personId") { type = NavType.IntType }, // Changed to IntType
                            navArgument("personName") { type = NavType.StringType },
                            navArgument("householdName") { type = NavType.StringType },
                            navArgument("householdEmail") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val tasks by taskViewModel.tasks.collectAsState()
                        val householdMembers by taskViewModel.householdMembers.collectAsState()

                        val personId = backStackEntry.arguments?.getInt("personId") // Changed to getInt
                        val personName = backStackEntry.arguments?.getString("personName")
                        val householdName = backStackEntry.arguments?.getString("householdName")
                        val householdEmail = backStackEntry.arguments?.getString("householdEmail")

                        if (personId != null && personName != null && householdName != null && householdEmail != null) {
                            LaunchedEffect(personId, householdName, householdEmail) {
                                taskViewModel.initialize(personId, householdName, householdEmail)
                            }

                            TaskScreen(
                                personName = personName,
                                tasks = tasks,
                                householdMembers = householdMembers,
                                onBackClicked = { navController.popBackStack() },
                                onAddTask = { task ->
                                    taskViewModel.addTask(
                                        title = task.title,
                                        description = task.description,
                                        dueDateMillis = task.dueDateMillis,
                                        priority = task.priority,
                                        assigneeId = task.assigneeId,
                                        isRecurring = task.isRecurring,
                                        recurringInterval = task.recurringInterval
                                    )
                                },
                                onUpdateTask = { taskViewModel.updateTask(it) },
                                onDeleteTask = { taskViewModel.deleteTask(it) }
                            )
                        } else {
                            Text("Error: Task data missing", color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}