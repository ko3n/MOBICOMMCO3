package com.mobdeve.s16.group6

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobdeve.s16.group6.ui.theme.ChoreoUITheme
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.mobdeve.s16.group6.utils.NotificationUtils
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val peopleViewModel: PeopleViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                NotificationUtils.showTaskNotification(
                    this,
                    "Notifications enabled!",
                    "You'll now receive task reminders!",
                    0
                )
            } else {
                Toast.makeText(
                    this,
                    "Notifications are disabled, enable them in your device settings to receive task reminders.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()
        createNotificationChannel()

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
                val personToEdit by peopleViewModel.personToEdit.collectAsState()

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
                                authViewModel.login(householdName, password) { success -> }
                            }
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
                                authViewModel.register(householdName, email, password) { success -> }
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

                    // Per-person Tasks
                    composable(
                        "tasks/{personId}/{firebaseId}/{personName}/{householdName}/{householdEmail}",
                        arguments = listOf(
                            navArgument("personId") { type = NavType.IntType },
                            navArgument("firebaseId") { type = NavType.StringType },
                            navArgument("personName") { type = NavType.StringType },
                            navArgument("householdName") { type = NavType.StringType },
                            navArgument("householdEmail") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val personId = backStackEntry.arguments?.getInt("personId")
                        val firebaseId = backStackEntry.arguments?.getString("firebaseId")
                        val encodedPersonName = backStackEntry.arguments?.getString("personName")
                        val householdName = backStackEntry.arguments?.getString("householdName")
                        val householdEmail = backStackEntry.arguments?.getString("householdEmail")

                        if (personId != null && firebaseId != null && encodedPersonName != null && householdName != null && householdEmail != null) {
                            val personName = URLDecoder.decode(encodedPersonName, StandardCharsets.UTF_8.toString())
                            val displayedPersonName = personToEdit?.takeIf { it.firebaseId == firebaseId }?.name ?: personName

                            // Initialize tasks for the person
                            LaunchedEffect(personId, householdName, householdEmail) {
                                taskViewModel.initialize(personId, householdName, householdEmail)
                            }

                            TaskScreen(
                                personName = displayedPersonName,
                                onSettingsClicked = {
                                    navController.navigate("settings/$firebaseId")
                                },
                                tasks = taskViewModel.tasks.collectAsState().value,
                                householdMembers = peopleViewModel.people.collectAsState().value,
                                onBackClicked = { navController.popBackStack() },
                                onAddTask = { task ->
                                    taskViewModel.addTask(
                                        title = task.title, description = task.description, dueDateMillis = task.dueDateMillis,
                                        priority = task.priority, assigneeId = task.assigneeId,
                                        isRecurring = task.isRecurring, recurringInterval = task.recurringInterval
                                    )
                                },
                                onUpdateTask = { taskViewModel.updateTask(it) },
                                onDeleteTask = { taskViewModel.deleteTask(it) },
                                taskViewModel = taskViewModel
                            )
                        } else {
                            Text("Error: Person data missing.")
                        }
                    }

                    // Settings per person
                    composable(
                        "settings/{firebaseId}",
                        arguments = listOf(navArgument("firebaseId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val firebaseId = backStackEntry.arguments?.getString("firebaseId")
                        if (firebaseId != null) {
                            SettingsScreen(
                                personFirebaseId = firebaseId,
                                peopleViewModel = peopleViewModel,
                                onBackClicked = { navController.popBackStack() },
                                onProfileClicked = { navController.navigate("editProfile") },
                                onLogoutClicked = {
                                    authViewModel.logout()
                                    navController.navigate("setup") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                },
                                onCompletedTasksClicked = {
                                    navController.navigate("completedtasks")
                                }
                            )
                        }
                    }

                    // Global household settings (optional: add a "settings" route with no arguments if needed)
                    composable("settings") {
                        SettingsScreen(
                            personFirebaseId = null,
                            peopleViewModel = peopleViewModel,
                            onBackClicked = { navController.popBackStack() },
                            onProfileClicked = { navController.navigate("editProfile") },
                            onLogoutClicked = {
                                authViewModel.logout()
                                navController.navigate("setup") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            },
                            onCompletedTasksClicked = {
                                navController.navigate("completedtasks")
                            }
                        )
                    }

                    // Edit Profile
                    composable("editProfile") {
                        val context = LocalContext.current
                        EditProfileScreen(
                            currentUser = personToEdit,
                            onBackClicked = { navController.popBackStack() },
                            onSaveClicked = { newName, onSaveSuccess ->
                                peopleViewModel.updateUserProfile(newName)
                                Toast.makeText(context, "Successfully Edited!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                                onSaveSuccess()
                            }
                        )
                    }

                    // Completed Tasks (household-wide)
                    composable("completedtasks") {
                        val currentHousehold by authViewModel.currentHousehold.collectAsState()
                        val allTasks by taskViewModel.allHouseholdTasks.collectAsState()
                        val householdMembers by taskViewModel.householdMembers.collectAsState()

                        // Initialize household-wide tasks/members when entering this screen
                        LaunchedEffect(currentHousehold) {
                            currentHousehold?.let {
                                taskViewModel.initializeForHousehold(it.name, it.email)
                            }
                        }

                        CompletedTasksScreen(
                            personName = currentHousehold?.name ?: "Household",
                            tasks = allTasks,
                            householdMembers = householdMembers,
                            onBackClicked = { navController.popBackStack() },
                            onSettingsClicked = { navController.navigate("settings") }
                        )
                    }
                }
            }
        }
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelId = "choreo_task_reminders"
            val name = "Task Reminders"
            val descriptionText = "notifies you about upcoming household tasks"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply{
                description = descriptionText
            }

            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}