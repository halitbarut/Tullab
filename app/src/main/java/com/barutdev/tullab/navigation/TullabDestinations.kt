package com.barutdev.tullab.navigation

import androidx.annotation.StringRes
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.barutdev.tullab.R

const val STUDENT_ID_ARG = "studentId"
const val HOMEWORK_ID_ARG = "homeworkId"

internal sealed class TullabDestination(
    @StringRes val labelRes: Int
) {
    abstract val route: String

    object StudentList : TullabDestination(
        labelRes = R.string.student_list_title
    ) {
        override val route: String = "student_list"
    }

    sealed class StudentScoped(
        internal val baseRoute: String,
        @StringRes labelRes: Int
    ) : TullabDestination(
        labelRes = labelRes
    ) {
        override val route: String get() = "$baseRoute/{$STUDENT_ID_ARG}"

        open fun createRoute(studentId: Int): String = "$baseRoute/$studentId"

        open fun arguments() = listOf(
            navArgument(STUDENT_ID_ARG) {
                type = NavType.IntType
            }
        )
    }

    object Dashboard : StudentScoped(
        baseRoute = "dashboard",
        labelRes = R.string.dashboard_title
    )

    object Calendar : StudentScoped(
        baseRoute = "calendar",
        labelRes = R.string.calendar_title
    )

    object Homework : StudentScoped(
        baseRoute = "homework",
        labelRes = R.string.homework_title
    ) {
        override val route: String = "$baseRoute/{$STUDENT_ID_ARG}?$HOMEWORK_ID_ARG={$HOMEWORK_ID_ARG}"

        fun createRoute(studentId: Int, homeworkId: Int? = null): String {
            return if (homeworkId != null) "$baseRoute/$studentId?$HOMEWORK_ID_ARG=$homeworkId"
            else "$baseRoute/$studentId"
        }

        override fun arguments() = super.arguments() + listOf(
            navArgument(HOMEWORK_ID_ARG) {
                type = NavType.IntType
                defaultValue = -1
            }
        )
    }

    object Reports : TullabDestination(
        labelRes = R.string.reports_tab_label
    ) {
        override val route: String = "reports"
    }

    object EditStudentProfile : StudentScoped(
        baseRoute = "student_profile",
        labelRes = R.string.student_profile_title
    )

    object BulkSchedule : StudentScoped(
        baseRoute = "bulk_schedule",
        labelRes = R.string.bulk_schedule_title
    )

    object AddStudentProfile : TullabDestination(
        labelRes = R.string.add_student_profile_title
    ) {
        override val route: String = "student_profile/add"
    }

    object Settings : TullabDestination(
        labelRes = R.string.settings_title
    ) {
        override val route: String = "settings"
    }

    object Onboarding : TullabDestination(
        labelRes = R.string.onboarding_title
    ) {
        override val route: String = "onboarding"
    }

    companion object {
        val bottomBarDestinations: List<TullabDestination> by lazy(LazyThreadSafetyMode.PUBLICATION) {
            listOf(Dashboard, Calendar, Homework)
        }

        fun fromRoute(route: String?): TullabDestination? = when (route) {
            StudentList.route -> StudentList
            Dashboard.route -> Dashboard
            Calendar.route -> Calendar
            Homework.route -> Homework
            Reports.route -> Reports
            EditStudentProfile.route -> EditStudentProfile
            BulkSchedule.route -> BulkSchedule
            AddStudentProfile.route -> AddStudentProfile
            Settings.route -> Settings
            Onboarding.route -> Onboarding
            else -> null
        }

        fun studentScopedFromRoute(route: String?): StudentScoped? {
            if (route == null) return null
            // AddStudentProfile uses "student_profile/add" which starts with EditStudentProfile.baseRoute
            // so we must exclude it explicitly before checking EditStudentProfile
            if (route == AddStudentProfile.route) return null
            return when {
                route == Dashboard.route || route.startsWith("${Dashboard.baseRoute}/") -> Dashboard
                route == Calendar.route || route.startsWith("${Calendar.baseRoute}/") -> Calendar
                route == Homework.route || route.startsWith("${Homework.baseRoute}/") -> Homework
                route == EditStudentProfile.route || route.startsWith("${EditStudentProfile.baseRoute}/") -> EditStudentProfile
                route == BulkSchedule.route || route.startsWith("${BulkSchedule.baseRoute}/") -> BulkSchedule
                else -> null
            }
        }
    }
}
