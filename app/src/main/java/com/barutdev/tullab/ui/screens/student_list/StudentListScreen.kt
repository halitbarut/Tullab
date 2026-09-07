package com.barutdev.tullab.ui.screens.student_list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.tullab.R
import com.barutdev.tullab.domain.model.Student
import com.barutdev.tullab.ui.navigation.FabConfig
import com.barutdev.tullab.ui.navigation.ScreenScaffoldConfig
import com.barutdev.tullab.ui.navigation.TopBarConfig
import com.barutdev.tullab.ui.navigation.TopBarAction as NavigationTopBarAction
import com.barutdev.tullab.ui.preferences.LocalUserPreferences
import com.barutdev.tullab.ui.theme.TullabTheme
import com.barutdev.tullab.ui.theme.TullabAnimationSpecs
import com.barutdev.tullab.ui.components.TullabSkeletonCard
import com.barutdev.tullab.ui.components.AnimatedListItem
import com.barutdev.tullab.util.formatCurrency
import com.barutdev.tullab.util.tullabStringResource
import java.util.Locale

@Composable
fun StudentListScreen(
    onAddStudent: () -> Unit,
    onStudentClick: (String) -> Unit,
    onEditStudentProfile: (Int) -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StudentListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userPreferences = LocalUserPreferences.current

    val topBarTitle = tullabStringResource(id = R.string.student_list_title)
    val addStudentDescription = tullabStringResource(id = R.string.student_list_add_student_fab_content_description)
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary

    val topBarActionDefinitions = remember(
        onNavigateToReports,
        onNavigateToSettings
    ) {
        listOf(
            TopBarAction(
                icon = Icons.Outlined.BarChart,
                descriptionResId = R.string.student_list_reports_action_description,
                onClick = onNavigateToReports
            ),
            TopBarAction(
                icon = Icons.Filled.Settings,
                descriptionResId = R.string.dashboard_settings_icon_description,
                onClick = onNavigateToSettings
            )
        )
    }

    val topBarActions = topBarActionDefinitions.map { action ->
        NavigationTopBarAction(
            icon = action.icon,
            contentDescription = tullabStringResource(id = action.descriptionResId),
            onClick = action.onClick
        )
    }

    val topBarConfig = remember(topBarTitle, topBarActions) {
        TopBarConfig(
            title = topBarTitle,
            actions = topBarActions
        )
    }
    val fabConfig = remember(
        addStudentDescription,
        containerColor,
        contentColor,
        onAddStudent
    ) {
        FabConfig(
            icon = Icons.Filled.Add,
            contentDescription = addStudentDescription,
            onClick = {
                onAddStudent()
            },
            containerColor = containerColor,
            contentColor = contentColor
        )
    }
    ScreenScaffoldConfig(
        topBarConfig = topBarConfig,
        fabConfig = fabConfig
    )

    StudentListScreenContent(
        students = uiState.students,
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearSearch = viewModel::onClearSearchQuery,
        onStudentClick = onStudentClick,
        onEditStudent = { student -> onEditStudentProfile(student.id) },
        onDeleteStudent = { student -> viewModel.deleteStudent(student.id) },
        currencyCode = userPreferences.currencyCode,
        isSearchActive = uiState.isSearchActive,
        hasAnyStudents = uiState.hasAnyStudents,
        isLoading = uiState.isLoading,
        modifier = modifier.fillMaxSize()
    )
}


