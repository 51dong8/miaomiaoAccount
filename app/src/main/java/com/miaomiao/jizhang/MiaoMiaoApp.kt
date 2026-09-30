package com.miaomiao.jizhang

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.miaomiao.jizhang.core.background.WorkManagerScheduler
import com.miaomiao.jizhang.core.common.NotificationHelper
import com.miaomiao.jizhang.core.data.BudgetChecker
import com.miaomiao.jizhang.core.data.repository.RecurringRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltAndroidApp
class MiaoMiaoApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var recurringRepository: RecurringRepository

    @Inject
    lateinit var budgetChecker: BudgetChecker

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)

        // 启动时立即执行：补生成周期账单 + 预算超支检查（当日到期不落账，保证首次安装当天不重复记账）
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching {
                recurringRepository.generateDue(LocalDate.now())
                val check = budgetChecker.check(YearMonth.now())
                NotificationHelper.notifyIfOverBudget(this@MiaoMiaoApp, check)
            }
        }

        // 调度每日后台任务
        WorkManagerScheduler.scheduleDaily(this)
        WorkManagerScheduler.runImmediately(this)
    }
}
