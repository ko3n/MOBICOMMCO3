package com.mobdeve.s16.group6

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun ChoreoLogo(modifier: Modifier = Modifier.size(250.dp)) {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "Choreo App Logo",

        modifier = modifier
    )
}