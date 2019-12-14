package com.radical.harvest

import android.app.Application
import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.records.*
import android.os.Bundle
import android.content.*
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import org.json.*

import android.util.*
import java.util.*

var mJSON: JSONArray? = null
var mJSONSubset: JSONArray = JSONArray()

class MainActivity : AppCompatActivity(), TextWatcher
{
    private class SearchTask: TimerTask
    {
        private var mText: String? = null;
        private var mContext: AppCompatActivity? = null
        private var mApp: Application = Application();
        public constructor(context: AppCompatActivity, application: Application, text: String)
        {
            mText = text.toUpperCase()
            mContext = context
            mApp = application
        }

        override public fun run()
        {
            var progress: ProgressDialog? = null
            mContext?.runOnUiThread(java.lang.Runnable {
                progress = ProgressDialog(mContext)
                progress?.setCancelable(false)
                progress?.setMessage("Searching...")
                progress?.show()
            })

            var j = 0
            var result = JSONArray()
            var listItems = arrayOfNulls<String>(12000);

/*
            var k = 12;
            while(k-- > 0)
            {
                var jsonString = String()
                mApp.assets.open("Data" + k).apply{
                    jsonString = this.readBytes().toString(Charsets.UTF_8)
                }.close()

                val JSON = JSONArray(jsonString)
                var size = JSON?.length() as Int
                for(i in 0 until size)
                {
                    val JSON = JSONArray(jsonString)
                    val obj = JSON?.get(i) as JSONObject
                    if ((obj.get("Street") as String).contains(mText as CharSequence) || mText == "")
                    {
                        result.put(obj)
                        listItems[j] = obj.get("Street") as String
                        j++
                    }
                }
            }
*/
/*
            var l = 23;
            while(l-- > 0)
            {
                var jsonString = String();
                mApp.assets.open("Void" + l).apply{
                    jsonString = this.readBytes().toString(Charsets.UTF_8)
                }.close()

                val JSON = JSONArray(jsonString)
                var size = JSON?.length() as Int
                for(i in 0 until size)
                {
                    val JSON = JSONArray(jsonString)
                    val obj = JSON?.get(i) as JSONObject
                    if ((obj.get("Street") as String).contains(mText as CharSequence) || mText == "")
                    {
                        result.put(obj)
                        listItems[j] = obj.get("Street") as String
                        j++
                    }
                }
            }
*/

            var jsonString = String()
            mApp.assets.open("DemoData").apply{
                jsonString = this.readBytes().toString(Charsets.UTF_8)
            }.close()

            var JSON = JSONArray(jsonString)
            var size = JSON?.length() as Int
            for(i in 0 until size)
            {
                val JSON = JSONArray(jsonString)
                val obj = JSON?.get(i) as JSONObject
                obj.put("Void", false);
                if ((obj.get("Street") as String).contains(mText as CharSequence) || mText == "")
                {
                    result.put(obj)
                    listItems[j] = obj.get("Street") as String
                    j++
                }
            }

            mApp.assets.open("DemoVoid").apply{
                jsonString = this.readBytes().toString(Charsets.UTF_8)
            }.close()

            JSON = JSONArray(jsonString)
            size = JSON?.length() as Int
            for(i in 0 until size)
            {
                val JSON = JSONArray(jsonString)
                val obj = JSON?.get(i) as JSONObject
                obj.put("Void", true);
                if ((obj.get("Street") as String).contains(mText as CharSequence) || mText == "")
                {
                    result.put(obj)
                    listItems[j] = obj.get("Street") as String
                    j++
                }
            }

            listItems = listItems.copyOfRange(0, j)
            mJSONSubset = result

            mContext?.runOnUiThread(java.lang.Runnable {
                (mContext?.findViewById(R.id.recordlist) as ListView).adapter = ArrayAdapter(mContext, android.R.layout.simple_list_item_1, listItems)
                (mContext?.findViewById(R.id.recordlist) as ListView).refreshDrawableState()
                progress?.dismiss()
            })
        }

    }

    private var mEditTimer: Timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.records)

        var jsonString = String();
        application.assets.open("Data1").apply{
            jsonString = this.readBytes().toString(Charsets.UTF_8)
        }.close()

        val JSON = JSONArray(jsonString)
        mJSON = JSON
        mJSONSubset = JSON

        val size = JSON.length()
        val listItems = arrayOfNulls<String>(size);
        for(i in 0 until size)
        {
            val obj = JSON[i] as JSONObject
            obj.put("Void", false);
            listItems[i] = obj.get("Street") as String
        }

        recordlist.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        recordlist.setOnItemClickListener { parent, view, position, id ->
            val obj = mJSONSubset[position] as JSONObject
            val intent = Intent(this, MapActivity::class.java)

            val latitude = obj.getDouble("Latitude")
            val longitude = obj.getDouble("Longitude")

            var description = ""
            description += obj.get("Street") as String + "<br><br>"

            intent.putExtra("latitude", latitude)
            intent.putExtra("longitude", longitude)
            intent.putExtra("description", description)
            intent.putExtra("rainfall", obj.getDouble("Rainfall"))
            intent.putExtra("void", obj.getBoolean("Void"))
            intent.putExtra("impervious", obj.getDouble("Impervious"))
            intent.putExtra("area", obj.getDouble("Area"))
            this.startActivity(intent);
        }

        search.addTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable)
    {
        mEditTimer.cancel()
        mEditTimer = Timer()
        mEditTimer.schedule(SearchTask(this, application, search.text.toString()), Date(System.currentTimeMillis() + 200))
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int){}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int){}
}
