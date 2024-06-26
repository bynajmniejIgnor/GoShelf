package com.example.goshelf

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.example.gobook.ShelfContent
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


class ShelfList : Fragment(R.layout.fragment_list) {
    private val client = OkHttpClient()
    private val returnFromShelfSearch = "Back to all them shelves"

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
            } else {
                newShelfBtn.text = "Back"
            }
        }

        newShelfBtn.setOnClickListener{
            if (newShelfBtn.text == "New Shelf") {
                newShelfNameField.setText("")
                newShelfNameField.visibility = View.VISIBLE
                newShelfNameField.requestFocus()
                newShelfBtn.text = "Back"
            } else if (newShelfBtn.text == returnFromShelfSearch) {
                activity?.supportFragmentManager?.beginTransaction()?.apply {
                    replace(R.id.fragment_container, ShelfList().apply{})
                    addToBackStack(null)
                    commit()
                }
            } else {
                newShelfName = newShelfNameField.getText().toString()
                if (newShelfName.isNotEmpty()) {
                    httpGet("http://${MainActivity.getInstance().globalServerAddress}/addShelf/${MainActivity.getInstance().globalUserId}/$newShelfName"){ resp ->
                        val shelf_id = JSONObject(resp).getString("response")
                        createShelf(view, newShelfName, 0, shelf_id)
                    }
                }
                newShelfNameField.visibility = View.GONE
                newShelfBtn.text = "New Shelf"
            }
            newShelfNameField.hideKeyboard()
        }

        val searched = arguments?.getString("shelfSearch")
        if (searched.isNullOrEmpty()) {
            httpGet("http://${MainActivity.getInstance().globalServerAddress}/shelves/${MainActivity.getInstance().globalUserId}") { responseBody ->
                displayShelves(view, responseBody)
            }
        } else {
            newShelfBtn.text = returnFromShelfSearch
            displayShelves(view, searched)
        }
        return view
    }

    private fun displayShelves(view: View, rawJson: String) {
        try {
            val response = JSONObject(rawJson).getString("response")
            val shelves = JSONArray(response)
                for (i in 0 until shelves.length()){
                    val name = shelves.getJSONObject(i).getString("Name")
                    val booksStored = shelves.getJSONObject(i).getString("Books_stored")
                    val shelfId = shelves.getJSONObject(i).getString("Shelf_id")
                    createShelf(view, name, booksStored.toInt(), shelfId)
                }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun createShelf(view: View, name: String, booksOn: Int, id: String) {
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

        val delBookBtn = Button(requireContext())
        delBookBtn.text = "\uD83D\uDDD1\uFE0F"
        val delParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.2f
        )
        delParams.setMargins(8, 8, 8, 8)
        delBookBtn.layoutParams = addParams
        delBookBtn.height = shelfHeight

        val shelfBtn = Button(requireContext())
        shelfBtn.text = name
        val shelfParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.9f
        )
        shelfParams.setMargins(8, 8, 8, 8)
        shelfBtn.layoutParams = shelfParams
        shelfBtn.height = shelfHeight


        linearLayout.addView(delBookBtn)
        linearLayout.addView(shelfBtn)
        linearLayout.addView(addBookBtn)

        innerLinearLayout.addView(linearLayout)

        val args = Bundle().apply {
            putString("shelfName", name)
            putString("shelfId", id)
        }

        shelfBtn.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                replace(R.id.fragmentContainerView, BackToMain().apply{})
                replace(R.id.fragment_container, ShelfContent().apply{
                    arguments = args
                })
                addToBackStack(null)
                commit()
            }
        }

        addBookBtn.setOnClickListener {
            MainActivity.getInstance().globalTmpShelfId = id
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                replace(R.id.fragmentContainerView, BackToMain().apply{})
                replace(R.id.fragment_container, PickOne().apply{
                    arguments = args
                })
                addToBackStack(null)
                commit()
            }
        }

        delBookBtn.setOnClickListener{
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete shelf")
            builder.setMessage("Are you sure you want to delete (disintegrate) shelf $name?")

            builder.setPositiveButton("Confirm") { dialog, _ ->
                httpGet("http://${MainActivity.getInstance().globalServerAddress}/deleteShelf/$id"){}
                Toast.makeText(context, "Shelf $name disintegrated", Toast.LENGTH_SHORT).show()
                activity?.supportFragmentManager?.beginTransaction()?.apply {
                    replace(R.id.fragment_container, ShelfList().apply{})
                    addToBackStack(null)
                    commit()
                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                Toast.makeText(context, "Disintegration cancelled", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
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



    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
 }

class CaptureActivityPortrait : CaptureActivity() {

}