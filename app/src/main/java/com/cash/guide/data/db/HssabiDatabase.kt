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
    version = 5,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `calculations` ADD COLUMN `paymentStatus` TEXT NOT NULL DEFAULT 'PAID'")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_calculations_paymentStatus` ON `calculations` (`paymentStatus`)")
                db.execSQL("UPDATE `calculations` SET `paymentStatus` = 'UNPAID' WHERE `title` LIKE '%Chantier%' OR `title` LIKE '%Salaires%' OR `title` LIKE '%Tissus%'")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `calculations` ADD COLUMN `calcType` TEXT NOT NULL DEFAULT 'PERSONNEL'")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_calculations_calcType` ON `calculations` (`calcType`)")
                db.execSQL("UPDATE `calculations` SET `calcType` = 'CREDIT' WHERE `paymentStatus` = 'UNPAID' OR `title` LIKE '%Chantier%' OR `title` LIKE '%Salaires%' OR `title` LIKE '%Tissus%'")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `calculations` ADD COLUMN `dueDateEpochMs` INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE `calculations` ADD COLUMN `reminderEnabled` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `calculations` ADD COLUMN `reminderTimeEpochMs` INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_calculations_dueDateEpochMs` ON `calculations` (`dueDateEpochMs`)")
            }
        }

        fun getInstance(context: Context): HssabiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HssabiDatabase::class.java,
                    "hssabi.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
