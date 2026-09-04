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
    exportSchema = false
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
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
