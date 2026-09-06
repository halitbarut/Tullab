package com.barutdev.tullab.navigation

import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.barutdev.tullab.R
import com.barutdev.tullab.ui.navigation.BottomNavPreloadViewModel
import com.barutdev.tullab.ui.navigation.TullabScaffoldController
import com.barutdev.tullab.ui.navigation.LocalTullabScaffoldController
import com.barutdev.tullab.ui.navigation.TopBarAction
import com.barutdev.tullab.ui.navigation.TopBarConfig
import com.barutdev.tullab.ui.theme.TullabTheme
import com.barutdev.tullab.util.tullabStringResource
import com.barutdev.tullab.util.tullabPluralResource
import com.barutdev.tullab.ui.theme.LocalLocale
import com.barutdev.tullab.ui.navigation.rememberTullabScaffoldController
import com.barutdev.tullab.ui.screens.bulk_schedule.BulkScheduleScreen
import com.barutdev.tullab.ui.screens.calendar.CalendarScreen
import com.barutdev.tullab.ui.screens.calendar.CalendarViewModel

import com.barutdev.tullab.ui.screens.dashboard.DashboardScreen
import com.barutdev.tullab.ui.screens.homework.HomeworkScreen
import com.barutdev.tullab.ui.screens.reports.ReportsScreen
import com.barutdev.tullab.ui.screens.settings.SettingsScreen
import com.barutdev.tullab.ui.screens.onboarding.OnboardingScreen
import com.barutdev.tullab.ui.screens.student_list.StudentListScreen
import com.barutdev.tullab.ui.screens.student_profile.AddStudentProfileScreen
import com.barutdev.tullab.ui.screens.student_profile.EditStudentProfileScreen
import com.barutdev.tullab.util.tullabStringResource
import java.util.Locale

