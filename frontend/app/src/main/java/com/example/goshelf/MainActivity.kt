package com.example.goshelf

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings.Secure
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private fun httpGet(url: String, callback: (String) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback("")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                callback(responseBody)
            }
        })
    }
    var globalServerAddress: String = "192.168.8.106:8080"
    var globalUserId: String = ""
    var globalAndroidId: String = ""

    companion object {
        private lateinit var instance: MainActivity

        fun getInstance(): MainActivity {
            return instance
        }
    }
    @SuppressLint("CommitTransaction", "HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        globalAndroidId = Secure.getString(contentResolver, Secure.ANDROID_ID)
        setContentView(R.layout.activity_main)

        httpGet("http://$globalServerAddress/androidId/$globalAndroidId"){ resp ->
            val response = JSONObject(resp)
            val userId = response.getString("response").toString()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, Header())
                .addToBackStack(null)
                .commit()
            if (userId != "" ) {
                globalUserId = userId
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ShelfList())
                    .addToBackStack(null)
                    .commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, Login())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is ShelfList || currentFragment is Login) {
            return
        }
        super.onBackPressed()
    }
}
