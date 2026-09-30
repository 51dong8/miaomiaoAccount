package com.miaomiao.jizhang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
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
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle(
                initialValue = SettingsRepository.THEME_SYSTEM
            )
            MiaoMiaoTheme(themeMode = themeMode) {
                AppRoot()
            }
        }
    }
}
