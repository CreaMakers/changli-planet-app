package com.example.changli_planet_app.widget.View

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class PdfView(context: Context,
    attrs:AttributeSet): SurfaceView(context,attrs),SurfaceHolder.Callback {
    private val mSurfaceHolder:SurfaceHolder by lazy { holder }
    private var isSurfaceCreated=false
    private var onAlreadyListener:OnAlreadyListener?=null

    init {
        mSurfaceHolder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isSurfaceCreated=true
        onAlreadyListener?.let {
            val bitmap=it.onAlready(holder)
            drawBitmap(bitmap)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isSurfaceCreated=false
        onAlreadyListener?.onFinish(holder)
    }

    private fun drawBitmap(bitmap: Bitmap){
        val canvas=mSurfaceHolder.lockCanvas()
        if(canvas!=null){
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(bitmap,0f,0f,null)
            mSurfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    public fun getIsSurfaceCreated()=isSurfaceCreated

    public fun setOnAlreadyListener(listener: OnAlreadyListener){
        onAlreadyListener=listener
    }
}


interface OnAlreadyListener{
    fun onAlready(holder: SurfaceHolder):Bitmap
    fun onFinish(holder: SurfaceHolder)
}