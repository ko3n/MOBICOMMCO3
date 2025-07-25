package com.mobdeve.s16.group6

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mobdeve.s16.group6.data.Person
import com.mobdeve.s16.group6.ui.theme.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleTab(
    peopleViewModel: PeopleViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    navController: NavController
) {
    var showDialog by remember { mutableStateOf(false) }

    val people by peopleViewModel.people.collectAsState()
    val addPersonError by peopleViewModel.addPersonError.collectAsState()
    val currentHousehold by authViewModel.currentHousehold.collectAsState() // Access currentHousehold here

    val context = LocalContext.current
    LaunchedEffect(addPersonError) {
        addPersonError?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            peopleViewModel.clearAddPersonError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PEOPLE",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp,
                        color = AppTextBlack
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = AppCardBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Person")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (people.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Is anyone home?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppCardBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add household members using the button above!",
                        fontSize = 18.sp,
                        color = AppCardBlue.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(people) { person ->
                        PersonItem(
                            person = person,
                            onPersonClick = { clickedPerson ->
                                currentHousehold?.let { household ->
                                    val encodedPersonName = URLEncoder.encode(clickedPerson.name, StandardCharsets.UTF_8.toString())
                                    val encodedHouseholdName = URLEncoder.encode(household.name, StandardCharsets.UTF_8.toString())
                                    val encodedHouseholdEmail = URLEncoder.encode(household.email, StandardCharsets.UTF_8.toString())

                                    navController.navigate(
                                        "tasks/${clickedPerson.id}/$encodedPersonName/$encodedHouseholdName/$encodedHouseholdEmail"
                                    )
                                } ?: run {
                                    Toast.makeText(context, "Error: Household data not available to open tasks.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreatePersonDialog(
            onDismiss = { showDialog = false },
            onAddPerson = { name ->
                showDialog = false
                currentHousehold?.let { household ->
                    peopleViewModel.addPerson(name, household.name, household.email)
                } ?: run {
                    Toast.makeText(context, "Error: Household not identified. Please re-login.", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

@Composable
fun PersonItem(person: Person, onPersonClick: (Person) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppLightBlue),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(50.dp)
            .clickable { onPersonClick(person) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = person.name,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}


@Composable
fun CreatePersonDialog(onDismiss: () -> Unit, onAddPerson: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppCardBlue),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "create new",
                        color = Color.White,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter person's name", color = Color.Gray) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppGray,
                        unfocusedContainerColor = AppGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onAddPerson(name) },
                    enabled = name.isNotBlank(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AppDarkBlue
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Add", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PeopleTabPreview() {
    ChoreoUITheme {
        PeopleTab(
            peopleViewModel = PeopleViewModel(Application()),
            authViewModel = AuthViewModel(Application()),
            navController = rememberNavController()
        )
    }
}