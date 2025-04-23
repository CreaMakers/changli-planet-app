package com.example.changli_planet_app.Activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.changli_planet_app.Activity.ViewModel.AccountBookViewModel
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityAddSomethingAccountBinding
import kotlinx.coroutines.launch
import kotlin.getValue

class AddSomethingAccountActivity : AppCompatActivity() {
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
                p0?.let { viewModel.updateItemPrice(p0.toString().toDouble()) }
            }

        })
        buyTime.setOnClickListener {

        }
    }
//
//    private fun showDatePicker() {
//        val dialog = DatePickerDialog(this)
//
//        dialog.setDate(2023, 3, 9)
//        dialog.setOnDateSelectedListener { year, month, day ->
//            val date = String.format("%d-%02d-%02d", year, month, day)
//            birthdayTextView.text = date
//        }
//
//
//        dialog.show()
//    }
//
    private fun back() {
        binding.ivExpand.setOnClickListener {
            Route.goAccountBook(this@AddSomethingAccountActivity)
            finish()
        }
    }

    private fun save() {
        binding.saveButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.addSomethingItem(
                    somethingName.text.toString(),
                    somethingPrice.text.toString().toDouble(),
                    somethingType.text.toString(),
                    buyTime.text.toString()
                )
            }
            Route.goAccountBook(this@AddSomethingAccountActivity)
            finish()
        }
    }


}