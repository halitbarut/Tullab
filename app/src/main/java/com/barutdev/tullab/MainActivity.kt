package com.barutdev.tullab

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barutdev.tullab.navigation.TullabNavGraph
import com.barutdev.tullab.ui.AppViewModel
import com.barutdev.tullab.ui.preferences.LocalUserPreferences
import com.barutdev.tullab.ui.theme.TullabTheme
import com.barutdev.tullab.ui.theme.ProvideLocale
import com.barutdev.tullab.util.ProvideMessageNotifier
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val userPreferences by appViewModel.userPreferences.collectAsStateWithLifecycle()

            ProvideLocale(languageCode = userPreferences.languageCode) {
                CompositionLocalProvider(LocalUserPreferences provides userPreferences) {
                    TullabTheme(darkTheme = userPreferences.isDarkMode) {
                        ProvideMessageNotifier {
                            TullabNavGraph()
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        val isGranted = ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            notificationPermissionLauncher.launch(permission)
        }
    }
}
