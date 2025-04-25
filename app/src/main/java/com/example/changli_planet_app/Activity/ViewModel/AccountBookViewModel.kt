package com.example.changli_planet_app.Activity.ViewModel

import androidx.lifecycle.ViewModel
import com.example.changli_planet_app.Cache.Room.dao.AccountBookDao
import com.example.changli_planet_app.Cache.Room.database.AccountBookDatabase
import com.example.changli_planet_app.Cache.Room.entity.SomethingItemEntity
import com.example.changli_planet_app.Cache.Room.entity.TopCardEntity
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AccountBookViewModel : ViewModel() {
    val _totalMoney = MutableStateFlow("")
    val totalAssets = _totalMoney.asSharedFlow()
    val _dailyAverage = MutableStateFlow("")
    val dailyAverage = _dailyAverage.asSharedFlow()
    val _topStartTime = MutableStateFlow("")
    val topStartTime = _topStartTime.asSharedFlow()
    val _itemStartTime = MutableStateFlow("")
    val itemStartTime = _itemStartTime.asSharedFlow()
    val _itemName = MutableStateFlow("")
    val itemName = _itemName.asSharedFlow()
    val _itemPrice = MutableStateFlow(0.0)
    val itemPrice = _itemPrice.asSharedFlow()
    val accountBookDao = AccountBookDatabase.getInstance(PlanetApplication.appContext)

    fun updateItemName(name: String) {
        _itemName.value = name
    }

    fun updateItemPrice(price: Double) {
        _itemPrice.value = price
    }


    fun updateItemStartTime(startTime: String) {
        _itemStartTime.value = startTime
    }

    fun loadTopCard(): TopCardEntity? {
        return accountBookDao.accountBookDao().getTopCard()
    }

    fun loadItems(): List<SomethingItemEntity>? {
        return accountBookDao.accountBookDao().getAllSomethingItems()
    }


    fun addSomethingItem(name: String, price: Double, type: String, buyTime: String) {

        val itemIconResId: Int = when (type) {
            "手机" -> R.drawable.iphone
            "电脑" -> R.drawable.laptop
            "耳机" -> R.drawable.earphone
            "自行车" -> R.drawable.bicycle
            "游戏" -> R.drawable.game
            "游戏设备" -> R.drawable.game_computer
            "电子手表" -> R.drawable.smart_watch
            "手表" -> R.drawable.watch
            else -> R.drawable.nfeature
        }
        val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val days = (sdf.parse(buyTime).time - Date().time) / 1000 / 60 / 60 / 24
        val newItem = SomethingItemEntity(0, name, price, price / days, buyTime, itemIconResId)
        accountBookDao.accountBookDao().insertOrUpdateSomethingItems(newItem)
        updateTopCardByAdd(buyTime, price)
    }

    fun findItemId(name: String, totalMoney: Double, startTime: String): Int? {
        return accountBookDao.accountBookDao().findIdByAttributes(name, totalMoney, startTime)
    }

    fun fixSomethingItem(name: String, price: Double, type: String, buyTime: String, oldId: Int) {
        val itemIconResId: Int = when (type) {
            "手机" -> R.drawable.iphone
            "电脑" -> R.drawable.laptop
            "耳机" -> R.drawable.earphone
            "自行车" -> R.drawable.bicycle
            "游戏" -> R.drawable.game
            "游戏设备" -> R.drawable.game_computer
            "电子手表" -> R.drawable.smart_watch
            "手表" -> R.drawable.watch
            else -> R.drawable.nfeature
        }
        val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val days = (sdf.parse(buyTime).time - Date().time) / 1000 / 60 / 60 / 24
        val newItem = SomethingItemEntity(oldId, name, price, price / days, buyTime, itemIconResId)
        accountBookDao.accountBookDao().insertOrUpdateSomethingItems(newItem)
        updateTopCardByFix()
    }

    fun updateTopCardByAdd(startTime: String, itemPrice: Double) {
        var topCard = accountBookDao.accountBookDao().getTopCard() ?: {
            val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault());
            val lastTime = sdf.parse(startTime)
            val now = Date()
            val days = (lastTime.time - now.time) / 1000 / 60 / 60 / 24;
            val newTopCard = TopCardEntity(
                1,
                1,
                itemPrice,
                itemPrice / days
            )
            accountBookDao.accountBookDao().insertOrUpdateTopCard(newTopCard)
        }
        var newTotalMoney: Double = 0.0
        var newDailyAverage: Double = 0.0
        var itemSize = 0
        for (entity in accountBookDao.accountBookDao().getAllSomethingItems()) {
            newDailyAverage = entity.dailyAverage + newDailyAverage
            newTotalMoney = entity.totalMoney + newTotalMoney
            itemSize++
        }
        val newTopCard = TopCardEntity(
            1,
            itemSize,
            newTotalMoney,
            newDailyAverage
        )
        accountBookDao.accountBookDao().insertOrUpdateTopCard(newTopCard)
    }

    fun updateTopCardByFix() {
        var topCard = accountBookDao.accountBookDao().getTopCard()!!
        var newTotalMoney: Double = 0.0
        var newDailyAverage: Double = 0.0
        var itemSize = 0
        for (entity in accountBookDao.accountBookDao().getAllSomethingItems()) {
            newDailyAverage = entity.dailyAverage + newDailyAverage
            newTotalMoney = entity.totalMoney + newTotalMoney
            itemSize++
        }
        val newTopCard = TopCardEntity(
            1,
            itemSize,
            newTotalMoney,
            newDailyAverage
        )
        accountBookDao.accountBookDao().insertOrUpdateTopCard(newTopCard)
    }
}

