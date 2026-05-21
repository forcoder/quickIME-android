package com.quickime.core.kb

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

/**
 * Room 数据库实体
 */
@Entity(tableName = "kb_entries")
@TypeConverters(KbConverters::class)
data class KbEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val category: KbCategory,
    val source: String,
    val useCount: Int,
    val createdAt: Long
) {
    fun toEntry() = KbEntry(
        id = id,
        content = content,
        category = category,
        source = source,
        useCount = useCount,
        createdAt = createdAt
    )

    companion object {
        fun fromEntry(entry: KbEntry) = KbEntryEntity(
            id = entry.id,
            content = entry.content,
            category = entry.category,
            source = entry.source,
            useCount = entry.useCount,
            createdAt = entry.createdAt
        )
    }
}

/**
 * 类型转换器
 */
class KbConverters {
    @TypeConverter
    fun fromCategory(category: KbCategory): String = category.name

    @TypeConverter
    fun toCategory(name: String): KbCategory = KbCategory.valueOf(name)
}
