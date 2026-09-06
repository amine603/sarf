package com.cash.guide.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CalculationEntity::class,
        CalculationItemEntity::class,
        CalculationGroupEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class HssabiDatabase : RoomDatabase() {

    abstract fun calculationDao(): CalculationDao
    abstract fun calculationGroupDao(): CalculationGroupDao

    companion object {
        @Volatile
        private var INSTANCE: HssabiDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `calculation_groups` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        `updatedAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE `calculations` ADD COLUMN `groupId` TEXT DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_calculations_groupId` ON `calculations` (`groupId`)")
            }
        }

        fun getInstance(context: Context): HssabiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HssabiDatabase::class.java,
                    "hssabi.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
