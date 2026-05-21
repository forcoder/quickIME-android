package com.quickime.core.kb

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 知识库 DAO
 */
@Dao
interface KbDao {

    @Query("SELECT * FROM kb_entries ORDER BY useCount DESC LIMIT :limit OFFSET :offset")
    fun getEntries(offset: Int, limit: Int): Flow<List<KbEntryEntity>>

    @Query("SELECT * FROM kb_entries WHERE category = :category ORDER BY useCount DESC")
    fun getEntriesByCategory(category: KbCategory): Flow<List<KbEntryEntity>>

    @Query("SELECT * FROM kb_entries WHERE content LIKE '%' || :keyword || '%' ORDER BY useCount DESC LIMIT :limit")
    fun searchByKeyword(keyword: String, limit: Int): Flow<List<KbEntryEntity>>

    @Query("SELECT * FROM kb_entries WHERE id = :id")
    suspend fun getById(id: Long): KbEntryEntity?

    @Query("SELECT COUNT(*) FROM kb_entries WHERE category = :category")
    suspend fun countByCategory(category: KbCategory): Int

    @Query("SELECT COUNT(*) FROM kb_entries")
    suspend fun countAll(): Int

    @Query("SELECT category, COUNT(*) as count, SUM(LENGTH(content)) as size FROM kb_entries GROUP BY category")
    suspend fun getStats(): List<CategoryStatsRaw>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: KbEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<KbEntryEntity>)

    @Update
    suspend fun update(entry: KbEntryEntity)

    @Delete
    suspend fun delete(entry: KbEntryEntity)

    @Query("UPDATE kb_entries SET useCount = useCount + 1 WHERE id = :id")
    suspend fun incrementUseCount(id: Long)

    @Query("UPDATE kb_entries SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: Long, category: KbCategory)

    @Query("DELETE FROM kb_entries")
    suspend fun deleteAll()
}

/**
 * 原生统计结果
 */
data class CategoryStatsRaw(
    val category: KbCategory,
    val count: Int,
    val size: Long
)
