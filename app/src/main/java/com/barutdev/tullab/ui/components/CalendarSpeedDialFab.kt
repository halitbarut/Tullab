package com.barutdev.tullab.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.barutdev.tullab.R
import com.barutdev.tullab.util.tullabStringResource

@Composable
fun CalendarSpeedDialFab(
    onScheduleForDateClick: () -> Unit,
    onBulkScheduleClick: () -> Unit,
    modifier: Modifier = Modifier,
    dateActionLabel: String = tullabStringResource(id = R.string.calendar_speed_dial_schedule_for_date),
    dateActionIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Outlined.Event,
    snackbarVisible: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = isExpanded) {
        isExpanded = false
    }

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "fab_rotation"
    )

    val snackbarOffset by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (snackbarVisible) 72.dp else 0.dp,
        animationSpec = tween(durationMillis = 250),
        label = "speed_dial_snackbar_offset"
    )

    val fabDescription = tullabStringResource(id = R.string.calendar_speed_dial_content_description)
    val bulkScheduleLabel = tullabStringResource(id = R.string.calendar_speed_dial_bulk_schedule)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // Dismiss scrim when expanded
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isExpanded = false
                    }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 16.dp + snackbarOffset, end = 16.dp)
        ) {
            // Speed Dial mini options
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(150)) + slideInVertically(tween(150)) { it / 2 },
                exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 }
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Option 1: Toplu Ders Ekle (Bulk Schedule)
                    SpeedDialOption(
                        label = bulkScheduleLabel,
                        icon = Icons.Outlined.DateRange,
                        onClick = {
                            isExpanded = false
                            onBulkScheduleClick()
                        }
                    )

                    // Option 2: Dynamic Action (Seçili Güne Planla / Dersi Düzenle)
                    SpeedDialOption(
                        label = dateActionLabel,
                        icon = dateActionIcon,
                        onClick = {
                            isExpanded = false
                            onScheduleForDateClick()
                        }
                    )
                }
            }

            // Main Speed Dial FAB
            FloatingActionButton(
                onClick = { isExpanded = !isExpanded },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(56.dp)
                    .semantics { contentDescription = fabDescription }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun SpeedDialOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 2.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        }
    }
}
