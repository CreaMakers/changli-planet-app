package com.example.changli_planet_app.Activity

import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityContractBinding


class ContractActivity : FullScreenActivity() {
    private lateinit var binding:ActivityContractBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityContractBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        PdfViewerActivity.launchPdfFromPath(
//            context = this,
//            path ="/storage/emulated/0/Download/Android面经.pdf",
//            pdfTitle = "Title",
//            saveTo = saveTo.ASK_EVERYTIME,
//            fromAssets = false
//        )
    }
}