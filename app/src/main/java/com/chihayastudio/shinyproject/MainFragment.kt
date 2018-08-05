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
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MainFragment : Fragment() {
    private var OVERLAY_PERMISSION_REQ_CODE = 1000
    private var REQUEST_MEDIA_PROJECTION = 1005

    private val DELAY_MILLI_SECOUNDS_CHECK_OVERLAY_PERMISSION: Long = 100

    private lateinit var mMediaProjectionManager: MediaProjectionManager
    private lateinit var handler: Handler

    private lateinit var checkPermissionButton: View
    private lateinit var startButton: View

    private val LANG = "jpn"
    private val TESS_DATA_DIR = "tessdata" + File.separator
    private val TESS_TRAINED_DATA = "$LANG.traineddata"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        handler = Handler()
        checkTrainedData(context!!)
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
            (context!!.applicationContext as ShotApplication).mediaProjectionManager = mMediaProjectionManager
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

    private fun checkTrainedData(context: Context) {
        val dataPath = context.filesDir.toString() + File.separator + TESS_DATA_DIR
        val dir = File(dataPath)
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles(context)
        }
        if (dir.exists()) {
            val dataFilePath = dataPath + TESS_TRAINED_DATA
            val datafile = File(dataFilePath)
            if (!datafile.exists()) {
                copyFiles(context)
            }
        }
    }

    private fun copyFiles(context: Context) {
        try {
            val filePath = context.filesDir.toString() + File.separator + TESS_DATA_DIR + TESS_TRAINED_DATA

            val inputStream = context.assets.open(TESS_DATA_DIR + TESS_TRAINED_DATA)
            val outStream = FileOutputStream(filePath)

            val buffer = ByteArray(1024)
            var read = 0
            while (inputStream.read(buffer).let { read = it; it>0 }) {
                Log.d("copy",read.toString())
                outStream.write(buffer, 0, read)
            }
            outStream.flush()
            outStream.close()
            inputStream.close()

            val file = File(filePath)
            if (!file.exists()) {
                throw FileNotFoundException()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

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