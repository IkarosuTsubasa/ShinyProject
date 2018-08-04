package com.chihayastudio.shinyproject.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import com.chihayastudio.shinyproject.R
import com.chihayastudio.shinyproject.manager.FloatManager


class FloatService : Service() {
    var float: FloatManager? = null

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val context = applicationContext
        float = FloatManager(
                context,
                object : FloatManager.GetViewCallback {
                    override fun dragingLogoViewOffset(smallView: View, isDraging: Boolean, isResetPosition: Boolean, offset: Float) {
                        if (isDraging && offset > 0) {
                            smallView.setBackgroundDrawable(null)
                        } else {
                            smallView.translationX = 0f
                            smallView.scaleX = 1f
                            smallView.scaleY = 1f
                        }
                    }

                    override fun getLogoView(): View {
                        val imageView = ImageView(context)
                        imageView.layoutParams = LinearLayout.LayoutParams(dip2px(55f), dip2px(55f))
                        imageView.scaleType = ImageView.ScaleType.CENTER
                        imageView.setImageResource(R.mipmap.ic_launcher)
                        return imageView
                    }

                    override fun onDestoryed() {
                    }

                    override fun resetLogoViewSize(hintLocation: Int, logoView: View) {
                        logoView.clearAnimation()
                        logoView.translationX = 0f
                        logoView.scaleX = 1f
                        logoView.scaleY = 1f
                    }
                }
        )
        float!!.show()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun dip2px(dipValue: Float): Int {
        val scale = applicationContext!!.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        float!!.destoryFloat()
        this.stopSelf()
    }
}