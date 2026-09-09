package com.barutdev.tullab.analytics

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.barutdev.tullab.navigation.TullabDestination
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Centralized Firebase Analytics helpers for reliable real-time reporting.
 *
 * - [screenNameForRoute] sanitizes navigation routes so raw IDs (e.g. `dashboard/12`)
 *   are never logged. Only stable, low-cardinality screen names are reported.
 * - [logScreenView] emits a manual `screen_view` event per navigation destination.
 * - [logAppOpen] emits an explicit `app_open` on cold start and guarantees
 *   collection is enabled regardless of build type.
 */
object AnalyticsTracker {

    /**
     * Resolve a stable screen name for a NavController destination route.
     *
     * Student-scoped routes (`dashboard/{studentId}` rendered as `dashboard/1`)
     * resolve to their [TullabDestination.StudentScoped.baseRoute] so we avoid
     * high-cardinality values and never log student IDs (PII).
     * Static routes are returned as-is (query params stripped).
     */
    fun screenNameForRoute(route: String?): String? {
        if (route.isNullOrBlank()) return null
        // Prefer the canonical base route for student-scoped destinations.
        TullabDestination.studentScopedFromRoute(route)?.let { return it.baseRoute }
        TullabDestination.fromRoute(route)?.let { return it.route }
        // Fallback for unknown routes: strip args/query but keep the base path.
        return route.substringBefore("?").substringBefore("/").ifBlank { null }
    }

    /** Build the [Bundle] payload for a manual `screen_view` event. */
    @VisibleForTesting
    fun screenViewParams(screenName: String): Bundle = Bundle().apply {
        putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
    }

    /**
     * Log a manual `screen_view` for the given raw navigation route.
     * No-op when the route cannot be resolved to a stable screen name.
     */
    fun logScreenView(analytics: FirebaseAnalytics, route: String?) {
        val screenName = screenNameForRoute(route) ?: return
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, screenViewParams(screenName))
    }

    /**
     * Enable collection unconditionally (never gated by build type) and emit an
     * explicit `app_open`. Automatic `app_open` can be delayed or dropped in
     * release builds with R8/AD_ID stripping, so an explicit event guarantees
     * real-time visibility in DebugView / Realtime dashboards.
     *
     * Works without the AD_ID permission: when `google_analytics_adid_collection_enabled`
     * is false (see AndroidManifest), the SDK reports without an advertising ID.
     */
    fun logAppOpen(analytics: FirebaseAnalytics) {
        analytics.setAnalyticsCollectionEnabled(true)
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
    }
}
