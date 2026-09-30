package com.miaomiao.jizhang.core.background

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** WorkManager 调度：每日一次 + 启动时立即执行一次。 */
object WorkManagerScheduler {

    private const val PERIODIC_WORK = "miaomiao_daily"
    private const val IMMEDIATE_WORK = "miaomiao_daily_now"

    fun scheduleDaily(context: Context) {
        val periodic = PeriodicWorkRequestBuilder<DailyWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodic
        )
    }

    fun runImmediately(context: Context) {
        val now = OneTimeWorkRequestBuilder<DailyWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK,
            ExistingWorkPolicy.REPLACE,
            now
        )
    }
}
