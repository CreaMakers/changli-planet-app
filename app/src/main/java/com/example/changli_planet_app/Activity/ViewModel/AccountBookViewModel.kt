package com.example.changli_planet_app.Activity.ViewModel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.changli_planet_app.Cache.Room.dao.AccountBookDao
import com.example.changli_planet_app.Cache.Room.database.AccountBookDatabase
import com.example.changli_planet_app.Cache.Room.entity.SomethingItemEntity
import com.example.changli_planet_app.Cache.Room.entity.TopCardEntity
import com.example.changli_planet_app.Cache.UserInfoManager
import com.example.changli_planet_app.Core.PlanetApplication
import com.example.changli_planet_app.R
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
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

    private val currentUserName: String
        get() = UserInfoManager.username

    fun refreshData() {
        viewModelScope.launch {

            val topCard = withContext(Dispatchers.IO) {
                accountBookDao.accountBookDao().getTopCardByUserName(currentUserName)
            }
            _topCard.value = topCard

            val items = withContext(Dispatchers.IO) {
                accountBookDao.accountBookDao().getSomethingItemsByUsername(currentUserName)
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

    fun checkIfNeedRefresh() {
        val kv = MMKV.defaultMMKV()
        val lastRefreshTime = kv.decodeLong("last_refresh_time", 0)
        val currentTime = System.currentTimeMillis()

        val lastRefreshDate = Calendar.getInstance().apply { timeInMillis = lastRefreshTime }
        val currentDate = Calendar.getInstance().apply { timeInMillis = currentTime }

        val lastRefreshDay = lastRefreshDate.get(Calendar.DAY_OF_YEAR)
        val currentDay = currentDate.get(Calendar.DAY_OF_YEAR)
        val lastRefreshYear = lastRefreshDate.get(Calendar.YEAR)
        val currentYear = currentDate.get(Calendar.YEAR)
        if (lastRefreshDay != currentDay || lastRefreshYear != currentYear) {
            refreshData()
            kv.encode("last_refresh_time", currentTime)
        }
    }


    fun addSomethingItem() {
        val newItem = createSomethingItem(
            name = _itemName.value,
            price = _itemPrice.value,
            buyTime = _itemStartTime.value,
            type = _itemTpye.value
        )
        accountBookDao.accountBookDao().insertOrUpdateSomethingItems(newItem)
        updateTopCardByAdd(_itemStartTime.value, _itemPrice.value)
    }

    fun fixSomethingItem(oldId: Int) {
        val newItem = createSomethingItem(
            id = oldId,
            name = _itemName.value,
            price = _itemPrice.value,
            buyTime = _itemStartTime.value,
            type = _itemTpye.value
        )
        accountBookDao.accountBookDao().insertOrUpdateSomethingItems(newItem)
        updateTopCardByFix()
    }

    fun deleteSomethingItem(itemId: Int) {
        val item = accountBookDao.accountBookDao().findItemById(itemId)!!
        accountBookDao.accountBookDao().deleteSomethingItem(itemId)
        var topCard =
            accountBookDao.accountBookDao().getTopCardByUserName(currentUserName)!!
        val newTopCardEntity = TopCardEntity(
            currentUserName,
            topCard.allNumber - 1,
            topCard.totalMoney - item.totalMoney,
            topCard.dailyAverage - item.dailyAverage
        )
        accountBookDao.accountBookDao().insertOrUpdateTopCard(newTopCardEntity)
    }

    fun updateTopCardByAdd(startTime: String, itemPrice: Double) {
        accountBookDao.accountBookDao().getTopCardByUserName(currentUserName) ?: run {
            val days = startTime.calculateDaysSince()
            TopCardEntity(
                currentUserName,
                1,
                itemPrice,
                itemPrice / days
            ).also {
                accountBookDao.accountBookDao().insertOrUpdateTopCard(it)
            }
        }
        val userItems = accountBookDao.accountBookDao().getSomethingItemsByUsername(currentUserName)
        val newTopCard = TopCardEntity(
            currentUserName,
            userItems.size,
            userItems.sumOf { it.totalMoney },
            userItems.sumOf { it.dailyAverage }
        )
        accountBookDao.accountBookDao().insertOrUpdateTopCard(newTopCard)
    }

    fun updateTopCardByFix() {
        val userItems = accountBookDao.accountBookDao().getSomethingItemsByUsername(currentUserName)
        val newTopCard = TopCardEntity(
            currentUserName,
            userItems.size,
            userItems.sumOf { it.totalMoney },
            userItems.sumOf { it.dailyAverage }
        )
        accountBookDao.accountBookDao().insertOrUpdateTopCard(newTopCard)
    }

    private fun String.calculateDaysSince(): Long {
        val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        return maxOf(1, (Date().time - sdf.parse(this).time) / (1000 * 60 * 60 * 24))
    }

    private val typeToIconMap = mapOf(
        "手机" to R.drawable.iphone,
        "电脑" to R.drawable.laptop,
        "笔记本电脑" to R.drawable.laptop,
        "平板电脑" to R.drawable.ic_tablet_pc,
        "耳机" to R.drawable.earphone,
        "自行车" to R.drawable.bicycle,
        "游戏" to R.drawable.game,
        "游戏设备" to R.drawable.game_computer,
        "电子手表" to R.drawable.smart_watch,
        "手表" to R.drawable.watch
    )

    private fun getIconResourceForType(type: String): Int {
        return typeToIconMap[type] ?: R.drawable.nfeature
    }

    private fun createSomethingItem(
        id: Int = 0,
        name: String,
        price: Double,
        buyTime: String,
        type: String
    ): SomethingItemEntity {
        val days = buyTime.calculateDaysSince()
        return SomethingItemEntity(
            id = id,
            name = name,
            totalMoney = price,
            dailyAverage = price / days,
            startTime = buyTime,
            picture = getIconResourceForType(type),
            username = currentUserName
        )
    }

}

