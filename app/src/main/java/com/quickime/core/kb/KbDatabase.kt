package com.quickime.core.kb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * 知识库数据库
 */
@Database(
    entities = [KbEntryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(KbConverters::class)
abstract class KbDatabase : RoomDatabase() {
    abstract fun kbDao(): KbDao

    companion object {
        const val DATABASE_NAME = "quickime_kb.db"
    }
}
