package com.example.mobiletexteditor.version.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database for storing local version control snapshots and delta history.
 */
@Database(
    entities = [FileVersionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VersionDatabase : RoomDatabase() {

    abstract fun fileVersionDao(): FileVersionDao

    companion object {
        @Volatile
        private var INSTANCE: VersionDatabase? = null

        fun getInstance(context: Context): VersionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VersionDatabase::class.java,
                    "editor_version_control.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
