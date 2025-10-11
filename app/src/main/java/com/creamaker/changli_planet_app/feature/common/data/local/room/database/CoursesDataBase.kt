package com.creamaker.changli_planet_app.feature.common.data.local.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.creamaker.changli_planet_app.feature.common.data.local.entity.TimeTableMySubject
import com.creamaker.changli_planet_app.feature.common.data.local.room.converter.WeeksTypeConverter
import com.creamaker.changli_planet_app.feature.common.data.local.room.dao.CourseDao

@Database(entities = [TimeTableMySubject::class], version = 10, exportSchema = true)
@TypeConverters(WeeksTypeConverter::class)
abstract class CoursesDataBase : RoomDatabase() {
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