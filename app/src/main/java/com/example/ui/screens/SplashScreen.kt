package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FintechBlue
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMutedGray
import com.example.ui.theme.TextSilverBase
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isLoggedIn: Boolean,
    onNavigateNext: (route: String) -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val currentIsLoggedIn by rememberUpdatedState(isLoggedIn)

    LaunchedEffect(Unit) {
        // Animate fade-in
        alphaAnim.animateTo(1f, animationSpec = tween(1200))
        delay(1000)
        // Auto navigate
        val startRoute = if (currentIsLoggedIn) "main" else "auth"
        onNavigateNext(startRoute)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianBg,
                        Color(0xFF080B14)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            Icon(
                imageVector = Icons.Default.AutoGraph,
                contentDescription = "Bull & Bear Journal Logo",
                tint = FintechBlue,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "K I N E T I X",
                color = TextSilverBase,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "P R O   J O U R N A L",
                color = FintechBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}
