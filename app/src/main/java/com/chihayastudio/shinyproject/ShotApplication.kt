package com.chihayastudio.shinyproject

import android.app.Application
import android.content.Intent
import android.media.projection.MediaProjectionManager

class ShotApplication : Application() {
    var result: Int = 0
    var intent: Intent? = null
    var mediaProjectionManager: MediaProjectionManager? = null
}
