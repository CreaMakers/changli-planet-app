package com.example.changli_planet_app.feature.ledger.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.changli_planet_app.feature.ledger.data.local.room.entity.LedgerItemEntity
import com.example.changli_planet_app.feature.ledger.data.local.room.entity.LedgerTopCardEntity

@Dao
interface AccountBookDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertOrUpdateTopCard(topCard: LedgerTopCardEntity)


    @Query("SELECT * FROM top_card WHERE username = :username")
    fun getTopCardByUserName(username: String): LedgerTopCardEntity?

    @Query("SELECT * FROM something_items WHERE username = :username")
    fun getSomethingItemsByUsername(username: String): List<LedgerItemEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertOrUpdateSomethingItems(items: LedgerItemEntity)

    @Query("SELECT id FROM something_items WHERE name = :name AND totalMoney = :totalMoney AND startTime = :startTime  LIMIT 1")
    fun findIdByAttributes(name: String, totalMoney: Double, startTime: String): Int?

    @Query("SELECT totalMoney FROM something_items WHERE id = :itemId")
    fun findPriceById(itemId: Int): Double?

    @Query("SELECT * FROM something_items WHERE id = :itemId")
    fun findItemById(itemId: Int): LedgerItemEntity?

    @Query("SELECT * FROM something_items")
    fun getAllSomethingItems(): List<LedgerItemEntity>

    @Query("DELETE FROM something_items WHERE id = :itemId")
    fun deleteSomethingItem(itemId: Int)
}