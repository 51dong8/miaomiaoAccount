package com.miaomiao.jizhang.core.background

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.miaomiao.jizhang.core.common.NotificationHelper
import com.miaomiao.jizhang.core.data.BudgetChecker
import com.miaomiao.jizhang.core.data.repository.RecurringRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.YearMonth

/**
 * 每日后台任务：
 * 1. 为所有启用的周期账单规则补生成到期账单；
 * 2. 检查月预算/分类预算是否超支，超支则发系统通知。
 */
@HiltWorker
class DailyWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val recurringRepository: RecurringRepository,
    private val budgetChecker: BudgetChecker
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            NotificationHelper.createChannel(applicationContext)
            val today = LocalDate.now()
            recurringRepository.generateDue(today)
            val check = budgetChecker.check(YearMonth.from(today))
            NotificationHelper.notifyIfOverBudget(applicationContext, check)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
