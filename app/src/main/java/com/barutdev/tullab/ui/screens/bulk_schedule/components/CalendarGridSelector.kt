package com.barutdev.tullab.ui.screens.bulk_schedule.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.barutdev.tullab.R
import com.barutdev.tullab.ui.theme.LocalLocale
import com.barutdev.tullab.ui.theme.TullabAnimationSpecs
import com.barutdev.tullab.util.tullabStringResource
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields

@Composable
fun CalendarGridSelector(
    currentMonth: YearMonth,
    selectedDates: Set<LocalDate>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateToggled: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    lessonsByDate: Map<LocalDate, List<com.barutdev.tullab.domain.model.Lesson>> = emptyMap(),
    homeworkByDate: Map<LocalDate, List<com.barutdev.tullab.domain.model.Homework>> = emptyMap()
) {
    val locale = LocalLocale.current
    val monthName = remember(currentMonth, locale) {
        currentMonth.month.getDisplayName(TextStyle.FULL, locale)
    }
    val formattedMonthName = monthName.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(locale) else char.toString()
    }
    val monthTitle = tullabStringResource(
        id = R.string.calendar_month_year_title,
        formattedMonthName,
        currentMonth.year
    )

    val daysOfWeek = remember(locale) { 
        val weekFields = WeekFields.of(locale)
        val firstDay = weekFields.firstDayOfWeek
        (0..6).map { firstDay.plus(it.toLong()) }
    }
    
    val weekFields = remember(locale) { WeekFields.of(locale) }
    val daysInWeek = DayOfWeek.values().size
    val firstOfMonth = remember(currentMonth) { currentMonth.atDay(1) }
    val leadingDays = ((firstOfMonth.dayOfWeek.value - weekFields.firstDayOfWeek.value) + daysInWeek) % daysInWeek
    val totalDays = currentMonth.lengthOfMonth()
    val trailingDays = (daysInWeek - (leadingDays + totalDays) % daysInWeek) % daysInWeek
    val calendarDays = remember(currentMonth, leadingDays, trailingDays) {
        buildList {
            repeat(leadingDays) { add(null) }
            repeat(totalDays) { dayIndex -> add(currentMonth.atDay(dayIndex + 1)) }
            repeat(trailingDays) { add(null) }
        }
    }
    val calendarRows = remember(calendarDays) {
        calendarDays.chunked(daysInWeek)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = tullabStringResource(id = R.string.calendar_previous_month_content_description)
                )
            }
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = tullabStringResource(id = R.string.calendar_next_month_content_description)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, locale).uppercase(locale),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            calendarRows.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    week.forEach { date ->
                        if (date == null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        } else {
                            val isSelected = selectedDates.contains(date)
                            val shape = MaterialTheme.shapes.medium
                            val animatedBackgroundColor by animateColorAsState(
                                targetValue = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                },
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 150),
                                label = "dayCellBackground"
                            )
                            val animatedTextColor by animateColorAsState(
                                targetValue = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 150),
                                label = "dayCellText"
                            )
                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.05f else 1f,
                                animationSpec = TullabAnimationSpecs.pressSpec,
                                label = "dayCellScale"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .graphicsLayer { scaleX = scale; scaleY = scale }
                                    .clip(shape)
                                    .background(animatedBackgroundColor)
                                    .minimumInteractiveComponentSize()
                                    .clickable { onDateToggled(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                val zoneId = java.time.ZoneId.systemDefault()
                                val today = java.time.LocalDate.now(zoneId)
                                val lessonsForDate = lessonsByDate[date].orEmpty()
                                val homeworkForDate = homeworkByDate[date].orEmpty()
                                val dayIndicators = com.barutdev.tullab.ui.screens.calendar.resolveDayIndicators(
                                    lessons = lessonsForDate,
                                    homework = homeworkForDate,
                                    date = date,
                                    today = today
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = animatedTextColor,
                                        textAlign = TextAlign.Center
                                    )
                                    if (dayIndicators.lessonColor != null || dayIndicators.homeworkColor != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        if (dayIndicators.lessonColor != null && dayIndicators.homeworkColor != null) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier.size(5.dp).clip(androidx.compose.foundation.shape.CircleShape).background(dayIndicators.lessonColor)
                                                )
                                                Box(
                                                    modifier = Modifier.size(5.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(1.5.dp)).background(dayIndicators.homeworkColor)
                                                )
                                            }
                                        } else if (dayIndicators.lessonColor != null) {
                                            Box(
                                                modifier = Modifier.size(5.dp).clip(androidx.compose.foundation.shape.CircleShape).background(dayIndicators.lessonColor)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.size(5.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(1.5.dp)).background(dayIndicators.homeworkColor!!)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
