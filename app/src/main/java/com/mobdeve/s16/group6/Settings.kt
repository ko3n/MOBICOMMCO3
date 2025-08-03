package com.mobdeve.s16.group6

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobdeve.s16.group6.ui.theme.AppDarkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    personFirebaseId: String?,
    peopleViewModel: PeopleViewModel,
    onBackClicked: () -> Unit,
    onProfileClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    onCompletedTasksClicked: () -> Unit // Added for household-wide completed tasks
) {
    // If editing a specific person, select them in the viewmodel
    LaunchedEffect(key1 = personFirebaseId) {
        if (!personFirebaseId.isNullOrEmpty()) {
            peopleViewModel.selectPersonToEdit(personFirebaseId)
        }
    }

    var notificationsOn by remember { mutableStateOf(true) }
    var addTasksOnTop by remember { mutableStateOf(true) }
    var remindersOn by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings", fontWeight = FontWeight.Bold, color = AppDarkBlue) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            SettingClickableItem(text = "Profile", icon = Icons.Default.Person, onClick = onProfileClicked)
            HorizontalDivider()
            SettingSwitchItem(text = "Notifications", checked = notificationsOn, onCheckedChange = { notificationsOn = it })
            HorizontalDivider()
            SettingSwitchItem(text = "Add new tasks on top", checked = addTasksOnTop, onCheckedChange = { addTasksOnTop = it })
            HorizontalDivider()
            SettingSwitchItem(text = "Reminders", checked = remindersOn, onCheckedChange = { remindersOn = it })
            HorizontalDivider()
            // Completed Tasks row (replaces play sound)
            SettingClickableItem(
                text = "View Completed Tasks",
                icon = Icons.Default.ListAlt,
                onClick = onCompletedTasksClicked
            )
            HorizontalDivider()
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onLogoutClicked,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AppDarkBlue, contentColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout Icon", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchItem(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text, fontSize = 16.sp)
            Text(if (checked) "on" else "off", fontSize = 14.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AppDarkBlue)
        )
    }
}

@Composable
private fun SettingClickableItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = AppDarkBlue)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = "Go to $text", tint = Color(0xFFADD8E6))
    }
}