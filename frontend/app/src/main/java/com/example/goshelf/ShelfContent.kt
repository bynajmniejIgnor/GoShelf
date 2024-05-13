package com.example.gobook

import android.os.Bundle
import android.text.BoringLayout
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
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.example.goshelf.BackToMain
import com.example.goshelf.MainActivity
import com.example.goshelf.R
import com.example.goshelf.ShelfList
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
    private fun displayBook(view: View, name: String, subtitle: String, authors: String, shelfId: String?) {
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
        val bookParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.8f
        )
        bookParams.setMargins(8, 8, 8, 8)
        bookBtn.layoutParams = bookParams
        bookBtn.height = bookHeight
        bookBtn.text = name + "\n" + subtitle + "\n" + authors
        linearLayout.addView(bookBtn)

        if (shelfId?.isEmpty() == false) {
            val backBtn = view.findViewById<Button>(R.id.backBtn)
            backBtn.visibility = View.VISIBLE
            backBtn.setOnClickListener {
                activity?.supportFragmentManager?.beginTransaction()?.apply {
                    replace(R.id.fragment_container, ShelfList().apply{})
                    addToBackStack(null)
                    commit()
                }
            }

            val shelf = Button(requireContext())
            val shelfParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.2f
            )
            shelfParams.setMargins(8, 50, 8, 8)
            shelf.layoutParams = shelfParams
            shelf.height = bookHeight
            shelf.text = "➡️"
            var shelfName = ""
            httpGet("http://${MainActivity.getInstance().globalServerAddress}/shelfName/$shelfId") { resp ->
                shelfName = JSONObject(resp).getString("response")
            }
            Log.d("SHELFNAME", shelfName)
            shelf.setOnClickListener {
                val args = Bundle()
                args.apply {
                    putString("shelfId", shelfId)
                    putString("shelfName", shelfName)
                }
                activity?.supportFragmentManager?.beginTransaction()?.apply {

                    replace(R.id.fragmentContainerView, BackToMain().apply{})
                    replace(R.id.fragment_container, ShelfContent().apply{
                        arguments = args
                    })
                    addToBackStack(null)
                    commit()
                }
            }
            linearLayout.addView(shelf)
        }

        innerLinearLayout.addView(linearLayout)
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

    private fun parseBooks(view: View, rawJson: String) {
        val response = JSONObject(rawJson).getString("response")
        val books = JSONArray(response)
        for (i in 0 until books.length()) {
            val title = books.getJSONObject(i).getString("Title")
            val subtitle = books.getJSONObject(i).getString("Subtitle")
            val authors = books.getJSONObject(i).getString("Authors")
            val shelfId = books.getJSONObject(i)?.getString("Shelf_id")
            displayBook(view, title, subtitle, authors, shelfId)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_shelf_content, container, false)

        val textView: TextView = rootView.findViewById(R.id.shelf_name)

        val shelfName = arguments?.getString("shelfName")
        val shelfId = arguments?.getString("shelfId")
        val booksSearched = arguments?.getString("bookSearch")

        if (booksSearched.isNullOrEmpty()) {
            textView.text = shelfName + " books"
            httpGet("http://${MainActivity.getInstance().globalServerAddress}/books/$shelfId") { responseBody ->
                Log.d("Books REQ", "http://${MainActivity.getInstance().globalServerAddress}/books/$shelfId")
                Log.d("Books", responseBody)
                try {
                    parseBooks(rootView, responseBody)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            textView.text = "Books found"
            Log.d("BOOK SEARCH", booksSearched)
            parseBooks(rootView, booksSearched)
        }
        return rootView
    }
}