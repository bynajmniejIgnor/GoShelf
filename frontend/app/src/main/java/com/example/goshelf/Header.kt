package com.example.goshelf

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.example.gobook.ShelfContent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class Header : Fragment() {
    private val client = OkHttpClient()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_header, container, false)

        val searchShelf = view.findViewById<EditText>(R.id.searchByShelfBtn)
        val searchBook = view.findViewById<EditText>(R.id.searchByBookBtn)
        val searchBtn = view.findViewById<Button>(R.id.searchBtn)

        searchShelf.setOnFocusChangeListener { _, _ ->
            searchBook.setText("")
        }
        searchBook.setOnFocusChangeListener { _, _ ->
            searchShelf.setText("")
        }

        searchShelf.addTextChangedListener {
            if (searchShelf.getText().toString().isNotEmpty()) {
                searchBtn.visibility = View.VISIBLE
                searchShelf.isCursorVisible = true
            } else {
                searchBtn.visibility = View.INVISIBLE
                searchShelf.isCursorVisible = false
            }
        }
        searchBook.addTextChangedListener {
            if (searchBook.getText().toString().isNotEmpty()) {
                searchBtn.visibility = View.VISIBLE
                searchShelf.isCursorVisible = true
            } else {
                searchBtn.visibility = View.INVISIBLE
                searchShelf.isCursorVisible = false
            }
        }

        searchBtn.setOnClickListener {
            searchShelf.hideKeyboard()
            searchBook.hideKeyboard()

            val obj: String
            val query: String

            if ( searchShelf.text.toString() != "" ) {
                obj = "shelf"
                query = searchShelf.text.toString().lowercase() //sqlite by default is case insensitive, im just sanity checking

            }  else {
                obj = "book"
                query = searchBook.text.toString().lowercase()
            }

            searchBook.setText("")
            searchShelf.setText("")

            Log.d("REQ","http://${MainActivity.getInstance().globalServerAddress}/search/$obj/${MainActivity.getInstance().globalUserId}/$query")
            httpGet("http://${MainActivity.getInstance().globalServerAddress}/search/$obj/${MainActivity.getInstance().globalUserId}/$query"){ resp ->
                Log.d("SEARCH",resp)

                val args = Bundle().apply {
                    putString("searched", resp)
                }
                activity?.supportFragmentManager?.beginTransaction()?.apply {
                    if (obj == "shelf") {
                        replace(R.id.fragment_container, ShelfList().apply {
                            arguments = args
                        })
                    } else {
                        //TODO: HANDLE BOOK SEARCHES
                    }
                    addToBackStack(null)
                    commit()
                }
            }
        }
        return view
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
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}