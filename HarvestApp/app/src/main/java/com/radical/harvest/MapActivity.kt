package com.radical.harvest

import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.map.*
import android.os.Bundle
import android.text.Html
import android.view.*

class MapActivity : AppCompatActivity()
{
    fun calculateStorage(impervious: Double, area: Double, rainfall: Double): Int
    {
        var gallons = 500; //default
        if(impervious < 1.0)
        {
            var needed = 40 - rainfall; //Want 52 inches/year equivalent, modify for dormant grass
            val pervious = (1-impervious)*area; //sqft
            val perviousIn = pervious*144 // sqin
            //.004329 gallons per cubic in
            val recommendedGallons = Math.min((impervious*area*144*.004329)*rainfall, (perviousIn*.004329)*needed); //Can store vs. actual need

            gallons = Math.ceil(recommendedGallons).toInt()

        }

        return gallons;
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map)

        val latitude = intent.getDoubleExtra("latitude",0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        var desc = intent.getStringExtra("description")
        val rainfall = intent.getDoubleExtra("rainfall", 0.0)
        val isVoid = intent.getBooleanExtra("void", true)
        var area = intent.getDoubleExtra("area", 0.0)
        var impervious = intent.getDoubleExtra("impervious", 0.0)

        place.settings.javaScriptEnabled = true
        place.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                return true
            }
        })

        if(isVoid)
        {
            var bytes: ByteArray? = null
            application.assets.open("impervious.kan").apply{
                bytes = this.readBytes()
            }.close()

            val multiplier = 10000.0;
            val kann = KannNative();
            impervious = kann.run((latitude*multiplier).toInt(), Math.abs((longitude*multiplier).toInt()), bytes)/multiplier

            application.assets.open("area.kan").apply{
                bytes = this.readBytes()
            }.close()
            area = kann.run((latitude*multiplier).toInt(), Math.abs((longitude*multiplier).toInt()), bytes)/1.0
        }
        else
        {
            asterisk.visibility = View.INVISIBLE;
        }

        desc += "Impervious Cover: " + java.lang.String.format("%.0f", 100*impervious) + "%<br>"
        desc += "Lot Size: " + java.lang.String.format("%.2f", area) + " sqft<br>"
        desc += "Annual Rainfall: " + java.lang.String.format("%.1f", (rainfall*2)) + " in<br>" //We only use 6 months of data

        val url = java.lang.String.format("https://www.bing.com/maps/embed?h=350&w=500&cp=%5f~%5f&lvl=17&typ=d&sty=r&src=SHELL&FORM=MBEDV8", latitude, longitude)
        place.loadUrl(url)

        desc += "<br><br>Recommended Reserves<br><b>" + calculateStorage(impervious, area, rainfall*2) + "</b> gallons"
        description.text = Html.fromHtml(desc)

        Thread(Runnable {
            var jThis = this as Object
            synchronized(this)
            {
                wait(2000)
            }
            this@MapActivity.runOnUiThread(java.lang.Runnable {
                var resource = R.mipmap.category0;
                if(rainfall >= 13.0)
                {
                    resource = R.mipmap.category1;
                }
                if(rainfall >= 16.0f)
                {
                    resource = R.mipmap.category2;
                }
                if(rainfall >= 19.0f)
                {
                    resource = R.mipmap.category3;
                }
                if(rainfall >= 22.0f)
                {
                    resource = R.mipmap.category4;
                }
                overlay.setImageResource(resource);
            })
        }).start()
    }
}
