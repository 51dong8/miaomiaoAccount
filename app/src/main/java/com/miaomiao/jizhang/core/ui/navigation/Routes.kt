package com.miaomiao.jizhang.core.ui.navigation

/** 导航路由。 */
object Routes {
    const val MAIN = "main"
    const val ADD = "add"
    const val ADD_EDIT = "add/{transactionId}"
    const val ADD_COPY = "add/copy/{copyId}"
    const val CAT = "cat"
    const val SETTINGS = "settings"
    const val CATEGORIES = "categories"
    const val ACCOUNTS = "accounts"
    const val RECURRING = "recurring"

    fun addEdit(transactionId: Long) = "add/$transactionId"
    fun addCopy(copyId: Long) = "add/copy/$copyId"
}
