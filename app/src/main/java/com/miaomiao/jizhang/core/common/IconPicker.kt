package com.miaomiao.jizhang.core.common

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * 分类/账户自定义图标：把用户选中的图片复制到应用私有目录，
 * 返回 "file:<绝对路径>" 字符串存入库中；CategoryAvatar 按此渲染。
 */
object IconPicker {

    /** 保存图片，返回 "file:" + 绝对路径；失败返回 null。 */
    fun saveImage(context: Context, uri: Uri, dirName: String): String? {
        return try {
            val dir = File(context.filesDir, dirName).apply { mkdirs() }
            val file = File(dir, "icon_${System.currentTimeMillis()}.png")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            "file:" + file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /** 是否为本地图片图标路径。 */
    fun isImageIcon(icon: String): Boolean = icon.startsWith("file:")
}
