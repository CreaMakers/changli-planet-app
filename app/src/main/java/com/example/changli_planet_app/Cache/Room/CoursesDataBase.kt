package com.example.changli_planet_app.Cache.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.changli_planet_app.Cache.WeeksTypeConverter

@Database(entities = [MySubject::class], version = 10, exportSchema = true)
@TypeConverters(WeeksTypeConverter::class)
abstract class CoursesDataBase() : RoomDatabase() {
    abstract fun courseDao(): CourseDao

    companion object {
        @Volatile
        private var INSTANCE: CoursesDataBase? = null
        fun getDatabase(context: Context) = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                CoursesDataBase::class.java,
                "course_database"
            )
                .build()
            INSTANCE = instance
            instance
        }
    }
}