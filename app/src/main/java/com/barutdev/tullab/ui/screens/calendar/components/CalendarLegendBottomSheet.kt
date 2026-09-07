package com.barutdev.tullab.ui.screens.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.barutdev.tullab.R
import com.barutdev.tullab.ui.theme.HomeworkGray
import com.barutdev.tullab.ui.theme.StatusBlue
import com.barutdev.tullab.ui.theme.StatusGreen
import com.barutdev.tullab.ui.theme.StatusRed
import com.barutdev.tullab.ui.theme.StatusYellow
import com.barutdev.tullab.util.tullabStringResource

/**
 * Material 3 ModalBottomSheet detailing the unified calendar color guide
 * and distinct visual markers (round dots for lessons, square marks for homework).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarLegendBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Title
            Text(
                text = tullabStringResource(id = R.string.calendar_legend_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Shape distinction hint card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = tullabStringResource(id = R.string.calendar_legend_shape_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Section 1: Lessons
            Text(
                text = tullabStringResource(id = R.string.calendar_legend_section_lessons),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LegendGuideItem(
                    indicatorColor = StatusBlue,
                    indicatorShape = CircleShape,
                    title = tullabStringResource(id = R.string.calendar_legend_lesson_scheduled_title),
                    description = tullabStringResource(id = R.string.calendar_legend_lesson_scheduled_desc)
                )
                LegendGuideItem(
                    indicatorColor = StatusRed,
                    indicatorShape = CircleShape,
                    title = tullabStringResource(id = R.string.calendar_legend_lesson_action_required_title),
                    description = tullabStringResource(id = R.string.calendar_legend_lesson_action_required_desc)
                )
                LegendGuideItem(
                    indicatorColor = StatusYellow,
                    indicatorShape = CircleShape,
                    title = tullabStringResource(id = R.string.calendar_legend_lesson_completed_title),
                    description = tullabStringResource(id = R.string.calendar_legend_lesson_completed_desc)
                )
                LegendGuideItem(
                    indicatorColor = StatusGreen,
                    indicatorShape = CircleShape,
                    title = tullabStringResource(id = R.string.calendar_legend_lesson_paid_title),
                    description = tullabStringResource(id = R.string.calendar_legend_lesson_paid_desc)
                )
                LegendGuideItem(
                    indicatorColor = HomeworkGray,
                    indicatorShape = CircleShape,
                    title = tullabStringResource(id = R.string.calendar_legend_lesson_cancelled_title),
                    description = tullabStringResource(id = R.string.calendar_legend_lesson_cancelled_desc)
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Section 2: Homework
            Text(
                text = tullabStringResource(id = R.string.calendar_legend_section_homework),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            val homeworkMarkShape = RoundedCornerShape(2.5.dp)

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LegendGuideItem(
                    indicatorColor = StatusBlue,
                    indicatorShape = homeworkMarkShape,
                    title = tullabStringResource(id = R.string.calendar_legend_homework_scheduled_title),
                    description = tullabStringResource(id = R.string.calendar_legend_homework_scheduled_desc)
                )
                LegendGuideItem(
                    indicatorColor = StatusRed,
                    indicatorShape = homeworkMarkShape,
                    title = tullabStringResource(id = R.string.calendar_legend_homework_action_required_title),
                    description = tullabStringResource(id = R.string.calendar_legend_homework_action_required_desc)
                )
                LegendGuideItem(
                    indicatorColor = StatusGreen,
                    indicatorShape = homeworkMarkShape,
                    title = tullabStringResource(id = R.string.calendar_legend_homework_completed_title),
                    description = tullabStringResource(id = R.string.calendar_legend_homework_completed_desc)
                )
                LegendGuideItem(
                    indicatorColor = HomeworkGray,
                    indicatorShape = homeworkMarkShape,
                    title = tullabStringResource(id = R.string.calendar_legend_homework_cancelled_title),
                    description = tullabStringResource(id = R.string.calendar_legend_homework_cancelled_desc)
                )
            }

            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun LegendGuideItem(
    indicatorColor: Color,
    indicatorShape: Shape,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(indicatorShape)
                    .background(indicatorColor)
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
