package com.chihayastudio.shinyproject

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import com.chihayastudio.shinyproject.service.FloatService
import android.media.projection.MediaProjectionManager
import android.view.*


class MainFragment : Fragment() {
    private var OVERLAY_PERMISSION_REQ_CODE = 1000
    private var REQUEST_MEDIA_PROJECTION = 1005

    private val DELAY_MILLI_SECOUNDS_CHECK_OVERLAY_PERMISSION: Long = 100

    private lateinit var mMediaProjectionManager: MediaProjectionManager
    private lateinit var handler: Handler

    private lateinit var checkPermissionButton: View
    private lateinit var startButton: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        handler = Handler()
        mMediaProjectionManager = context!!.applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // set button
        startButton = view.findViewById(R.id.start_button)
        checkPermissionButton = view.findViewById(R.id.permission_check_button)

        //check permission button
        if (!hasDrawOverlaysPermission()) {
            checkPermissionButton.setOnClickListener {
                checkPermission()
            }
            startButton.visibility = View.GONE
        } else {
            startButton.visibility = View.VISIBLE
            checkPermissionButton.visibility = View.GONE
        }

        //start button
        startButton = view.findViewById(R.id.start_button)
        startButton.setOnClickListener {
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var count = 0
        when (requestCode) {
            OVERLAY_PERMISSION_REQ_CODE -> {
                // 「他アプリの上に表示する」設定の取得が画面復帰直後には取得できない為、１秒間(100ms x 10回)設定の変更を監視する
                handler.post(object : Runnable {
                    override fun run() {
                        if (count > 10 || hasDrawOverlaysPermission()) {
                            startButton.visibility = View.VISIBLE
                            checkPermissionButton.visibility = View.GONE
                        } else {
                            count++
                            handler.postDelayed(this, DELAY_MILLI_SECOUNDS_CHECK_OVERLAY_PERMISSION)
                        }
                    }
                })
            }
            REQUEST_MEDIA_PROJECTION -> {
                if (resultCode != RESULT_OK) {
                    Log.d("onActivityResult", "User cancelled")
                    return
                }
                (context!!.applicationContext as ShotApplication).result = resultCode
                (context!!.applicationContext as ShotApplication).intent = data
                startService()
            }
        }
    }

    override fun onDestroy() {
        stopService()
        super.onDestroy()
    }

    private fun checkPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context!!.packageName}"))
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
    }

    private fun hasDrawOverlaysPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    private fun startService() {

        val intent = Intent(context, FloatService::class.java)
        // Serviceの開始
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context!!.stopService(intent)
            context!!.startForegroundService(intent)
        } else {
            context!!.stopService(intent)
            context!!.startService(intent)
        }
    }

    private fun stopService() {
        val intent = Intent(context, FloatService::class.java)
        context!!.stopService(intent)
    }

}