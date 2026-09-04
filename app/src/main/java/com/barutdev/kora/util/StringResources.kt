package com.barutdev.kora.util

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.barutdev.kora.ui.theme.LocalLocale

@Composable
inline fun koraStringResource(
    @StringRes id: Int,
    vararg formatArgs: Any
): String {
    val context = LocalContext.current
    val locale = LocalLocale.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val newConfiguration = Configuration(configuration)
    newConfiguration.setLocale(locale)
    val localizedContext = context.createConfigurationContext(newConfiguration)
    return localizedContext.resources.getString(id, *formatArgs)
}
