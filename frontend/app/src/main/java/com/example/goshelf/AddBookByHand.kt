package com.example.goshelf

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class AddBookByHand : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_add_book_by_hand, container, false)
        val titleField = view.findViewById<EditText>(R.id.titleField)
        val subtitleField = view.findViewById<EditText>(R.id.subtitleField)
        val authorsField = view.findViewById<EditText>(R.id.authorsField)
        val btn = view.findViewById<Button>(R.id.submitBtn)

        btn.setOnClickListener {
            if (titleField.text.isEmpty() || subtitleField.text.isEmpty() || authorsField.text.isEmpty() ) {
                Toast.makeText(requireContext(), "All the above fields are mandatory you silly :3", Toast.LENGTH_SHORT).show()
            } else {
                httpGet("http://${MainActivity.getInstance().globalServerAddress}/addBook/${MainActivity.getInstance().globalTmpShelfId}/${titleField.text}/${subtitleField.text}/${authorsField.text}"){}
                Toast.makeText(requireContext(), "New book is on the shelf :))", Toast.LENGTH_SHORT).show()
                activity?.supportFragmentManager?.beginTransaction()?.apply {
                    setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right, R.anim.enter_left_to_right, R.anim.exit_right_to_left)
                    replace(R.id.fragmentContainerView, BackToMain().apply{})
                    replace(R.id.fragment_container, PickOne().apply{})
                    addToBackStack(null)
                    commit()
                }
            }
        }
        return view
    }
}