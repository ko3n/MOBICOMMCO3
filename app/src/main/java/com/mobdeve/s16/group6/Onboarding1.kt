package com.mobdeve.s16.group6

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.mobdeve.s16.group6.ui.theme.AppCardBlue
import com.mobdeve.s16.group6.ui.theme.AppDarkBlue
import com.mobdeve.s16.group6.ui.theme.AppTextBlack
import com.mobdeve.s16.group6.ui.theme.ChoreoUITheme


@Composable
fun Onboarding1(
    onContinueClicked: () -> Unit,
    onSkipClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Skip",
                color = AppDarkBlue,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
                    .clickable { onSkipClicked() }
            )

            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = "Welcome to Choreo !",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = AppTextBlack
            )
            Spacer(modifier = Modifier.height(48.dp))


            ChoreoLogo(modifier = Modifier.size(250.dp))

            Spacer(modifier = Modifier.height(12.dp))


            Spacer(modifier = Modifier.weight(1f))
        }

        BottomInfoCard(onContinueClicked = onContinueClicked)
    }
}



@Composable
fun BottomInfoCard(onContinueClicked: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        colors = CardDefaults.cardColors(containerColor = AppCardBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 40.dp)
        ) {
            Text(
                text = "Keep Your Household\nRunning Smoothly",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Create a home group, assign chores in seconds, and keep everyone in the loop—no more nagging or confusion!",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onContinueClicked,
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.End),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White,
                    contentColor = AppCardBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Continue",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Onboarding1Preview() {
    ChoreoUITheme {
        Onboarding1(onContinueClicked = {}, onSkipClicked = {})
    }
}