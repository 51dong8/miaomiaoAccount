package com.miaomiao.jizhang.core.common

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.miaomiao.jizhang.R
import com.miaomiao.jizhang.core.data.BudgetCheckResult

/** 预算超支系统通知。 */
object NotificationHelper {

    const val CHANNEL_ID = "budget_alerts"
    private const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "预算提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "月预算或分类预算超支提醒" }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /** 若超支则发送系统通知；未授权通知权限或未超支则静默返回。 */
    fun notifyIfOverBudget(context: Context, result: BudgetCheckResult) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)

        val lines = mutableListOf<String>()
        if (result.overMonthly) {
            lines.add(
                "本月已支出 ${MoneyFormatter.format(result.monthlySpentFen)}，超预算 ${MoneyFormatter.format(result.monthlySpentFen - result.monthlyBudgetFen)}"
            )
        }
        if (result.overCategoryIds.isNotEmpty()) {
            lines.add("有 ${result.overCategoryIds.size} 个分类预算超支")
        }
        if (lines.isEmpty()) return

        builder
            .setContentTitle("喵喵记 · 预算超支提醒")
            .setContentText(lines.joinToString("；"))
            .setStyle(NotificationCompat.BigTextStyle().bigText(lines.joinToString("\n")))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // 无通知权限，忽略
        }
    }
}
