package com.chihayastudio.shinyproject

import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chihayastudio.shinyproject.manager.FloatManager



class MainFragment : Fragment() {
    private var OVERLAY_PERMISSION_REQ_CODE = 1000

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var mTextView = view.findViewById<View>(R.id.textView) as TextView
        view.findViewById<View>(R.id.button).setOnClickListener {

            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context!!.packageName}"))
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
            }

            FloatManager(
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
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun resetLogoViewSize(hintLocation: Int, logoView: View) {
                            logoView.clearAnimation()
                            logoView.translationX = 0f
                            logoView.scaleX = 1f
                            logoView.scaleY = 1f
                        }
                    }
            ).show()
        }
    }

    private fun dip2px(dipValue: Float): Int {
        val scale = context!!.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}