package com.creamaker.changli_planet_app.feature.ledger.data.local.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.creamaker.changli_planet_app.feature.ledger.data.local.room.dao.AccountBookDao
import com.creamaker.changli_planet_app.feature.ledger.data.local.room.entity.LedgerItemEntity
import com.creamaker.changli_planet_app.feature.ledger.data.local.room.entity.LedgerTopCardEntity

@Database(entities = [
    LedgerItemEntity::class,
    LedgerTopCardEntity::class
], version = 2, exportSchema = false)
abstract class AccountBookDatabase : RoomDatabase() {
    abstract fun accountBookDao(): AccountBookDao

    companion object {
        private var INSTANCE: AccountBookDatabase? = null
        fun getInstance(context: Context): AccountBookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AccountBookDatabase::class.java,
                    "account_book_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}