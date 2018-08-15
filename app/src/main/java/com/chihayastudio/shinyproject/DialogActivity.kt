package com.chihayastudio.shinyproject

import android.support.v4.app.FragmentActivity
import android.os.Bundle

class DialogActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragment = DialogFragment()
        fragment.show(fragmentManager, "alert_dialog")
    }
}