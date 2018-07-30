package com.chihayastudio.shinyproject

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mainFragment = MainFragment()

        val transaction = supportFragmentManager.beginTransaction()

        transaction.add(R.id.main, mainFragment)
        transaction.commit()
    }
}