@Composable
private fun StudentListScreenContent(
    students: List<StudentWithDebt>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onStudentClick: (String) -> Unit,
    onEditStudent: (Student) -> Unit,
    onDeleteStudent: (Student) -> Unit = {},
    currencyCode: String,
    isSearchActive: Boolean,
    hasAnyStudents: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        StudentListLoadingState(
            modifier = modifier.fillMaxSize()
        )
        return
    }

    if (!hasAnyStudents) {
        StudentListEmptyState(
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val showNoResults = isSearchActive && students.isEmpty()
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    if (studentToDelete != null) {
        val targetStudent = studentToDelete!!
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = {
                Text(text = tullabStringResource(id = R.string.delete_student_confirmation_title))
            },
            text = {
                Text(
                    text = tullabStringResource(
                        id = R.string.delete_student_confirmation_message,
                        targetStudent.fullName
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = targetStudent
                        studentToDelete = null
                        onDeleteStudent(toDelete)
                    }
                ) {
                    Text(
                        text = tullabStringResource(id = R.string.dialog_action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text(text = tullabStringResource(id = R.string.dialog_action_cancel))
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            StudentListSearchField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClearQuery = onClearSearch,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (showNoResults) {
            item {
                StudentListNoResultsState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 16.dp)
                )
            }
        } else {
            itemsIndexed(
                items = students,
                key = { _, studentWithDebt -> studentWithDebt.student.id }
            ) { index, studentWithDebt ->
                AnimatedListItem(index = index + 1) {
                    StudentListItem(
                        student = studentWithDebt,
                        currencyCode = currencyCode,
                        onStudentClick = onStudentClick,
                        onEditClick = onEditStudent,
                        onDeleteClick = { student -> studentToDelete = student },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentListSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val placeholder = tullabStringResource(id = R.string.student_list_search_placeholder)
    val clearContentDescription = tullabStringResource(id = R.string.student_list_search_clear_content_description)

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("StudentListSearchField"),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = clearContentDescription
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
        ),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search)
    )
}

@Composable
private fun StudentListNoResultsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(96.dp)
        )
        Text(
            text = tullabStringResource(id = R.string.student_list_search_no_results_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}


@Composable
private fun StudentListLoadingState(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        items(5) { index ->
            val animatedAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = TullabAnimationSpecs.fadeInSpec,
                label = "skeleton$index"
            )
            TullabSkeletonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = animatedAlpha }
            )
        }
    }
}

@Composable
private fun StudentListEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Groups,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(120.dp)
            )
            Text(
                text = tullabStringResource(id = R.string.student_list_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = tullabStringResource(id = R.string.student_list_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun StudentListItem(
    student: StudentWithDebt,
    currencyCode: String,
    onStudentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onEditClick: ((Student) -> Unit)? = null,
    onDeleteClick: ((Student) -> Unit)? = null
) {
    val studentDetails = student.student
    val formattedDebt by remember(student.currentDebt, currencyCode) {
        derivedStateOf { formatCurrency(student.currentDebt, currencyCode) }
    }
    val studentInitials by remember(studentDetails.fullName) {
        derivedStateOf { studentDetails.initials() }
    }
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = { onStudentClick(studentDetails.id.toString()) },
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = studentInitials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = studentDetails.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tullabStringResource(
                        id = R.string.student_list_amount_due,
                        formattedDebt
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = tullabStringResource(id = R.string.student_card_more_actions),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = tullabStringResource(id = R.string.student_action_edit)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onEditClick?.invoke(studentDetails)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = tullabStringResource(id = R.string.student_action_delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick?.invoke(studentDetails)
                        }
                    )
                }
            }
        }
    }
}

private val previewStudents = listOf(
    Student(id = 1, fullName = "Elif Yılmaz", hourlyRate = 360.0),
    Student(id = 2, fullName = "Ahmet Demir", hourlyRate = 240.0),
    Student(id = 3, fullName = "Ayşe Kaya", hourlyRate = 180.0)
)

private val previewStudentsWithDebt = previewStudents.mapIndexed { index, student ->
    StudentWithDebt(
        student = student,
        currentDebt = (index + 1) * 150.0
    )
}

@Preview(showBackground = true)
@Composable
private fun StudentListScreenPreview() {
    TullabTheme {
        StudentListScreenContent(
            students = previewStudentsWithDebt,
            searchQuery = "",
            onSearchQueryChange = {},
            onClearSearch = {},
            onStudentClick = {},
            onEditStudent = {},
            currencyCode = "TRY",
            isSearchActive = false,
            hasAnyStudents = true,
            isLoading = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentListEmptyScreenPreview() {
    TullabTheme {
        StudentListScreenContent(
            students = emptyList(),
            searchQuery = "",
            onSearchQueryChange = {},
            onClearSearch = {},
            onStudentClick = {},
            onEditStudent = {},
            currencyCode = "TRY",
            isSearchActive = false,
            hasAnyStudents = false,
            isLoading = false
        )
    }
}

private data class TopBarAction(
    val icon: ImageVector,
    @StringRes val descriptionResId: Int,
    val onClick: () -> Unit
)

private fun Student.initials(): String = fullName
    .split(" ")
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString(separator = "") { part ->
        part.take(1).uppercase(Locale.getDefault())
    }
    .ifEmpty {
        fullName.take(2).uppercase(Locale.getDefault())
    }
