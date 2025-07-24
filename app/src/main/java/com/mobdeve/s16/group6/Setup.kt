package com.mobdeve.s16.group6

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.mobdeve.s16.group6.ui.theme.AppCardBlue
import com.mobdeve.s16.group6.ui.theme.AppTextBlack
import com.mobdeve.s16.group6.ui.theme.ChoreoUITheme

@Composable
fun SetupScreen(
    onCreateHouseholdClicked: () -> Unit,
    onJoinHouseholdClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // shared logo component
        ChoreoLogo()

        Spacer(modifier = Modifier.height(12.dp))


        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "SELECT",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppTextBlack,
            letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateHouseholdClicked,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppCardBlue,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Create New Household",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onJoinHouseholdClicked,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppCardBlue,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Join Household",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SetupScreenPreview() {
    ChoreoUITheme {
        SetupScreen(onCreateHouseholdClicked = {}, onJoinHouseholdClicked = {})
    }
}