package com.example.changli_planet_app.Cache.Room.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.changli_planet_app.Cache.Room.dao.AccountBookDao
import com.example.changli_planet_app.Cache.Room.entity.SomethingItemEntity
import com.example.changli_planet_app.Cache.Room.entity.TopCardEntity

@Database(entities = [
    SomethingItemEntity::class,
    TopCardEntity::class
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