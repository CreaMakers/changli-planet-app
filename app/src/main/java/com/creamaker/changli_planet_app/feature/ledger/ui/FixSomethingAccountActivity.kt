package com.creamaker.changli_planet_app.feature.ledger.ui

import ProductCategoryPicker
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.creamaker.changli_planet_app.R
import com.creamaker.changli_planet_app.base.FullScreenActivity
import com.creamaker.changli_planet_app.core.Route
import com.creamaker.changli_planet_app.databinding.ActivityFixSomethingAcccountBinding
import com.creamaker.changli_planet_app.feature.ledger.viewModel.AccountBookViewModel
import com.creamaker.changli_planet_app.widget.view.DatePickerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 记账本修改item
 */
class FixSomethingAccountActivity : FullScreenActivity<ActivityFixSomethingAcccountBinding>() {
    private val viewModel: AccountBookViewModel by viewModels()
    private val somethingName by lazy { binding.somethingNameEdit}
    private val somethingPrice by lazy { binding.somethingPriceEdit }
    private val addMessage by lazy { binding.addMessage }
    private val somethingType by lazy { binding.tvCategory }
    private val buyTime by lazy { binding.buyTimeEdit }
    private var itemId: Int = -1

    override fun createViewBinding(): ActivityFixSomethingAcccountBinding = ActivityFixSomethingAcccountBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemId = intent.getIntExtra("ITEM_ID", -1)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.addTop) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                insets.top,
                view.paddingRight,
                view.paddingBottom
            )
            WindowInsetsCompat.CONSUMED
        }

        back()
        save()
        initMessage()
        delete()
        loadItemData()
    }


    private fun loadItemData() {
        lifecycleScope.launch {
            val item = withContext(Dispatchers.IO) {
                viewModel.accountBookDao.accountBookDao().findItemById(itemId)
            }

            item?.let {
                viewModel.updateItemName(it.name)
                viewModel.updateItemPrice(it.totalMoney)
                viewModel.updateItemStartTime(it.startTime)

                somethingName.setText(it.name)
                somethingPrice.setText(it.totalMoney.toString())
                buyTime.text = it.startTime

                val type = when (it.picture) {
                    R.drawable.ic_iphone -> "手机"
                    R.drawable.ic_tablet_pc->"平板电脑"
                    R.drawable.laptop -> "笔记本电脑"
                    R.drawable.ic_earphone -> "耳机"
                    R.drawable.ic_bicycle -> "自行车"
                    R.drawable.ic_game -> "游戏"
                    R.drawable.ic_game_computer -> "游戏设备"
                    R.drawable.smart_watch -> "电子手表"
                    R.drawable.watch -> "手表"
                    else -> "其他"
                }
                somethingType.text = type
                viewModel.updateItemType(type)
            }
        }
    }

    private fun initMessage() {
        somethingName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
            }

            override fun onTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {

            }

            override fun afterTextChanged(p0: Editable?) {
                p0?.let { viewModel.updateItemName(p0.toString()) }
            }
        })
        somethingPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
            }

            override fun onTextChanged(
                p0: CharSequence?,
                p1: Int,
                p2: Int,
                p3: Int
            ) {
            }

            override fun afterTextChanged(p0: Editable?) {

                p0?.let {
                    if (p0.isNotEmpty()) {
                        try {
                            viewModel.updateItemPrice(p0.toString().toDouble())
                        } catch (e: NumberFormatException) {
                            // 可以显示错误提示
                            Toast.makeText(
                                this@FixSomethingAccountActivity,
                                "请输入有效价格",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            }

        })
        somethingType.setOnClickListener {
            showTypePicker()
        }
        binding.ivExpand.setOnClickListener {
            showTypePicker()
        }
        buyTime.setOnClickListener {
            showDatePicker()
        }

    }

    private fun showTypePicker() {
        var productCategoryPicker = ProductCategoryPicker(this, "", "")
        productCategoryPicker.setOnCategorySelectedListener { categories, subcategories ->
            val tpye = String.format("%s--%s", categories, subcategories)
            somethingType.text = tpye
            lifecycleScope.launch {
                viewModel.updateItemType(subcategories)
            }
        }
        productCategoryPicker.show()
    }


    private fun showDatePicker() {
        val dialog = DatePickerDialog(this)
        dialog.setDate(2023, 3, 9)
        dialog.setOnDateSelectedListener { year, month, day ->
            val date = String.format("%d-%02d-%02d", year, month, day)
            buyTime.text = date
            lifecycleScope.launch {
                viewModel.updateItemStartTime(date)
            }

        }
        dialog.show()
    }

    private fun back() {
        binding.backBtn.setOnClickListener {
            //Route.goAccountBook(this@FixSomethingAccountActivity)
            finish()
        }
    }

    private  fun save() {
        binding.saveButton.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    viewModel.fixSomethingItem(itemId)
                }
                Toast.makeText(applicationContext, "修改成功", Toast.LENGTH_SHORT).show()
                delay(300)
               // Route.goAccountBook(this@FixSomethingAccountActivity)
                finish()
            }
        }
    }

    private fun delete(){
        binding.deleteButton.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    viewModel.deleteSomethingItem(itemId)
                }
                Toast.makeText(applicationContext, "删除成功", Toast.LENGTH_SHORT).show()
                delay(300)
                Route.goAccountBook(this@FixSomethingAccountActivity)
                finish()
            }
        }
    }


}