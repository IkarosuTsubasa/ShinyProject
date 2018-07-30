package com.chihayastudio.shinyproject

import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chihayastudio.shinyproject.service.FloatService

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

            val intent = Intent(context, FloatService::class.java)
            // Serviceの開始
            // API26以上
            context!!.startService(intent)

        }
    }
}