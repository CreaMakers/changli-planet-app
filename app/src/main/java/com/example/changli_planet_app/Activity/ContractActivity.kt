package com.example.changli_planet_app.Activity

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.changli_planet_app.Adapter.PdfRenderAdapter
import com.example.changli_planet_app.Core.FullScreenActivity
import com.example.changli_planet_app.R
import com.example.changli_planet_app.databinding.ActivityContractBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class ContractActivity : FullScreenActivity() {
    private lateinit var binding:ActivityContractBinding
    private val toolbar:LinearLayout by lazy { binding.toolbar }
    private val back:ImageView by lazy { binding.back }
    private val documentName:TextView by lazy { binding.documentName }
    private val pdfContent:RecyclerView by lazy { binding.pdfContent }

    private val adapter:PdfRenderAdapter by lazy { PdfRenderAdapter() }
    private lateinit var renderer: PdfRenderer
    private var fileName="本科学生手册（2024版）.pdf"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityContractBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main){view,insets->
            val systemBars=insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.main.setPadding(view.paddingLeft,view.paddingTop,view.paddingRight,systemBars.bottom)
            toolbar.setPadding(toolbar.paddingLeft,systemBars.top,toolbar.paddingRight,toolbar.paddingBottom)
            WindowInsetsCompat.CONSUMED
        }

        back.setOnClickListener{
            finish()
        }

        documentName.text=fileName

        lifecycleScope.launch(Dispatchers.IO) {
            openPdfFileFormAsset(fileName)
            renderPage()
        }

        pdfContent.layoutManager=LinearLayoutManager(this)
        pdfContent.adapter=adapter
    }

    private fun openPdfFileFormAsset(fileName:String){
        val file=File(filesDir,fileName)
        if(!file.exists()){
            assets.open(fileName).use { input->
                FileOutputStream(file).use { output->
                    input.copyTo(output)
                }
            }
        }

        val parcelFileDescriptor=ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY)
        renderer= PdfRenderer(parcelFileDescriptor)
    }

    private suspend fun renderPage(){
        val width=resources.displayMetrics.widthPixels
        val padding=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,80f,resources.displayMetrics).toInt()
        val height=resources.displayMetrics.heightPixels-padding

        val pdfPage=renderer.pageCount
        for(i in 0..pdfPage-1){
            renderer.apply {
                val page=renderer.openPage(i)
                val bitmap= createBitmap(width,height,Bitmap.Config.ARGB_8888)
                page.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                withContext(Dispatchers.Main){
                    adapter.add(i,bitmap)
                }
                page.close()
            }
        }
        renderer.close()
    }
}