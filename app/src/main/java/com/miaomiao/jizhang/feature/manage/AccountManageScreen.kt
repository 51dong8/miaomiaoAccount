package com.miaomiao.jizhang.feature.manage

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miaomiao.jizhang.core.common.IconPicker
import com.miaomiao.jizhang.core.data.entity.AccountEntity
import com.miaomiao.jizhang.core.ui.components.CategoryAvatar

/** 账户管理页。 */
@Composable
fun AccountManageScreen(
    onBack: () -> Unit
) {
    val vm: AccountManageViewModel = hiltViewModel()
    val accounts by vm.accounts.collectAsStateWithLifecycle()

    var editing by remember { mutableStateOf<AccountEntity?>(null) }
    var adding by remember { mutableStateOf(false) }

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
                text = "账户管理",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { adding = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "添加账户", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Text(
            text = "归档的账户不会出现在记账选择中，历史账单不受影响。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(accounts, key = { it.id }) { account ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { editing = account }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryAvatar(
                        icon = account.icon,
                        color = Color(account.color),
                        size = 40.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (account.isArchived) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (account.isArchived) {
                            Text(
                                text = "已归档",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = !account.isArchived,
                        onCheckedChange = { enabled ->
                            vm.setArchived(account, archived = !enabled)
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (adding) {
        AccountEditDialog(
            title = "添加账户",
            initialName = "",
            initialIcon = "💳",
            initialColor = androidx.compose.ui.graphics.Color(0xFF1677FF),
            onDismiss = { adding = false },
            onConfirm = { name, icon, color ->
                vm.add(name, icon, color)
                adding = false
            }
        )
    }

    editing?.let { account ->
        AccountEditDialog(
            title = "编辑账户",
            initialName = account.name,
            initialIcon = account.icon,
            initialColor = Color(account.color),
            onDismiss = { editing = null },
            onConfirm = { name, icon, color ->
                vm.update(account, name, icon, color)
                editing = null
            }
        )
    }
}

@Composable
private fun AccountEditDialog(
    title: String,
    initialName: String,
    initialIcon: String,
    initialColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var icon by remember { mutableStateOf(initialIcon) }
    var color by remember { mutableStateOf(initialColor) }
    val context = LocalContext.current
    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { icon = IconPicker.saveImage(context, it, "account_icons") ?: icon }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 6) name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("图标", style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { pickLauncher.launch("image/*") },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("上传图片", style = MaterialTheme.typography.labelSmall)
                        }
                        if (IconPicker.isImageIcon(icon)) {
                            TextButton(
                                onClick = { icon = "💳" },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("清除", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AccountEmojis.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    if (icon == emoji) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { icon = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 16.sp)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("颜色", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    com.miaomiao.jizhang.core.ui.theme.CategoryPalette.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(c)
                                .then(
                                    if (color == c) {
                                        Modifier
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(c)
                                            .padding(3.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(MaterialTheme.colorScheme.onBackground)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { color = c }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onConfirm(name.trim(), icon, color.value.toLong())
            }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
