package com.miaomiao.jizhang.feature.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miaomiao.jizhang.core.common.DateUtils
import com.miaomiao.jizhang.core.data.repository.SettingsRepository
import com.miaomiao.jizhang.core.ui.components.SectionCard
import kotlinx.coroutines.launch

/** 设置页。 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val vm: SettingsViewModel = hiltViewModel()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()
    val currency by vm.currencySymbol.collectAsStateWithLifecycle()
    val monthStartDay by vm.monthStartDay.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var confirmImport by remember { mutableStateOf<String?>(null) }

    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = vm.exportCsv()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(text.toByteArray(Charsets.UTF_8))
                }
                toast("CSV 已导出")
            }
        }
    }

    val jsonExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = vm.exportJson()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(text.toByteArray(Charsets.UTF_8))
                }
                toast("JSON 备份已导出")
            }
        }
    }

    val jsonImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?.toString(Charsets.UTF_8)
                if (text != null) confirmImport = text
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "设置",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // 外观
            SectionCard(Modifier.fillMaxWidth()) {
                SettingSectionTitle(Icons.Rounded.Palette, "外观")
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    ThemeOption("跟随系统", themeMode == SettingsRepository.THEME_SYSTEM, Modifier.weight(1f)) {
                        vm.setThemeMode(SettingsRepository.THEME_SYSTEM)
                    }
                    ThemeOption("浅色", themeMode == SettingsRepository.THEME_LIGHT, Modifier.weight(1f)) {
                        vm.setThemeMode(SettingsRepository.THEME_LIGHT)
                    }
                    ThemeOption("深色", themeMode == SettingsRepository.THEME_DARK, Modifier.weight(1f)) {
                        vm.setThemeMode(SettingsRepository.THEME_DARK)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // 通用
            SectionCard(Modifier.fillMaxWidth()) {
                SettingSectionTitle(Icons.Rounded.Restore, "通用")
                Spacer(Modifier.height(8.dp))
                Text(
                    "货币符号",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = currency,
                    onValueChange = vm::setCurrencySymbol,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("如 ¥、$、€") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "每月起始日（预算周期从该日开始）",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..28).forEach { day ->
                        StartDayChip(
                            day = day,
                            selected = day == monthStartDay,
                            onClick = { vm.setMonthStartDay(day) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // 数据备份
            SectionCard(Modifier.fillMaxWidth()) {
                SettingSectionTitle(Icons.Rounded.Backup, "数据备份")
                Spacer(Modifier.height(4.dp))
                DataActionRow(
                    icon = Icons.Rounded.FileDownload,
                    title = "导出 CSV",
                    subtitle = "账单明细，可用 Excel 打开"
                ) {
                    csvLauncher.launch("喵喵记账单_${DateUtils.todayStr()}.csv")
                }
                EntryDivider()
                DataActionRow(
                    icon = Icons.Rounded.FileDownload,
                    title = "导出完整备份（JSON）",
                    subtitle = "含分类、账户、预算、猫咪数据"
                ) {
                    jsonExportLauncher.launch("喵喵记账备份_${DateUtils.todayStr()}.json")
                }
                EntryDivider()
                DataActionRow(
                    icon = Icons.Rounded.FileUpload,
                    title = "导入恢复（JSON）",
                    subtitle = "将清空当前数据后恢复备份",
                    tint = MaterialTheme.colorScheme.error
                ) {
                    jsonImportLauncher.launch(arrayOf("application/json"))
                }
            }
            Spacer(Modifier.height(16.dp))

            Text(
                text = "数据仅保存在本机，离线可用。",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    // 导入确认
    confirmImport?.let { json ->
        AlertDialog(
            onDismissRequest = { confirmImport = null },
            title = { Text("导入备份？") },
            text = { Text("导入会清空当前全部数据，并替换为备份内容。此操作不可撤销，建议先导出备份。") },
            confirmButton = {
                TextButton(onClick = {
                    val ok = runCatching {
                        kotlinx.coroutines.runBlocking { vm.importJson(json) }
                    }.getOrDefault(false)
                    confirmImport = null
                    toast(if (ok) "导入成功" else "导入失败：文件格式不正确")
                }) {
                    Text("导入", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmImport = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun SettingSectionTitle(
    icon: ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (active) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun StartDayChip(
    day: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 38.dp, height = 32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$day",
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DataActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EntryDivider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}
