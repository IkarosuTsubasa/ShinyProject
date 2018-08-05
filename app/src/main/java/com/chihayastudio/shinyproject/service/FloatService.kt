package com.chihayastudio.shinyproject.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.ImageButton
import com.chihayastudio.shinyproject.R
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.LayoutInflater
import com.chihayastudio.shinyproject.ShotApplication
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import com.googlecode.tesseract.android.TessBaseAPI


class FloatService : Service() {
    // createVirtualEnvironment
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var strDate: String
    private lateinit var pathImage: String
    private lateinit var nameImage: String

    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null

    private var mResultCode = 0
    private lateinit var mResultData: Intent
    private lateinit var mMediaProjectionManager1: MediaProjectionManager

    private lateinit var mWindowManager1: WindowManager
    private var windowWidth = 0
    private var windowHeight = 0
    private lateinit var mImageReader: ImageReader
    private lateinit var metrics: DisplayMetrics
    private var mScreenDensity = 0

    private lateinit var mFloatLayout: View
    private lateinit var wmParams: WindowManager.LayoutParams
    private lateinit var mWindowManager: WindowManager
    private lateinit var layoutInflater: LayoutInflater
    private lateinit var mFloatView: ImageButton

    private val TAG = "MainActivity"
    private val LANG = "jpn"

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createFloatView()

        createVirtualEnvironment()
        return super.onStartCommand(intent, flags, startId)
    }


    private fun createVirtualEnvironment() {
        dateFormat = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss")
        strDate = dateFormat.format(java.util.Date())
        pathImage = Environment.getExternalStorageDirectory().path + "/Pictures/"
        nameImage = "shiny.png"
        mMediaProjectionManager1 = application.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mWindowManager1 = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowWidth = mWindowManager1.defaultDisplay.width
        windowHeight = mWindowManager1.defaultDisplay.height
        metrics = DisplayMetrics()
        mWindowManager1.defaultDisplay.getMetrics(metrics)
        mScreenDensity = metrics.densityDpi
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2) //ImageFormat.RGB_565

        Log.d("VirtualEnvironment", "prepared the virtual environment")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatView() {
        wmParams = WindowManager.LayoutParams()
        mWindowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wmParams.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                it.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            it.format = PixelFormat.RGBA_8888
            it.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            it.gravity = Gravity.LEFT or Gravity.TOP
            it.x = 0
            it.y = 0
            it.width = WindowManager.LayoutParams.WRAP_CONTENT
            it.height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        layoutInflater = LayoutInflater.from(this)

        mFloatLayout = layoutInflater.inflate(R.layout.float_layout, null)
        mWindowManager.addView(mFloatLayout, wmParams)
        mFloatView = mFloatLayout.findViewById(R.id.float_id) as ImageButton

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))

        mFloatView.setOnTouchListener { _, event ->
            wmParams.x = event.rawX.toInt() - mFloatView.measuredWidth / 2
            wmParams.y = event.rawY.toInt() - mFloatView.measuredHeight / 2 - 25
            mWindowManager.updateViewLayout(mFloatLayout, wmParams)
            false
        }

        mFloatView.setOnClickListener(View.OnClickListener {
            // hide the button
            mFloatView.visibility = View.INVISIBLE

            val handler1 = Handler()
            handler1.postDelayed({
                //start virtual
                startVirtual()
                val handler2 = Handler()
                handler2.postDelayed({
                    //capture the screen
                    startCapture()
                    val handler3 = Handler()
                    handler3.postDelayed({
                        mFloatView.visibility = View.VISIBLE
                        stopVirtual()
                    }, 500)
                }, 500)
            }, 500)
        })
    }

    private fun startVirtual() {
        if (mMediaProjection != null) {
            Log.i(TAG, "want to display virtual")
            virtualDisplay()
        } else {
            Log.i(TAG, "start screen capture intent")
            Log.i(TAG, "want to build mediaprojection and display virtual")
            setUpMediaProjection()
            virtualDisplay()
        }
    }

    private fun virtualDisplay() {
        mVirtualDisplay = mMediaProjection!!.createVirtualDisplay("screen-mirror",
                windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.surface, null, null)
        Log.i(TAG, "virtual displayed")
    }

    private fun setUpMediaProjection() {
        mResultData = (application as ShotApplication).intent!!
        mResultCode = (application as ShotApplication).result
        mMediaProjectionManager1 = (application as ShotApplication).mediaProjectionManager!!
        mMediaProjection = mMediaProjectionManager1.getMediaProjection(mResultCode, mResultData)
        Log.i(TAG, "mMediaProjection defined")
    }

    private fun startCapture() {
        strDate = dateFormat.format(java.util.Date())
        nameImage = "$pathImage$strDate.png"

        val image = mImageReader.acquireLatestImage()
        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        var bitmap: Bitmap? = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap!!.copyPixelsFromBuffer(buffer)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
        image.close()
        Log.i(TAG, "image data captured")

        if (bitmap != null) {

            var imageWidth = bitmap.width
            var imageHeight = bitmap.height
            val nWidth = (imageWidth * 0.23f).toInt()
            val nHeight = (imageHeight * 0.20f).toInt()
            val startX = (imageWidth - nWidth) / 2
            val startY = (imageHeight* 0.14).toInt()

            bitmap = Bitmap.createBitmap(bitmap, startX, startY, nWidth, nHeight, null, true)

            var DATA_PATH = applicationContext.filesDir.toString()
            Log.d("DATA_PATH", DATA_PATH)
            val baseApi = TessBaseAPI()
            baseApi.init(DATA_PATH, LANG)
            baseApi.setImage(bitmap)
            val recognizedText = baseApi.utF8Text
            baseApi.end()

            Log.d("recognizedText", recognizedText)

            try {
                val fileImage = File(nameImage)
                if (!fileImage.exists()) {
                    fileImage.createNewFile()
                    Log.i(TAG, "image file created")
                }
                val out = FileOutputStream(fileImage)
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                    val media = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val contentUri = Uri.fromFile(fileImage)
                    media.data = contentUri
                    this.sendBroadcast(media)
                    Log.i(TAG, "screen image saved")
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun stopVirtual() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.release()
        mVirtualDisplay = null
        Log.i(TAG, "virtual display stopped")
    }

    private fun tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
        Log.i(TAG, "mMediaProjection undefined")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout)
        }
        tearDownMediaProjection()
        Log.i(TAG, "application destroy")
    }
}