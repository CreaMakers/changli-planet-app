package com.example.changli_planet_app.Activity.ViewModel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.changli_planet_app.Cache.Room.dao.AccountBookDao
import com.example.changli_planet_app.Cache.Room.database.AccountBookDatabase
import com.example.changli_planet_app.Cache.Room.entity.SomethingItemEntity
import com.example.changli_planet_app.Cache.Room.entity.TopCardEntity
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val _itemTpye = MutableStateFlow("")
    val itemType = _itemTpye.asSharedFlow()
    val accountBookDao = AccountBookDatabase.getInstance(PlanetApplication.appContext)
    private val _topCard = MutableStateFlow<TopCardEntity?>(null)
    val topCard = _topCard.asSharedFlow()

    private val _items = MutableStateFlow<List<SomethingItemEntity>>(emptyList())
    val items = _items.asSharedFlow()

    fun refreshData() {
        viewModelScope.launch {

            val topCard = withContext(Dispatchers.IO) {
                accountBookDao.accountBookDao().getTopCard()
            }
            _topCard.value = topCard

            val items = withContext(Dispatchers.IO) {
                accountBookDao.accountBookDao().getAllSomethingItems()
            }
            _items.value = items
        }
    }


    fun updateItemName(name: String) {
        _itemName.value = name
    }

    fun updateItemPrice(price: Double) {
        _itemPrice.value = price
    }

    fun updateItemType(type: String) {
        _itemTpye.value = type
    }

    fun updateItemStartTime(startTime: String) {
        _itemStartTime.value = startTime
    }


    fun addSomethingItem() {
        val name = _itemName.value
        val price = _itemPrice.value
        val type = _itemTpye.value
        val buyTime = _itemStartTime.value
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
        val days = Math.max((Date().time - sdf.parse(buyTime).time) / 1000 / 60 / 60 / 24, 1)
        val newItem = SomethingItemEntity(0, name, price, price / days, buyTime, itemIconResId)
        accountBookDao.accountBookDao().insertOrUpdateSomethingItems(newItem)
        updateTopCardByAdd(buyTime, price)
    }


    fun deleteSomethingItem(itemId: Int) {
        val item = accountBookDao.accountBookDao().findItemById(itemId)!!
        accountBookDao.accountBookDao().deleteSomethingItem(itemId)
        var topCard = accountBookDao.accountBookDao().getTopCard()!!
        val newTopCardEntity = TopCardEntity(
            1,
            topCard.allNumber - 1,
            topCard.totalMoney - item.totalMoney,
            topCard.dailyAverage - item.dailyAverage
        )
        accountBookDao.accountBookDao().insertOrUpdateTopCard(newTopCardEntity)
    }

    fun fixSomethingItem(oldId: Int) {
        val name = _itemName.value
        val price = _itemPrice.value
        val type = _itemTpye.value
        val buyTime = _itemStartTime.value
        val itemIconResId: Int = when (type) {
            "手机" -> R.drawable.iphone
            "笔记本电脑" -> R.drawable.laptop
            "平板电脑" ->R.drawable.ic_tablet_pc
            "耳机" -> R.drawable.earphone
            "自行车" -> R.drawable.bicycle
            "游戏" -> R.drawable.game
            "游戏设备" -> R.drawable.game_computer
            "电子手表" -> R.drawable.smart_watch
            "手表" -> R.drawable.watch
            else -> R.drawable.nfeature
        }
        val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val days = (Date().time - sdf.parse(buyTime).time) / 1000 / 60 / 60 / 24
        val newItem = SomethingItemEntity(oldId, name, price, price / days, buyTime, itemIconResId)
        accountBookDao.accountBookDao().insertOrUpdateSomethingItems(newItem)
        updateTopCardByFix()
    }

    fun updateTopCardByAdd(startTime: String, itemPrice: Double) {
        var topCard = accountBookDao.accountBookDao().getTopCard() ?: {
            val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault());
            val lastTime = sdf.parse(startTime)
            val now = Date()
            val days = Math.max(1, (now.time - lastTime.time) / 1000 / 60 / 60 / 24)
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

