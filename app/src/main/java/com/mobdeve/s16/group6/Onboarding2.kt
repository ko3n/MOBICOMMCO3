package com.mobdeve.s16.group6

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.mobdeve.s16.group6.ui.theme.AppCardBlue
import com.mobdeve.s16.group6.ui.theme.AppDarkBlue
import com.mobdeve.s16.group6.ui.theme.ChoreoUITheme


@Composable
fun Onboarding2(
    onBackClicked: () -> Unit,
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
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AppDarkBlue
                    )
                }

                Text(
                    text = "Skip",
                    color = AppDarkBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { onSkipClicked() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.scooter_illustration),
                contentDescription = "Person on a scooter",
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }

        BottomInfoCard2(onContinueClicked = onContinueClicked)
    }
}

@Composable
fun BottomInfoCard2(onContinueClicked: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp),
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
                text = "Fair, Flexible, and Fully Trackable",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Create one-off or recurring chores, assign them in a tap, and let Choreo automate rotations, filters, and reminders—so your home stays organized and on schedule.",
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
fun Onboarding2Preview() {
    ChoreoUITheme {
        Onboarding2(
            onBackClicked = {},
            onContinueClicked = {},
            onSkipClicked = {}
        )
    }
}