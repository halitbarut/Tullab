package com.barutdev.tullab.ui.screens.homework

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.barutdev.tullab.util.tullabStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.Homework
import com.barutdev.tullab.domain.model.HomeworkStatus
import com.barutdev.tullab.ui.navigation.FabConfig
import com.barutdev.tullab.ui.navigation.ScreenScaffoldConfig
import com.barutdev.tullab.ui.navigation.TopBarAction
import com.barutdev.tullab.ui.navigation.TopBarConfig
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.barutdev.tullab.ui.screens.common.StudentNameUiStatus
import com.barutdev.tullab.ui.screens.common.deriveStudentNameUiStatus
import com.barutdev.tullab.ui.screens.homework.components.HomeworkBottomSheet
import com.barutdev.tullab.ui.screens.homework.components.HomeworkFilterChips
import com.barutdev.tullab.ui.theme.TullabAnimationSpecs
import com.barutdev.tullab.ui.theme.TullabTheme
import com.barutdev.tullab.ui.theme.LocalLocale
import com.barutdev.tullab.ui.theme.StatusGreen
import com.barutdev.tullab.ui.theme.StatusRed
import com.barutdev.tullab.ui.theme.StatusYellow
import com.barutdev.tullab.ui.components.AnimatedListItem
import com.barutdev.tullab.ui.components.TullabHapticFeedbackType
import com.barutdev.tullab.ui.components.rememberTullabHapticFeedback
import com.barutdev.tullab.ui.navigation.LocalTullabScaffoldController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import com.barutdev.tullab.util.calculateDelayToNextMidnight
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun HomeworkScreen(
    onNavigateToStudentList: () -> Unit,
    expectedStudentId: Int? = null,
    homeworkId: Int? = null,
    modifier: Modifier = Modifier,
    viewModel: HomeworkViewModel = hiltViewModel(
        key = expectedStudentId?.let { "homework-$it" } ?: "homework-default"
    )
) {
    val studentName by viewModel.studentName.collectAsStateWithLifecycle()
    val homeworkList by viewModel.homework.collectAsStateWithLifecycle()
    val isDialogVisible by viewModel.isDialogVisible.collectAsStateWithLifecycle()
    val editingHomework by viewModel.editingHomework.collectAsStateWithLifecycle()
    val locale = LocalLocale.current
    val scaffoldController = LocalTullabScaffoldController.current
    val haptics = rememberTullabHapticFeedback()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(expectedStudentId) {
        Log.d("HomeworkScreen", "Composing for expectedStudentId=$expectedStudentId")
    }
    LaunchedEffect(viewModel.studentId) {
        Log.d("HomeworkScreen", "Rendering homework for studentId=${viewModel.studentId}")
    }

    val homeworkDeletedMessage = tullabStringResource(id = R.string.snackbar_homework_deleted)
    val homeworkCompletedMessage = tullabStringResource(id = R.string.snackbar_homework_completed)
    val undoLabel = tullabStringResource(id = R.string.snackbar_action_undo)

    HomeworkBottomSheet(
        showSheet = isDialogVisible,
        editingHomework = editingHomework,
        onDismiss = viewModel::dismissHomeworkDialog,
        onConfirm = { title, description, dueDate, status, performanceNotes ->
            viewModel.onSubmitHomework(
                title = title,
                description = description,
                dueDate = dueDate,
                status = status,
                performanceNotes = performanceNotes
            )
        },
        onDelete = { homework ->
            haptics.perform(TullabHapticFeedbackType.WARNING)
            viewModel.deleteHomeworkWithUndo(homework) { snapshot ->
                coroutineScope.launch {
                    scaffoldController.showUndoSnackbar(
                        message = homeworkDeletedMessage,
                        actionLabel = undoLabel,
                        onUndo = { viewModel.restoreHomework(snapshot) }
                    )
                }
            }
        }
    )

    val studentNameStatus = deriveStudentNameUiStatus(
        studentName = studentName,
        hasStudentReference = viewModel.hasStudentReference
    )
    val homeworkLabel = tullabStringResource(id = R.string.homework_title)
    val resolvedStudentName = studentName.takeIf {
        studentNameStatus == StudentNameUiStatus.Ready && it.isNotBlank()
    }
    val topBarTitle = resolvedStudentName?.let {
        tullabStringResource(
            id = R.string.homework_top_bar_title,
            it,
            homeworkLabel
        )
    } ?: homeworkLabel
    val navigateDescription = tullabStringResource(
        id = R.string.top_bar_navigate_to_student_list_content_description
    )
    val addHomeworkDescription = tullabStringResource(id = R.string.homework_add_fab_content_description)
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val displayStudentName = studentName.takeIf { studentNameStatus == StudentNameUiStatus.Ready && it.isNotBlank() }

    val topBarConfig = remember(
        topBarTitle,
        navigateDescription,
        onNavigateToStudentList
    ) {
        TopBarConfig(
            title = topBarTitle,
            navigationIcon = TopBarAction(
                icon = Icons.Outlined.Groups,
                contentDescription = navigateDescription,
                onClick = onNavigateToStudentList
            )
        )
    }
    val fabConfig = remember(
        addHomeworkDescription,
        containerColor,
        contentColor,
        viewModel
    ) {
        FabConfig(
            icon = Icons.Filled.Add,
            contentDescription = addHomeworkDescription,
            onClick = viewModel::showAddHomeworkDialog,
            containerColor = containerColor,
            contentColor = contentColor
        )
    }
    ScreenScaffoldConfig(
        topBarConfig = topBarConfig,
        fabConfig = fabConfig
    )

    HomeworkScreenContent(
        modifier = modifier.fillMaxSize(),
        studentName = displayStudentName,
        homeworkList = homeworkList,
        onHomeworkClick = viewModel::showEditHomeworkDialog,
        onToggleHomeworkStatus = { homework ->
            if (homework.status != com.barutdev.tullab.domain.model.HomeworkStatus.CANCELLED) {
                haptics.perform(TullabHapticFeedbackType.CLICK)
                val previousStatus = homework.status
                viewModel.toggleHomeworkStatus(homework)
                if (previousStatus == com.barutdev.tullab.domain.model.HomeworkStatus.PENDING) {
                    coroutineScope.launch {
                        scaffoldController.showUndoSnackbar(
                            message = homeworkCompletedMessage,
                            actionLabel = undoLabel,
                            onUndo = { viewModel.revertHomeworkStatusUndo(homework, previousStatus) }
                        )
                    }
                }
            }
        }
    )
}