private const val SLIDE_DURATION_MS = 350
private const val FADE_DURATION_MS = 300
private const val NAVIGATION_LOG_TAG = "TullabNavigation"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TullabNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    scaffoldController: TullabScaffoldController = rememberTullabScaffoldController()
) {
    val startDestinationViewModel: StartDestinationViewModel = hiltViewModel()
    val startRoute: String? by startDestinationViewModel.startRoute.collectAsState(initial = null)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentStudentId = navBackStackEntry.studentIdOrNull()
    val preloadViewModel: BottomNavPreloadViewModel = hiltViewModel()
    val bottomNavTransitionState = remember { BottomNavTransitionState() }
    val performanceLogger = remember { NavigationPerformanceLogger() }
    val bottomBarState = remember { BottomBarState() }

    LaunchedEffect(currentStudentId) {
        if (currentStudentId != null) {
            preloadViewModel.prime(currentStudentId)
        }
    }

    LaunchedEffect(currentDestination) {
        if (!bottomNavTransitionState.isPrimed && currentDestination.isBottomBarDestination()) {
            bottomNavTransitionState.markPrimedAfterFirstFrames()
        }
        performanceLogger.onRouteChanged(currentDestination?.route)
    }

    LaunchedEffect(currentDestination, currentStudentId) {
        val studentScoped = currentDestination.asStudentScopedDestination()
        val studentId = currentStudentId
        if (studentScoped != null && studentId != null) {
            bottomBarState.record(studentScoped, studentId)
        }
    }

    val bottomBarVisible = shouldShowBottomBar(currentDestination)

    val onNavigateToStudentList = remember(navController) {
        {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != TullabDestination.StudentList.route) {
                navController.navigate(TullabDestination.StudentList.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }
    val onNavigateToSettings = remember(navController) {
        {
            navController.navigate(TullabDestination.Settings.route) {
                launchSingleTop = true
            }
        }
    }

    val topBarConfig by scaffoldController.topBarConfig
    val navigationContentDescription = tullabStringResource(
        id = R.string.top_bar_navigate_to_student_list_content_description
    )
    val settingsContentDescription = tullabStringResource(
        id = R.string.dashboard_settings_icon_description
    )
    val studentScopedDestination = currentDestination.asStudentScopedDestination()
    val fallbackTitleRes = when {
        studentScopedDestination != null -> studentScopedDestination.labelRes
        currentDestination?.route == TullabDestination.Settings.route -> TullabDestination.Settings.labelRes
        currentDestination?.route == TullabDestination.StudentList.route -> TullabDestination.StudentList.labelRes
        else -> R.string.app_name
    }
    val fallbackTitle = tullabStringResource(id = fallbackTitleRes)
    val defaultNavigationAction = remember(
        studentScopedDestination,
        currentStudentId,
        navigationContentDescription,
        onNavigateToStudentList
    ) {
        if (studentScopedDestination != null && currentStudentId != null) {
            TopBarAction(
                icon = Icons.Outlined.Groups,
                contentDescription = navigationContentDescription,
                onClick = onNavigateToStudentList
            )
        } else {
            null
        }
    }
    val defaultSettingsAction = remember(
        settingsContentDescription,
        onNavigateToSettings
    ) {
        TopBarAction(
            icon = Icons.Filled.Settings,
            contentDescription = settingsContentDescription,
            onClick = onNavigateToSettings
        )
    }
    val topBarState by remember(
        topBarConfig,
        fallbackTitle,
        defaultNavigationAction,
        defaultSettingsAction
    ) {
        derivedStateOf {
            val actions = topBarConfig?.actions
                ?.takeIf { it.isNotEmpty() }
                ?: listOf(defaultSettingsAction)
            TullabTopBarState(
                title = topBarConfig?.title ?: fallbackTitle,
                navigationIcon = topBarConfig?.navigationIcon ?: defaultNavigationAction,
                actions = actions
            )
        }
    }

    CompositionLocalProvider(LocalTullabScaffoldController provides scaffoldController) {
        Scaffold(
            modifier = modifier,
            topBar = { TullabTopBar(state = topBarState) },
            bottomBar = {
                if (bottomBarVisible) {
                    TullabBottomNavigation(
                        currentDestination = currentDestination,
                        onNavigate = { destination ->
                            when (destination) {
                                is TullabDestination.StudentScoped -> {
                                    val targetStudentId = currentStudentId
                                        ?: bottomBarState.lastStudentId(destination)
                                        ?: bottomBarState.lastKnownStudentId()
                                    if (targetStudentId != null) {
                                        navController.navigateToStudentScoped(
                                            destination = destination,
                                            studentId = targetStudentId,
                                            bottomBarState = bottomBarState
                                        )
                                    } else {
                                        Log.w(
                                            NAVIGATION_LOG_TAG,
                                            "Skipping navigation to ${destination.baseRoute} because no student context is available"
                                        )
                                    }
                                }
                                else -> Unit
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                val fabConfig = scaffoldController.fabConfig.value
                if (fabConfig != null) {
                    FloatingActionButton(
                        onClick = fabConfig.onClick,
                        containerColor = fabConfig.containerColor
                            ?: MaterialTheme.colorScheme.primary,
                        contentColor = fabConfig.contentColor
                            ?: MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = fabConfig.icon,
                            contentDescription = fabConfig.contentDescription
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = scaffoldController.snackbarHostState) }
        ) { innerPadding ->
            if (startRoute == null) {
                Box(
                    modifier = Modifier.padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                NavHost(
                    navController = navController,
                    startDestination = startRoute!!,
                    modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    if (bottomNavTransitionState.shouldAnimate(
                            initialState.destination,
                            targetState.destination
                        )
                    ) {
                        tullabEnterTransition()
                    } else {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = FADE_DURATION_MS,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    }
                },
                exitTransition = {
                    if (bottomNavTransitionState.shouldAnimate(
                            initialState.destination,
                            targetState.destination
                        )
                    ) {
                        tullabExitTransition()
                    } else {
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = FADE_DURATION_MS,
                                easing = FastOutLinearInEasing
                            )
                        )
                    }
                },
                popEnterTransition = {
                    if (bottomNavTransitionState.shouldAnimate(
                            initialState.destination,
                            targetState.destination
                        )
                    ) {
                        tullabPopEnterTransition()
                    } else {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = FADE_DURATION_MS,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    }
                },
                popExitTransition = {
                    if (bottomNavTransitionState.shouldAnimate(
                            initialState.destination,
                            targetState.destination
                        )
                    ) {
                        tullabPopExitTransition()
                    } else {
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = FADE_DURATION_MS,
                                easing = FastOutLinearInEasing
                            )
                        )
                    }
                }
            ) {
                composable(
                    route = TullabDestination.Onboarding.route
                ) {
                    OnboardingScreen(
                        onCompleted = {
                            navController.navigate(TullabDestination.StudentList.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(
                    route = TullabDestination.StudentList.route
                ) {
                    StudentListScreen(
                        onAddStudent = {
                            navController.navigate(TullabDestination.AddStudentProfile.route)
                        },
                        onStudentClick = { studentId ->
                            studentId.toIntOrNull()?.let { id ->
                                navController.navigateToStudentScoped(
                                    destination = TullabDestination.Dashboard,
                                    studentId = id,
                                    bottomBarState = bottomBarState
                                )
                            }
                        },
                        onEditStudentProfile = { studentId ->
                            navController.navigate(
                                TullabDestination.EditStudentProfile.createRoute(studentId)
                            )
                        },
                        onNavigateToReports = {
                            navController.navigateToReports()
                        },
                        onNavigateToSettings = onNavigateToSettings
                    )
                }

                composable(
                    route = TullabDestination.AddStudentProfile.route
                ) {
                    AddStudentProfileScreen(
                        onBack = { navController.popBackStack() },
                        onProfileSaved = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = TullabDestination.Dashboard.route,
                    arguments = TullabDestination.Dashboard.arguments()
                ) { backStackEntry ->
                    val studentId = backStackEntry.requireStudentId()
                    Log.d(
                        NAVIGATION_LOG_TAG,
                        "Rendering Dashboard entry=${backStackEntry.id} for studentId=$studentId"
                    )
                    key("dashboard-$studentId") {
                        DashboardScreen(
                            expectedStudentId = studentId,
                            onNavigateToSettings = {
                                navController.navigate(TullabDestination.Settings.route) {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToStudentList = {
                                navController.navigate(TullabDestination.StudentList.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                            onEditStudentProfile = { id ->
                                navController.navigate(
                                    TullabDestination.EditStudentProfile.createRoute(id)
                                )
                            }
                        )
                    }
                }

                composable(
                    route = TullabDestination.Calendar.route,
                    arguments = TullabDestination.Calendar.arguments(),
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern = "app://tullab/studentCalendar/{$STUDENT_ID_ARG}"
                        }
                    )
                ) { backStackEntry ->
                    val studentId = backStackEntry.requireStudentId()
                    Log.d(
                        NAVIGATION_LOG_TAG,
                        "Rendering Calendar entry=${backStackEntry.id} for studentId=$studentId"
                    )
                    val calendarViewModel: CalendarViewModel = hiltViewModel(
                        key = "calendar-$studentId"
                    )
                    
                    val savedStateHandle = backStackEntry.savedStateHandle
                    val bulkCreated by savedStateHandle.getStateFlow<Int?>("bulk_created", null).collectAsState()
                    val bulkSkipped by savedStateHandle.getStateFlow<Int?>("bulk_skipped", null).collectAsState()
                    val bulkIds by savedStateHandle.getStateFlow<IntArray?>("bulk_ids", null).collectAsState()
                    
                    val undoActionLabel = tullabStringResource(R.string.bulk_schedule_undo_action)
                    val successMsgCreated = bulkCreated?.let { tullabPluralResource(R.plurals.bulk_schedule_success_created, it, it) } ?: ""
                    val successMsgSkipped = bulkSkipped?.let { if (it > 0) " " + tullabPluralResource(R.plurals.bulk_schedule_success_skipped, it, it) else "" } ?: ""
                    val successMsg = successMsgCreated + successMsgSkipped

                    val snackbarHostState = scaffoldController.snackbarHostState
                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(bulkCreated, bulkSkipped, bulkIds) {
                        val currentBulkCreated = bulkCreated
                        val currentBulkIds = bulkIds
                        if (currentBulkCreated != null && currentBulkIds != null) {
                            calendarViewModel.setBatchUndoSession(
                                com.barutdev.tullab.domain.model.BatchUndoSession(studentId, currentBulkIds.toList())
                            )
                            savedStateHandle.remove<Int>("bulk_created")
                            savedStateHandle.remove<Int>("bulk_skipped")
                            savedStateHandle.remove<IntArray>("bulk_ids")
                            
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = successMsg,
                                    actionLabel = if (currentBulkCreated > 0) undoActionLabel else null,
                                    duration = androidx.compose.material3.SnackbarDuration.Long
                                )
                                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                    calendarViewModel.undoBulkLessons()
                                }
                            }
                        }
                    }

                    val undoSuccess by calendarViewModel.batchUndoSession.collectAsState()
                    LaunchedEffect(undoSuccess) {
                        // We need a separate state if undo succeeds, but the usecase returns result.
                        // Actually CalendarViewModel handles undo, we can show a snackbar when it succeeds.
                    }

                    key("calendar-$studentId") {
                        CalendarScreen(
                            expectedStudentId = studentId,
                            viewModel = calendarViewModel,
                            onNavigateToStudentList = {
                                navController.navigate(TullabDestination.StudentList.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToHomework = { sId, hId ->
                                navController.navigate(TullabDestination.Homework.createRoute(sId, hId))
                            },
                            onNavigateToBulkSchedule = { sId ->
                                navController.navigate(TullabDestination.BulkSchedule.createRoute(sId))
                            }
                        )
                    }
                }

                composable(
                    route = TullabDestination.Homework.route,
                    arguments = TullabDestination.Homework.arguments()
                ) { backStackEntry ->
                    val studentId = backStackEntry.requireStudentId()
                    val homeworkId = backStackEntry.arguments?.getInt(HOMEWORK_ID_ARG)?.takeIf { it != -1 }
                    Log.d(
                        NAVIGATION_LOG_TAG,
                        "Rendering Homework entry=${backStackEntry.id} for studentId=$studentId homeworkId=$homeworkId"
                    )
                    key("homework-$studentId") {
                        HomeworkScreen(
                            expectedStudentId = studentId,
                            homeworkId = homeworkId,
                            onNavigateToStudentList = {
                                navController.navigate(TullabDestination.StudentList.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                composable(
                    route = TullabDestination.Reports.route
                ) { backStackEntry ->
                    Log.d(
                        NAVIGATION_LOG_TAG,
                        "Rendering Reports entry=${backStackEntry.id} (global)"
                    )
                    ReportsScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                composable(
                    route = TullabDestination.EditStudentProfile.route,
                    arguments = TullabDestination.EditStudentProfile.arguments()
                ) {
                    EditStudentProfileScreen(
                        onBack = { navController.popBackStack() },
                        onProfileSaved = { navController.popBackStack() }
                    )
                }

                composable(
                    route = TullabDestination.BulkSchedule.route,
                    arguments = TullabDestination.BulkSchedule.arguments()
                ) { backStackEntry ->
                    val studentId = backStackEntry.requireStudentId()
                    Log.d(
                        NAVIGATION_LOG_TAG,
                        "Rendering BulkSchedule entry=${backStackEntry.id} for studentId=$studentId"
                    )
                    key("bulk_schedule-$studentId") {
                        com.barutdev.tullab.ui.screens.bulk_schedule.BulkScheduleScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateBackWithResult = { created, skipped, ids ->
                                navController.previousBackStackEntry?.savedStateHandle?.set("bulk_created", created)
                                navController.previousBackStackEntry?.savedStateHandle?.set("bulk_skipped", skipped)
                                navController.previousBackStackEntry?.savedStateHandle?.set("bulk_ids", ids)
                                navController.popBackStack()
                            }
                        )
                    }
                }

                composable(
                    route = TullabDestination.Settings.route
                ) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
}

private data class TullabTopBarState(
    val title: String,
    val navigationIcon: TopBarAction?,
    val actions: List<TopBarAction>
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TullabTopBar(state: TullabTopBarState) {
    LaunchedEffect(state.title) {
        Log.d(NAVIGATION_LOG_TAG, "TullabTopBar visible with title=${state.title}")
    }
    TopAppBar(
        title = { Text(text = state.title) },
        navigationIcon = {
            val navigation = state.navigationIcon
            if (navigation != null) {
                IconButton(onClick = navigation.onClick) {
                    Icon(
                        imageVector = navigation.icon,
                        contentDescription = navigation.contentDescription
                    )
                }
            }
        },
        actions = {
            state.actions.forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription
                    )
                }
            }
        }
    )
}

@Composable
private fun TullabBottomNavigation(
    currentDestination: NavDestination?,
    onNavigate: (TullabDestination) -> Unit
) {
    NavigationBar {
        TullabDestination.bottomBarDestinations.forEach { destination ->
            val isSelected = currentDestination.isDestination(destination)
            val label = tullabStringResource(id = destination.labelRes)
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onNavigate(destination)
                    }
                },
                icon = {
                    Icon(
                        imageVector = destination.icon(),
                        contentDescription = label
                    )
                },
                label = { Text(text = label) }
            )
        }
    }
}

@VisibleForTesting
internal fun shouldShowBottomBar(
    currentDestination: NavDestination?
): Boolean = currentDestination.isBottomBarDestination()

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tullabEnterTransition(): EnterTransition {
    val direction = resolveSlideDirection(initialState.destination.route, targetState.destination.route)
    return if (direction != null) {
        slideInHorizontally(
            animationSpec = tween(
                durationMillis = SLIDE_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            initialOffsetX = { fullWidth ->
                val offset = (fullWidth * 0.25f).toInt()
                when (direction) {
                    AnimatedContentTransitionScope.SlideDirection.Left -> offset
                    AnimatedContentTransitionScope.SlideDirection.Right -> -offset
                    else -> offset
                }
            }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )
    } else {
        fadeIn(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tullabExitTransition(): ExitTransition {
    val direction = resolveSlideDirection(initialState.destination.route, targetState.destination.route)
    return if (direction != null) {
        slideOutHorizontally(
            animationSpec = tween(
                durationMillis = SLIDE_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            targetOffsetX = { fullWidth ->
                val offset = (fullWidth * 0.25f).toInt()
                when (direction) {
                    AnimatedContentTransitionScope.SlideDirection.Left -> -offset
                    AnimatedContentTransitionScope.SlideDirection.Right -> offset
                    else -> -offset
                }
            }
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )
    } else {
        fadeOut(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tullabPopEnterTransition(): EnterTransition {
    val direction = resolveSlideDirection(initialState.destination.route, targetState.destination.route)
    return if (direction != null) {
        slideInHorizontally(
            animationSpec = tween(
                durationMillis = SLIDE_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            initialOffsetX = { fullWidth ->
                val offset = (fullWidth * 0.25f).toInt()
                when (direction) {
                    AnimatedContentTransitionScope.SlideDirection.Left -> offset
                    AnimatedContentTransitionScope.SlideDirection.Right -> -offset
                    else -> offset
                }
            }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )
    } else {
        fadeIn(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tullabPopExitTransition(): ExitTransition {
    val direction = resolveSlideDirection(initialState.destination.route, targetState.destination.route)
    return if (direction != null) {
        slideOutHorizontally(
            animationSpec = tween(
                durationMillis = SLIDE_DURATION_MS,
                easing = FastOutSlowInEasing
            ),
            targetOffsetX = { fullWidth ->
                val offset = (fullWidth * 0.25f).toInt()
                when (direction) {
                    AnimatedContentTransitionScope.SlideDirection.Left -> -offset
                    AnimatedContentTransitionScope.SlideDirection.Right -> offset
                    else -> -offset
                }
            }
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )
    } else {
        fadeOut(
            animationSpec = tween(
                durationMillis = FADE_DURATION_MS,
                easing = FastOutSlowInEasing
            )
        )
    }
}

@VisibleForTesting
internal fun resolveSlideDirection(
    initialRoute: String?,
    targetRoute: String?
): AnimatedContentTransitionScope.SlideDirection? {
    val initial = TullabDestination.studentScopedFromRoute(initialRoute)
        ?: TullabDestination.fromRoute(initialRoute)
    val target = TullabDestination.studentScopedFromRoute(targetRoute)
        ?: TullabDestination.fromRoute(targetRoute)
    if (initial == null || target == null || initial == target) return null
    val initialIndex = TullabDestination.bottomBarDestinations.indexOf(initial)
    val targetIndex = TullabDestination.bottomBarDestinations.indexOf(target)
    if (initialIndex == -1 || targetIndex == -1) return null
    return if (targetIndex > initialIndex) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }
}

private fun NavDestination?.isBottomBarDestination(): Boolean {
    val route = this?.route ?: return false
    return this.asStudentScopedDestination() != null
}

private fun NavDestination?.asStudentScopedDestination(): TullabDestination.StudentScoped? =
    TullabDestination.studentScopedFromRoute(this?.route)

private fun NavDestination?.isDestination(destination: TullabDestination): Boolean {
    val currentRoute = this?.route ?: return false
    if (currentRoute == destination.route) return true
    return when (destination) {
        is TullabDestination.StudentScoped -> currentRoute.startsWith(destination.baseRoute)
        else -> false
    }
}

private fun TullabDestination.icon(): ImageVector = when (this) {
    TullabDestination.Dashboard -> Icons.Outlined.Dashboard
    TullabDestination.Calendar -> Icons.Outlined.CalendarMonth
    TullabDestination.Homework -> Icons.Outlined.Assignment
    TullabDestination.EditStudentProfile -> Icons.Outlined.Dashboard
    else -> Icons.Outlined.Dashboard
}

private fun NavBackStackEntry?.studentIdOrNull(): Int? {
    if (this == null) return null
    return arguments
        ?.takeIf { it.containsKey(STUDENT_ID_ARG) }
        ?.getInt(STUDENT_ID_ARG)
}

private fun NavBackStackEntry.requireStudentId(): Int {
    val resolvedId = checkNotNull(arguments?.getInt(STUDENT_ID_ARG)) {
        "Destination ${destination.route} requires $STUDENT_ID_ARG argument"
    }
    Log.d(
        NAVIGATION_LOG_TAG,
        "BackStackEntry id=$id route=${destination.route} resolvedStudentId=$resolvedId"
    )
    return resolvedId
}

private fun NavHostController.navigateToStudentScoped(
    destination: TullabDestination.StudentScoped,
    studentId: Int,
    bottomBarState: BottomBarState
) {
    if (isCurrentBottomBarDestination(destination, studentId)) {
        return
    }
    val shouldRestoreState = bottomBarState.shouldRestore(destination, studentId)
    if (!shouldRestoreState) {
        bottomBarState.clear(destination)
    }

    val route = destination.createRoute(studentId)
    val recordedStudentId = bottomBarState.lastStudentId(destination)
    val shouldLaunchSingleTop = recordedStudentId == studentId && shouldRestoreState
    Log.d(
        NAVIGATION_LOG_TAG,
        "navigateToStudentScoped route=$route restore=$shouldRestoreState " +
            "singleTop=$shouldLaunchSingleTop recorded=$recordedStudentId"
    )

    navigate(route) {
        launchSingleTop = shouldLaunchSingleTop
        restoreState = false
        popUpTo(graph.findStartDestination().id) {
            saveState = false
        }
    }
}

private fun NavHostController.navigateToReports() {
    if (currentDestination?.route == TullabDestination.Reports.route) return
    Log.d(NAVIGATION_LOG_TAG, "navigateToReports route=${TullabDestination.Reports.route}")
    navigate(TullabDestination.Reports.route) {
        launchSingleTop = true
        restoreState = false
        popUpTo(graph.findStartDestination().id) {
            saveState = false
        }
    }
}

private fun NavHostController.isCurrentBottomBarDestination(
    destination: TullabDestination.StudentScoped,
    studentId: Int
): Boolean {
    val currentEntry = currentBackStackEntry ?: return false
    if (!currentEntry.destination.isDestination(destination)) return false
    return currentEntry.arguments
        ?.takeIf { it.containsKey(STUDENT_ID_ARG) }
        ?.getInt(STUDENT_ID_ARG) == studentId
}

private class NavigationPerformanceLogger {
    private var lastTimestampNanos: Long = SystemClock.elapsedRealtimeNanos()
    private var lastRoute: String? = null

    fun onRouteChanged(route: String?) {
        if (route == lastRoute) return
        val now = SystemClock.elapsedRealtimeNanos()
        val deltaMs = (now - lastTimestampNanos) / 1_000_000.0
        val formatted = String.format(Locale.US, "%.1f", deltaMs)
        Log.d(NAVIGATION_LOG_TAG, "Route change to $route took $formatted ms")
        lastRoute = route
        lastTimestampNanos = now
    }
}
