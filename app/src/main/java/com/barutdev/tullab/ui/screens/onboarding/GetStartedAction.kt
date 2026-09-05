package com.barutdev.tullab.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.barutdev.tullab.R
import com.barutdev.tullab.util.tullabStringResource

@Composable
fun GetStartedAction(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "press_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        animationSpec = tween(durationMillis = 300),
        label = "bg_color"
    )

    Button(
        onClick = onClick,
        enabled = isEnabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .height(56.dp) // larger touch target than 48dp minimum
            .scale(scale)
    ) {
        Text(text = tullabStringResource(id = R.string.onboarding_get_started))
    }
}
