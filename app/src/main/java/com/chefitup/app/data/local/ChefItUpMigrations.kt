package com.chefitup.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 2 enables schema export; no structural changes from v1.
 * Future schema edits should add a new Migration here and bump @Database version.
 */
object ChefItUpMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No-op: version bump for exportSchema + documented migration path.
        }
    }
}
