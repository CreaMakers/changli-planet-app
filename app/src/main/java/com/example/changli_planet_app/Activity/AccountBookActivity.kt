package com.example.changli_planet_app.Activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.changli_planet_app.Activity.ViewModel.AccountBookViewModel
import com.example.changli_planet_app.Adapter.SomethingItemAdapter
import com.example.changli_planet_app.Cache.Room.database.AccountBookDatabase
import com.example.changli_planet_app.Cache.Room.entity.SomethingItemEntity
import com.example.changli_planet_app.Cache.Room.entity.TopCardEntity
import com.example.changli_planet_app.Core.Route
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityAccountBookBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AccountBookActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAccountBookBinding.inflate(layoutInflater) }
    private val viewModel: AccountBookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupWindowInsets()
        handleEvents()
        observeViewModel()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun handleEvents() {
        binding.emptyText.setOnClickListener {
            Route.goAddSomethingAccount(this@AccountBookActivity)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val topCard = withContext(Dispatchers.IO) {
                    viewModel.loadTopCard()
                } ?: TopCardEntity(
                    1, 0, 0.0, 0.0
                )
                val somethingItems =withContext(Dispatchers.IO) {
                    viewModel.loadItems()
                }?: emptyList<SomethingItemEntity>()
                if (somethingItems.size == 0) {
                    binding.allNumber.text = "0/0"
                    binding.somethingVlume.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                } else {
                    binding.allNumber.text = "${somethingItems.size}/0"
                    binding.somethingVlume.adapter = SomethingItemAdapter(viewModel.loadItems()!!)
                    binding.somethingVlume.layoutManager =
                        LinearLayoutManager(this@AccountBookActivity)
                }
                binding.allMoneyNumber.text = topCard.totalMoney.toString()
                binding.dailyCostNumber.text = topCard.dailyAverage.toString()
            }
        }
    }
}