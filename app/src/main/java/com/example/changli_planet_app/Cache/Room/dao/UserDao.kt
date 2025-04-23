package com.example.changli_planet_app.Cache.Room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.changli_planet_app.Cache.Room.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM user_entity WHERE userId = :userId")
    fun getUserById(userId: Int): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user_entity WHERE cacheTime < :timestamp")
    fun getOutdatedUsers(timestamp: Long): List<UserEntity>
}