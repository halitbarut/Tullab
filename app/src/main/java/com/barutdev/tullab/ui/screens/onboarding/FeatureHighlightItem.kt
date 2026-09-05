package com.barutdev.tullab.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class FeatureHighlightItem(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)
