package com.miaomiao.jizhang.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.miaomiao.jizhang.core.data.dao.AccountDao
import com.miaomiao.jizhang.core.data.dao.BudgetDao
import com.miaomiao.jizhang.core.data.dao.CategoryDao
import com.miaomiao.jizhang.core.data.dao.CatStateDao
import com.miaomiao.jizhang.core.data.dao.RecurringRuleDao
import com.miaomiao.jizhang.core.data.dao.TransactionDao
import com.miaomiao.jizhang.core.data.entity.AccountEntity
import com.miaomiao.jizhang.core.data.entity.BudgetEntity
import com.miaomiao.jizhang.core.data.entity.CategoryEntity
import com.miaomiao.jizhang.core.data.entity.CatStateEntity
import com.miaomiao.jizhang.core.data.entity.RecurringRuleEntity
import com.miaomiao.jizhang.core.data.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        BudgetEntity::class,
        CatStateEntity::class,
        RecurringRuleEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun catStateDao(): CatStateDao
    abstract fun recurringRuleDao(): RecurringRuleDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /** v1 → v2：新增周期账单规则表 */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recurring_rules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        categoryId INTEGER NOT NULL,
                        accountId INTEGER NOT NULL,
                        note TEXT NOT NULL,
                        frequency TEXT NOT NULL,
                        dayOfWeek INTEGER,
                        dayOfMonth INTEGER,
                        startDate TEXT NOT NULL,
                        lastGeneratedDate TEXT NOT NULL,
                        active INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "miaomiao.db"
            )
                .addCallback(SeedCallback())
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
    }

    /** 首次创建数据库时写入默认分类与账户（在数据库事务内同步执行，无竞态）。 */
    private class SeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.execSQL(
                """
                INSERT OR IGNORE INTO categories (id, name, icon, color, type, isDefault, sortOrder) VALUES
                (1, '餐饮', '🍜', 0xFFFF6B6B, 'EXPENSE', 1, 1),
                (2, '交通', '🚌', 0xFF4ECDC4, 'EXPENSE', 1, 2),
                (3, '购物', '🛍️', 0xFFFFA94D, 'EXPENSE', 1, 3),
                (4, '居住', '🏠', 0xFF9775FA, 'EXPENSE', 1, 4),
                (5, '娱乐', '🎮', 0xFFF783AC, 'EXPENSE', 1, 5),
                (6, '医疗', '💊', 0xFF20C997, 'EXPENSE', 1, 6),
                (7, '教育', '📚', 0xFF74C0FC, 'EXPENSE', 1, 7),
                (8, '其他', '📦', 0xFFADB5BD, 'EXPENSE', 1, 8),
                (9, '工资', '💰', 0xFF2ECC71, 'INCOME', 1, 1),
                (10, '奖金', '🎁', 0xFFF9CA24, 'INCOME', 1, 2),
                (11, '理财', '📈', 0xFF0984E3, 'INCOME', 1, 3),
                (12, '其他收入', '💵', 0xFF6C5CE7, 'INCOME', 1, 4)
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO accounts (id, name, icon, color, isArchived, isDefault, sortOrder) VALUES
                (13, '现金', '💵', 0xFF2ECC71, 0, 1, 1),
                (14, '微信', '💬', 0xFF09AA46, 0, 0, 2),
                (15, '支付宝', '💳', 0xFF1677FF, 0, 0, 3),
                (16, '银行卡', '🏦', 0xFFEB5757, 0, 0, 4)
                """.trimIndent()
            )
        }
    }
}
