package com.cash.guide.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CalculationEntity::class,
        CalculationItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class HssabiDatabase : RoomDatabase() {

    abstract fun calculationDao(): CalculationDao

    companion object {
        @Volatile
        private var INSTANCE: HssabiDatabase? = null

        fun getInstance(context: Context): HssabiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HssabiDatabase::class.java,
                    "hssabi.db"
                )
                    // Production migration safety: Never use fallbackToDestructiveMigration().
                    // Future schema changes must provide explicit Room Migration instances
                    // (e.g., .addMigrations(MIGRATION_1_2)) to preserve user calculation history.
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
