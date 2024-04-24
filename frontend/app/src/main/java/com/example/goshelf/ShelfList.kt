package com.example.goshelf

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.CaptureActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


class ShelfList : Fragment(R.layout.fragment_list) {
    private val client = OkHttpClient()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_list, container, false)

        val newShelfBtn = view.findViewById<Button>(R.id.new_shelf_btn)
        val newShelfNameField = view.findViewById<EditText>(R.id.new_shelf_name)
        var newShelfName = ""

        newShelfNameField.addTextChangedListener {
            if (newShelfNameField.getText().toString().isNotEmpty()) {
                newShelfBtn.text = "Submit"
            }
            else {
                newShelfBtn.text = "Back"
            }
        }

        newShelfBtn.setOnClickListener{
            if (newShelfBtn.text == "New Shelf"){
                newShelfNameField.setText("")
                newShelfNameField.visibility = View.VISIBLE
                newShelfNameField.requestFocus()
                newShelfBtn.text = "Back"
            }
            else{
                newShelfName = newShelfNameField.getText().toString()
                if (newShelfName.isNotEmpty()) {
                    createShelf(view, newShelfName, 0)
                }
                newShelfNameField.visibility = View.GONE
                newShelfBtn.text = "New Shelf"
            }
            newShelfNameField.hideKeyboard()
        }
        return view
    }

    private fun displayShelves(view: View, rawJson: String) {
        try {
            val response = JSONObject(rawJson).getString("response")
            val shelves = JSONArray(response)
            Log.d("Shelves",shelves.toString())
                for (i in 0 until shelves.length()){
                    val name = shelves.getJSONObject(i).getString("Name")
                    val booksStored = shelves.getJSONObject(i).getString("Books_stored")
                    createShelf(view, name, booksStored.toInt())
                }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        httpGet("http://192.168.0.168:8080/shelves/1") { responseBody ->
            displayShelves(view, responseBody)
        }
    }

    private fun createShelf(view: View, name: String, booksOn: Int) {
        val scrollView = view.findViewById<ScrollView>(R.id.scrollView)
        val innerLinearLayout = scrollView.findViewById<LinearLayout>(R.id.inner_linear_layout)

        val shelfHeight = 250
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.layoutParams = layoutParams

        val addBookBtn = Button(requireContext())
        addBookBtn.text = "+"
        val addParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.2f
        )
        addParams.setMargins(8, 8, 8, 8)
        addBookBtn.layoutParams = addParams
        addBookBtn.height = shelfHeight

        val shelfBtn = Button(requireContext())
        shelfBtn.text = name
        val shelfParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.8f
        )
        shelfParams.setMargins(8, 8, 8, 8)
        shelfBtn.layoutParams = shelfParams
        shelfBtn.height = shelfHeight

        linearLayout.addView(shelfBtn)
        linearLayout.addView(addBookBtn)

        innerLinearLayout.addView(linearLayout)

        val args = Bundle().apply {
            putString("shelfName", name)
        }

        shelfBtn.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragmentContainerView, BackToMain().apply{})
                replace(R.id.fragment_container, ShelfContent().apply{
                    arguments = args
                })
                addToBackStack(null)
                commit()
            }
        }

        addBookBtn.setOnClickListener {
            startBarcodeScanning()
            Toast.makeText(requireContext(), "Added book to shelf $name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun httpGet(url: String, callback: (String) -> Unit) {
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

    data class BookInfo(
        val title: String,
        val subtitle: String?,
        val authors: List<String>
    )
    private fun parseJson(rawJson: String): BookInfo? {
        try {
            val items = JSONObject(rawJson).getJSONArray("items")
            if (items.length() > 0) {
                val volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo")
                val title = volumeInfo.getString("title")
                val authorsArray = volumeInfo.getJSONArray("authors")

                val authors = mutableListOf<String>()
                for (i in 0 until authorsArray.length()) {
                    authors.add(authorsArray.getString(i))
                }

                val subtitle = if (volumeInfo.has("subtitle")) {
                    volumeInfo.getString("subtitle")
                } else {
                    null
                }

               return BookInfo(title, subtitle, authors)
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun startBarcodeScanning() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setPrompt("Show me some book EANs")

        // god bless https://stackoverflow.com/questions/33550042/zxing-embedded-setcaptureactivity-causes-that-the-activity-does-not-appear
        // now i can rotate my scanner
        integrator.setOrientationLocked(false)
        integrator.setBeepEnabled(true)

        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show()
            } else {
                val barcodeValue = result.contents
                Log.d("BarcodeScanner", "Scanned: $barcodeValue")
                httpGet("https://www.googleapis.com/books/v1/volumes?q=isbn:$barcodeValue") { responseBody ->
                    val bookInfo = parseJson(responseBody)
                    if (bookInfo != null){
                        Log.d("Title",bookInfo.title)
                        if (bookInfo.subtitle != null) Log.d("Subtitle",bookInfo.subtitle)
                        Log.d("Authors:",bookInfo.authors.toString())
                    }

                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBarcodeScanning()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission required for scanning",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}

class CaptureActivityPortrait : CaptureActivity() {

}
