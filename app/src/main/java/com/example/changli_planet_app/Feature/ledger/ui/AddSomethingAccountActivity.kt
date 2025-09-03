package com.example.changli_planet_app.Feature.ledger.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.changli_planet_app.Base.FullScreenActivity
import com.example.changli_planet_app.Feature.ledger.viewModel.AccountBookViewModel
import com.example.changli_planet_app.Widget.Picker.ProductCategoryPicker
import com.example.changli_planet_app.Widget.View.DatePickerDialog
import com.example.changli_planet_app.databinding.ActivityAddSomethingAccountBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 添加记账本item类
 */
class AddSomethingAccountActivity : FullScreenActivity() {
    private val binding by lazy { ActivityAddSomethingAccountBinding.inflate(layoutInflater) }
    private val viewModel: AccountBookViewModel by viewModels()
    private val somethingName by lazy { binding.somethingNameEdit }
    private val somethingPrice by lazy { binding.somethingPriceEdit }
    private val addMessage by lazy { binding.addMessage }
    private val somethingType by lazy { binding.tvCategory }
    private val buyTime by lazy { binding.buyTimeEdit }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                                this@AddSomethingAccountActivity,
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
            //Route.goAccountBook(this@AddSomethingAccountActivity)
            finish()
        }
    }

    private  fun save() {
        binding.saveButton.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    viewModel.addSomethingItem()
                }
                Toast.makeText(applicationContext, "添加成功", Toast.LENGTH_SHORT).show()
                delay(800)
                //Route.goAccountBook(this@AddSomethingAccountActivity)
                finish()
            }
        }
    }

}