@Composable
private fun HomeworkScreenContent(
    modifier: Modifier = Modifier,
    studentName: String?,
    homeworkList: List<Homework>,
    onHomeworkClick: (Homework) -> Unit,
    onToggleHomeworkStatus: (Homework) -> Unit
) {
    val locale = LocalLocale.current
    var selectedFilter by rememberSaveable { mutableStateOf(HomeworkFilter.ALL) }
    var utcToday by remember { mutableStateOf(LocalDate.now(ZoneOffset.UTC)) }

    LaunchedEffect(Unit) {
        while (isActive) {
            val delayMs = calculateDelayToNextMidnight(Instant.now(), ZoneOffset.UTC)
            delay(delayMs)
            utcToday = LocalDate.now(ZoneOffset.UTC)
        }
    }

    val filteredHomeworkList = remember(homeworkList, selectedFilter) {
        when (selectedFilter) {
            HomeworkFilter.ALL -> homeworkList
            HomeworkFilter.PENDING -> homeworkList.filter {
                it.status == HomeworkStatus.PENDING
            }
            HomeworkFilter.COMPLETED -> homeworkList.filter {
                it.status == HomeworkStatus.COMPLETED
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Section Title
        AnimatedListItem(index = 0) {
            Text(
                text = tullabStringResource(id = R.string.homework_assignments_section_title),
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Card 2: Filter Chips
        AnimatedListItem(index = 1) {
            HomeworkFilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
        }

        if (filteredHomeworkList.isEmpty()) {
            AnimatedListItem(index = 2) {
                HomeworkEmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp)
                )
            }
        } else {
            filteredHomeworkList.forEachIndexed { listIndex, homework ->
                AnimatedListItem(index = listIndex + 3) {
                    HomeworkListItem(
                        homework = homework,
                        locale = locale,
                        onClick = onHomeworkClick,
                        onToggleStatus = onToggleHomeworkStatus,
                        utcToday = utcToday
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeworkEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Assignment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(120.dp)
            )
            Text(
                text = tullabStringResource(id = R.string.homework_empty_state_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = tullabStringResource(id = R.string.homework_empty_state_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Composable
private fun HomeworkListItem(
    homework: Homework,
    locale: Locale,
    onClick: (Homework) -> Unit,
    onToggleStatus: (Homework) -> Unit,
    modifier: Modifier = Modifier,
    utcToday: LocalDate = LocalDate.now(ZoneOffset.UTC)
) {
    val dueDateText = remember(homework.dueDate, locale) {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        Instant.ofEpochMilli(homework.dueDate)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(formatter)
    }
    val isOverdue = remember(homework, utcToday) { homework.isOverdue(utcToday) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = TullabAnimationSpecs.contentSizeSpec)
            .clickable { onClick(homework) },
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = homework.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = tullabStringResource(id = R.string.homework_due_date_label, dueDateText),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            StatusBadge(status = homework.status, isOverdue = isOverdue)
            if (homework.status != HomeworkStatus.CANCELLED) {
                IconButton(
                    onClick = { onToggleStatus(homework) },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    val tint = if (homework.status == HomeworkStatus.COMPLETED) {
                        StatusGreen
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = tullabStringResource(
                            id = if (homework.status == HomeworkStatus.COMPLETED) {
                                R.string.calendar_homework_action_mark_pending
                            } else {
                                R.string.calendar_homework_action_mark_complete
                            }
                        ),
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: HomeworkStatus,
    isOverdue: Boolean,
    modifier: Modifier = Modifier
) {
    val (labelRes, targetColor) = when {
        isOverdue -> R.string.homework_status_overdue to StatusRed
        status == HomeworkStatus.PENDING -> R.string.homework_status_pending to StatusYellow
        status == HomeworkStatus.COMPLETED -> R.string.homework_status_completed to StatusGreen
        status == HomeworkStatus.CANCELLED -> R.string.homework_status_cancelled to androidx.compose.ui.graphics.Color.Gray
        else -> R.string.homework_status_pending to StatusYellow
    }
    val text = tullabStringResource(id = labelRes)
    
    // Animate color transitions
    val animatedColor by androidx.compose.animation.animateColorAsState(
        targetValue = targetColor,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 150),
        label = "statusBadgeColor"
    )
    
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(animatedColor.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = animatedColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeworkScreenPreview() {
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)
    val homeworkList = listOf(
        Homework(
            id = 1,
            studentId = 1,
            title = "Trigonometry Worksheet",
            description = "Complete problems 1-20",
            creationDate = today.minusDays(3).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            dueDate = today.atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = HomeworkStatus.COMPLETED,
            performanceNotes = "Excellent work"
        ),
        Homework(
            id = 2,
            studentId = 1,
            title = "Calculus Problems",
            description = "Review integrals",
            creationDate = today.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            dueDate = today.plusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            status = HomeworkStatus.PENDING,
            performanceNotes = null
        )
    )
    TullabTheme {
        HomeworkScreenContent(
            studentName = "Elif Yılmaz",
            homeworkList = homeworkList,
            onHomeworkClick = {},
            onToggleHomeworkStatus = {},
            modifier = Modifier
        )
    }
}
