package com.miaomiao.jizhang

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miaomiao.jizhang.core.data.repository.SettingsRepository
import com.miaomiao.jizhang.core.ui.navigation.AppRoot
import com.miaomiao.jizhang.core.ui.theme.MiaoMiaoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle(
                initialValue = SettingsRepository.THEME_SYSTEM
            )
            MiaoMiaoTheme(themeMode = themeMode) {
                AppRoot()
            }
        }
    }

    /** Android 13+ 通知权限（预算超支提醒用；拒绝不影响其他功能）。 */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }
}
