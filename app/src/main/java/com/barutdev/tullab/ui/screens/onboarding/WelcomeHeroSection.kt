package com.barutdev.tullab.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barutdev.tullab.R
import com.barutdev.tullab.util.tullabStringResource

@Composable
fun WelcomeHeroSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Brand Emblem Container
        Box(
            modifier = Modifier
                .size(100.dp) // Enlarge the frame
                .clip(RoundedCornerShape(24.dp)) // Rounded square frame
                .background(androidx.compose.ui.graphics.Color.White), // White background
            contentAlignment = Alignment.Center // Center the icon
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.tullab),
                contentDescription = null, // Decorative
                modifier = Modifier.size(72.dp) // Enlarge the icon
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Headline
        Text(
            text = tullabStringResource(id = R.string.onboarding_welcome_title),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = tullabStringResource(id = R.string.onboarding_welcome_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
