package com.radical.harvest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.*
import java.lang.*

class SplashActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        supportActionBar?.hide()

        Thread(Runnable {
            var jThis = this as Object
            synchronized(this)
            {
                wait(2000)
            }
            this@SplashActivity.runOnUiThread(java.lang.Runnable {
                val intent = Intent(this, VisualActivity::class.java)
                startActivity(intent)
            })
        }).start()
    }
}
