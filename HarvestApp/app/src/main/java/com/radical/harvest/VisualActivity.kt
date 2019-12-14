package com.radical.harvest

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import kotlinx.android.synthetic.main.visual.*
import java.lang.*
import android.view.GestureDetector
import android.support.v4.view.GestureDetectorCompat

class VisualActivity : AppCompatActivity(), GestureDetector.OnGestureListener
{
    var gFrame = 0;
    var gTapMode = false;
    var gFinished = false;
    var gDetector: GestureDetectorCompat? = null

    fun buildImageThread(id: Int, pause: Long, shouldToast: Boolean): Thread
    {
        return Thread(Runnable {
            var jThis = this as Object
            synchronized(this)
            {
                wait(pause)
                this@VisualActivity.runOnUiThread(java.lang.Runnable {
                    if(shouldToast && gTapMode)
                    {
                    }
                    else
                    {
                        visual.setImageResource(id);
                    }

                    if(id == R.mipmap.map4)
                    {
                        gDetector = GestureDetectorCompat(this, this)

                        if(shouldToast && !gFinished)
                        {
                            var toast = Toast.makeText(this, "Tap to Proceed", Toast.LENGTH_SHORT)
                            toast.setGravity(android.view.Gravity.CENTER, 0, 0)
                            toast.show()
                        }
                    }
                })
            }
        });
    }

    fun runAnimation(pause: Long, wait: Long)
    {
        buildImageThread(R.mipmap.map1, pause + wait, false).start()
        buildImageThread(R.mipmap.map2, pause + wait*2, false).start()
        buildImageThread(R.mipmap.map3, pause + wait*3, false).start()
        buildImageThread(R.mipmap.map4, pause + wait*4, false).start()
        buildImageThread(R.mipmap.map4, pause + wait*16, true).start()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.visual)
        supportActionBar?.hide()

        runAnimation(500L, 500L);
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean
    {
        gDetector?.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onShowPress(event: MotionEvent?)
    {
    }

    override fun onSingleTapUp(event: MotionEvent?): Boolean
    {
        if(gTapMode)
        {
            when(gFrame)
            {
                0 -> buildImageThread(R.mipmap.map0, 1, false).start()
                1 -> buildImageThread(R.mipmap.map1, 1, false).start()
                2 -> buildImageThread(R.mipmap.map2, 1, false).start()
                3 -> buildImageThread(R.mipmap.map3, 1, false).start()
                4 -> buildImageThread(R.mipmap.map4, 1, false).start()
            }

            if(gFrame == 4)
            {
                gFrame = 0
            }

            gFrame++
        }
        else
        {
            gFinished = true;
            startActivity(Intent(this, MainActivity::class.java))
        }

        return false
    }

    override fun onDown(event: MotionEvent?): Boolean
    {
        return false
    }

    override fun onFling(event0: MotionEvent?, event1: MotionEvent?, velocityX: Float, velocityY: Float): Boolean
    {
        return false;
    }

    override fun onScroll(event0: MotionEvent?, event1: MotionEvent?, distanceX: Float, distanceY: Float): Boolean
    {
        return false;
    }

    override fun onLongPress(event: MotionEvent?)
    {
        if(!gTapMode)
        {
            gTapMode = true
            gFinished = true;
            gFrame = 1
            buildImageThread(R.mipmap.map0, 1, false).start()
        }
        else
        {
            gTapMode = false;
            gFinished = false;
            buildImageThread(R.mipmap.map4, 1, true).start()
        }
    }
}
