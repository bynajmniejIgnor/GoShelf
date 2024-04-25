package com.example.gobook

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.goshelf.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

class ShelfContent : Fragment() {

    private val client = OkHttpClient()
    private var shelfId: String = ""
    private fun displayBook(view: View, name: String) {
        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)
        val innerLinearLayout = scrollView.findViewById<LinearLayout>(R.id.inner_linear_layout)

        val bookHeight = 250
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.layoutParams = layoutParams

        val bookBtn = Button(requireContext())
        bookBtn.text = name
        val bookParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.8f
        )
        bookParams.setMargins(8, 8, 8, 8)
        bookBtn.layoutParams = bookParams
        bookBtn.height = bookHeight

        linearLayout.addView(bookBtn)

        innerLinearLayout.addView(linearLayout)

        val args = Bundle().apply {
            putString("bookName", name)
        }

        bookBtn.setOnClickListener {

        }
    }

    fun httpGet(url: String, callback: (String) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    callback.invoke("")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""

                requireActivity().runOnUiThread {
                    callback.invoke(responseBody)
                }
            }
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_shelf_content, container, false)

        val textView: TextView = rootView.findViewById(R.id.shelf_name)

        val shelfName = arguments?.getString("shelfName")
        shelfId = arguments?.getString("shelfId").toString()
        textView.text = shelfName + " books"

        httpGet("http://192.168.0.168:8080/books/$shelfId") { responseBody ->
            Log.d("Books", responseBody)
            try {
                val response = JSONObject(responseBody).getString("response")
                val books = JSONArray(response)
                for (i in 0 until books.length()) {
                    val title = books.getJSONObject(i).getString("Title")
                    val subtitle = books.getJSONObject(i).getString("Subtitle")
                    val authors = books.getJSONObject(i).getString("Authors")
                    displayBook(rootView, title)
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return rootView
    }
}