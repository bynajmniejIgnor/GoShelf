package com.example.goshelf

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import org.w3c.dom.Text

class PickOne : Fragment() {
    data class BookInfo(
        val title: String,
        val subtitle: String?,
        val authors: List<String>
    )
    private val client = OkHttpClient()
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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_pick_one, container, false)
        val isbnBtn = view.findViewById<Button>(R.id.isbnBtn)
        val handBtn = view.findViewById<Button>(R.id.handBtn)
        val text = view.findViewById<TextView>(R.id.text)
        var shelfName = ""
        Log.d("SHELF NAME", "http://${MainActivity.getInstance().globalServerAddress}/shelfName/${MainActivity.getInstance().globalTmpShelfId}")
        httpGet("http://${MainActivity.getInstance().globalServerAddress}/shelfName/${MainActivity.getInstance().globalTmpShelfId}") { resp ->
            Log.d("RESP", resp)
            shelfName = JSONObject(resp).getString("response")
            text.setText("Add book to shelf $shelfName")
        }

        isbnBtn.setOnClickListener {
            startBarcodeScanning()
        }

        handBtn.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                replace(R.id.fragmentContainerView, BackToMain().apply{})
                replace(R.id.fragment_container, AddBookByHand().apply{})
                addToBackStack(null)
                commit()
            }
        }

        return view
    }

    private fun startBarcodeScanning() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setPrompt("Show me some book EANs")

        integrator.setOrientationLocked(true)
        integrator.setBeepEnabled(true)

        integrator.initiateScan()
    }

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
                    ""
                }
                return BookInfo(title, subtitle, authors)
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            val barcodeValue = result.contents
            Log.d("BarcodeScanner", "Scanned: $barcodeValue")
            httpGet("https://www.googleapis.com/books/v1/volumes?q=isbn:$barcodeValue") { responseBody ->
                val bookInfo = parseJson(responseBody)
                if (bookInfo != null){
                    Log.d("Title",bookInfo.title)
                    if (bookInfo.subtitle != null) Log.d("Subtitle",bookInfo.subtitle)
                    Log.d("Authors:",bookInfo.authors.toString())
                    httpGet("http://${MainActivity.getInstance().globalServerAddress}/addBook/${MainActivity.getInstance().globalTmpShelfId}/${bookInfo.title}/${bookInfo.subtitle}/${bookInfo.authors}"){}
                    Toast.makeText(requireContext(), "New book is on the shelf :))", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(requireContext(), "Sorry, I don't recognize this ISBN :((", Toast.LENGTH_SHORT).show()
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
}