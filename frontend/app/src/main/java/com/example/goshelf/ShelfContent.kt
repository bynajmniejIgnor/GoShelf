package com.example.gobook

import android.app.AlertDialog
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
import java.lang.reflect.InvocationTargetException

class ShelfContent : Fragment() {
    private val client = OkHttpClient()
    private fun displayBook(view: View, bookId: String, name: String, subtitle: String, authors: String, shelfId: String) {
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

        val delBookBtn = Button(requireContext())
        val delBookParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.2f
        )
        delBookParams.setMargins(8, 47, 8, 8)
        delBookBtn.layoutParams = delBookParams
        delBookParams.height = bookHeight
        delBookBtn.text = "\uD83D\uDDD1\uFE0F"

        var shelfName = ""
        try {
            httpGet("http://${MainActivity.getInstance().globalServerAddress}/shelfName/$shelfId") { resp ->
                shelfName = JSONObject(resp).getString("response")
            }
        } catch (e: InvocationTargetException) {
            Log.d("EXCEPTION",e.cause.toString())
        }

        delBookBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete book")
            builder.setMessage("Are you sure you want to delete (incinerate) book $name on shelf $shelfName?")

            builder.setPositiveButton("Confirm") { dialog, _ ->
                val args = Bundle().apply {
                    putString("shelfId", shelfId)
                    putString("shelfName", shelfName)
                }
                httpGet("http://${MainActivity.getInstance().globalServerAddress}/deleteBook/$bookId"){}
                Toast.makeText(context, "Book $name incinerated", Toast.LENGTH_SHORT).show()
                activity?.supportFragmentManager?.beginTransaction()?.apply {
                    replace(R.id.fragment_container, ShelfContent().apply{
                        arguments = args
                    })

                    replace(R.id.fragmentContainerView, BackToMain().apply{})
                    addToBackStack(null)
                    commit()
                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                Toast.makeText(context, "Incineration cancelled", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
        linearLayout.addView(delBookBtn)

        val bookBtn = Button(requireContext())
        val bookParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.9f
        )
        bookParams.setMargins(8, 8, 8, 8)
        bookBtn.layoutParams = bookParams
        bookBtn.height = bookHeight
        bookBtn.text = name + "\n" + subtitle + "\n" + authors
        bookBtn.textSize = 12f
        linearLayout.addView(bookBtn)

        val booksSearched = arguments?.getString("bookSearch")
        if (!booksSearched.isNullOrEmpty()) {
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
            shelfParams.setMargins(8, 47, 8, 8)
            shelf.layoutParams = shelfParams
            shelf.height = bookHeight
            shelf.text = "➡️"
            var shelfName = ""
            httpGet("http://${MainActivity.getInstance().globalServerAddress}/shelfName/$shelfId") { resp ->
                shelfName = JSONObject(resp).getString("response")
            }
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
        Log.d("TESTING", response)
        if (response == "null") {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragmentContainerView, BackToMain().apply{})
                addToBackStack(null)
                commit()
            }
            return
        }
        val books = JSONArray(response)
        for (i in 0 until books.length()) {
            val title = books.getJSONObject(i).getString("Title")
            val subtitle = books.getJSONObject(i).getString("Subtitle")
            val authors = books.getJSONObject(i).getString("Authors")
            val shelfId = books.getJSONObject(i).getString("Shelf_id")
            val bookId = books.getJSONObject(i).getString("Book_id")
            displayBook(view, bookId, title, subtitle, authors, shelfId)
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
            val response = JSONObject(booksSearched).getString("response")
            if (response == "null") textView.text = "No books found :(("
            else textView.text = "Books found"
            Log.d("BOOK SEARCH", booksSearched)
            parseBooks(rootView, booksSearched)
        }
        return rootView
    }
}