package com.miaomiao.jizhang.core.data

import androidx.room.withTransaction
import com.miaomiao.jizhang.core.common.DateUtils
import com.miaomiao.jizhang.core.common.MoneyFormatter
import com.miaomiao.jizhang.core.common.TxType
import com.miaomiao.jizhang.core.data.dao.AccountDao
import com.miaomiao.jizhang.core.data.dao.BudgetDao
import com.miaomiao.jizhang.core.data.dao.CategoryDao
import com.miaomiao.jizhang.core.data.dao.CatStateDao
import com.miaomiao.jizhang.core.data.dao.TransactionDao
import com.miaomiao.jizhang.core.data.entity.AccountEntity
import com.miaomiao.jizhang.core.data.entity.BudgetEntity
import com.miaomiao.jizhang.core.data.entity.CategoryEntity
import com.miaomiao.jizhang.core.data.entity.CatStateEntity
import com.miaomiao.jizhang.core.data.entity.TransactionEntity
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据备份：CSV（仅账单）/ JSON（全量，可恢复）。
 */
@Singleton
class BackupManager @Inject constructor(
    private val db: AppDatabase,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val budgetDao: BudgetDao,
    private val catStateDao: CatStateDao
) {

    suspend fun exportCsv(): String {
        val transactions = transactionDao.getAll()
        val categories = categoryDao.getAll().associateBy { it.id }
        val accounts = accountDao.getAll().associateBy { it.id }
        val sb = StringBuilder()
        sb.append("日期,类型,金额(元),分类,账户,备注\n")
        for (t in transactions) {
            val date = DateUtils.toLocalDate(t.dateTime).toString()
            val type = if (t.type == TxType.EXPENSE) "支出" else "收入"
            val amount = MoneyFormatter.formatPlain(t.amount)
            val category = categories[t.categoryId]?.name ?: "已删除分类"
            val account = accounts[t.accountId]?.name ?: "已删除账户"
            sb.append("$date,$type,$amount,$category,$account,${escapeCsv(t.note)}\n")
        }
        return sb.toString()
    }

    suspend fun exportJson(): String {
        val root = JSONObject()
        root.put("app", "miaomiao.jizhang")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        root.put("transactions", JSONArray().apply {
            transactionDao.getAll().forEach { put(transactionToJson(it)) }
        })
        root.put("categories", JSONArray().apply {
            categoryDao.getAll().forEach { put(categoryToJson(it)) }
        })
        root.put("accounts", JSONArray().apply {
            accountDao.getAll().forEach { put(accountToJson(it)) }
        })
        root.put("budgets", JSONArray().apply {
            budgetDao.getAll().forEach { put(budgetToJson(it)) }
        })
        catStateDao.get()?.let { root.put("cat", catToJson(it)) }
        return root.toString(2)
    }

    /** 导入 JSON：清空现有数据后全量重建。 */
    suspend fun importJson(json: String): Boolean {
        return try {
            val root = JSONObject(json)
            db.withTransaction {
                transactionDao.deleteAll()
                categoryDao.deleteAll()
                accountDao.deleteAll()
                budgetDao.deleteAll()
                catStateDao.deleteAll()

                root.optJSONArray("categories")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        categoryDao.insert(categoryFromJson(arr.getJSONObject(i)))
                    }
                }
                root.optJSONArray("accounts")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        accountDao.insert(accountFromJson(arr.getJSONObject(i)))
                    }
                }
                root.optJSONArray("transactions")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        transactionDao.insert(transactionFromJson(arr.getJSONObject(i)))
                    }
                }
                root.optJSONArray("budgets")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        budgetDao.upsert(budgetFromJson(arr.getJSONObject(i)))
                    }
                }
                root.optJSONObject("cat")?.let { catStateDao.insert(catFromJson(it)) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun escapeCsv(field: String): String =
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }

    // ---------- JSON 序列化 ----------

    private fun transactionToJson(t: TransactionEntity) = JSONObject().apply {
        put("id", t.id)
        put("type", t.type)
        put("amount", t.amount)
        put("categoryId", t.categoryId)
        put("accountId", t.accountId)
        put("note", t.note)
        put("dateTime", t.dateTime)
        put("createdAt", t.createdAt)
    }

    private fun transactionFromJson(o: JSONObject) = TransactionEntity(
        id = o.optLong("id"),
        type = o.optString("type", TxType.EXPENSE),
        amount = o.optLong("amount"),
        categoryId = o.optLong("categoryId"),
        accountId = o.optLong("accountId"),
        note = o.optString("note"),
        dateTime = o.optLong("dateTime", System.currentTimeMillis()),
        createdAt = o.optLong("createdAt", System.currentTimeMillis())
    )

    private fun categoryToJson(c: CategoryEntity) = JSONObject().apply {
        put("id", c.id)
        put("name", c.name)
        put("icon", c.icon)
        put("color", c.color)
        put("type", c.type)
        put("isDefault", c.isDefault)
        put("sortOrder", c.sortOrder)
    }

    private fun categoryFromJson(o: JSONObject) = CategoryEntity(
        id = o.optLong("id"),
        name = o.optString("name"),
        icon = o.optString("icon", "📦"),
        color = o.optLong("color", 0xFFADB5BD),
        type = o.optString("type", TxType.EXPENSE),
        isDefault = o.optBoolean("isDefault"),
        sortOrder = o.optInt("sortOrder")
    )

    private fun accountToJson(a: AccountEntity) = JSONObject().apply {
        put("id", a.id)
        put("name", a.name)
        put("icon", a.icon)
        put("color", a.color)
        put("isArchived", a.isArchived)
        put("isDefault", a.isDefault)
        put("sortOrder", a.sortOrder)
    }

    private fun accountFromJson(o: JSONObject) = AccountEntity(
        id = o.optLong("id"),
        name = o.optString("name"),
        icon = o.optString("icon", "💳"),
        color = o.optLong("color", 0xFF1677FF),
        isArchived = o.optBoolean("isArchived"),
        isDefault = o.optBoolean("isDefault"),
        sortOrder = o.optInt("sortOrder")
    )

    private fun budgetToJson(b: BudgetEntity) = JSONObject().apply {
        put("id", b.id)
        put("month", b.month)
        b.categoryId?.let { put("categoryId", it) }
        put("amount", b.amount)
        put("createdAt", b.createdAt)
    }

    private fun budgetFromJson(o: JSONObject) = BudgetEntity(
        id = o.optLong("id"),
        month = o.optString("month"),
        categoryId = if (o.has("categoryId")) o.optLong("categoryId") else null,
        amount = o.optLong("amount"),
        createdAt = o.optLong("createdAt", System.currentTimeMillis())
    )

    private fun catToJson(c: CatStateEntity) = JSONObject().apply {
        put("fishCount", c.fishCount)
        put("streakDays", c.streakDays)
        put("lastRecordDate", c.lastRecordDate)
        put("totalCount", c.totalCount)
        put("animationsEnabled", c.animationsEnabled)
    }

    private fun catFromJson(o: JSONObject) = CatStateEntity(
        fishCount = o.optInt("fishCount"),
        streakDays = o.optInt("streakDays"),
        lastRecordDate = o.optString("lastRecordDate"),
        totalCount = o.optInt("totalCount"),
        animationsEnabled = o.optBoolean("animationsEnabled", true)
    )
}
