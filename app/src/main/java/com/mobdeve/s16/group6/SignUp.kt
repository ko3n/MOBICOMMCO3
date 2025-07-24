package com.mobdeve.s16.group6

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.VisualTransformation

import com.mobdeve.s16.group6.ui.theme.*

@Composable
fun SignUpScreen(
    onBackClicked: () -> Unit,
    onRegisterClicked: (String, String, String, String) -> Unit
) {

    var householdName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar with Back Arrow
        IconButton(
            onClick = onBackClicked,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = AppDarkBlue
            )
        }

        // Main Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.5f))


            ChoreoLogo(modifier = Modifier.size(200.dp))

            Spacer(modifier = Modifier.height(12.dp))


            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Let's help you meet up your tasks!",
                fontSize = 10.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Household Name Text Field
            SignUpTextField(
                value = householdName,
                onValueChange = { householdName = it },
                placeholder = "Enter household name"
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Email Text Field
            SignUpTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Enter your email",
                keyboardType = KeyboardType.Email
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Text Field
            SignUpTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Enter password",
                isPassword = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Text Field
            SignUpTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm Password",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = { onRegisterClicked(householdName, email, password, confirmPassword) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppCardBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Register",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

// A reusable text field for this screen to avoid repetition
@Composable
private fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.Gray) },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppGray.copy(alpha = 0.5f),
            unfocusedContainerColor = AppGray.copy(alpha = 0.5f),
            disabledContainerColor = AppGray.copy(alpha = 0.5f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    ChoreoUITheme {
        SignUpScreen(onBackClicked = {}, onRegisterClicked = { _, _, _, _ -> })
    }
}