package com.miaomiao.jizhang.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miaomiao.jizhang.core.data.entity.CatStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatStateDao {

    @Query("SELECT * FROM cat_state WHERE id = 1")
    fun observe(): Flow<CatStateEntity?>

    @Query("SELECT * FROM cat_state WHERE id = 1")
    suspend fun get(): CatStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: CatStateEntity)

    @Update
    suspend fun update(state: CatStateEntity)

    @Query("DELETE FROM cat_state")
    suspend fun deleteAll()
}
