package com.chihayastudio.shinyproject

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chihayastudio.shinyproject.service.FloatService

class MainFragment : Fragment() {
    private var OVERLAY_PERMISSION_REQ_CODE = 1000
    private val DELAY_MILLI_SECOUNDS_CHECK_OVERLAY_PERMISSION: Long = 100

    private lateinit var handler: Handler

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        handler = Handler()
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.button).setOnClickListener {
            checkPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var count = 0
        when(requestCode){
            OVERLAY_PERMISSION_REQ_CODE-> {
                // 「他アプリの上に表示する」設定の取得が画面復帰直後には取得できない為、１秒間(100ms x 10回)設定の変更を監視する
                handler.post(object: Runnable {
                    override fun run() {
                        if (count > 10 || hasDrawOverlaysPermission()) {
                            startService()
                        } else {
                            count++
                            handler.postDelayed(this, DELAY_MILLI_SECOUNDS_CHECK_OVERLAY_PERMISSION)
                        }
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }

    private fun checkPermission() {
        if (hasDrawOverlaysPermission()) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context!!.packageName}"))
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        }else{
            startService()
        }
    }

    private fun hasDrawOverlaysPermission(): Boolean {
        return !Settings.canDrawOverlays(context)
    }

    private fun startService(){
        val intent = Intent(context, FloatService::class.java)
        // Serviceの開始
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.stopService(intent)
            context!!.startForegroundService(intent)
        }else{
            context!!.stopService(intent)
            context!!.startService(intent)
        }
    }
    private fun stopService(){
        val intent = Intent(context, FloatService::class.java)
        // Serviceの開始
        context!!.stopService(intent)
    }

}