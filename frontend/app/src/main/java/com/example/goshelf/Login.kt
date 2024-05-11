package com.example.goshelf

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import org.json.JSONObject
import java.security.MessageDigest
import android.provider.Settings.Secure


class Login : Fragment() {
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

    //https://gist.github.com/lovubuntu/164b6b9021f5ba54cefc67f60f7a1a25
    // <3
    private fun sha256(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    @SuppressLint("HardwareIds")
    private fun setAndroidId(user_id: String) {
        val android_id = Secure.getString(context?.contentResolver, Secure.ANDROID_ID)
        Log.d("AndroidId", android_id)
        httpGet("http://${MainActivity.getInstance().globalServerAddress}/androidId/$user_id/$android_id"){ resp ->
            Log.d("AndroidId", resp)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_login, container, false)

        val loginField = rootView.findViewById<EditText>(R.id.loginField)
        val passwordField = rootView.findViewById<EditText>(R.id.passwordField)

        loginField.onFocusChangeListener = View.OnFocusChangeListener {_, hasFocus ->
            if (hasFocus) {
                loginField.hint = ""
            } else {
                loginField.hint = "username"
            }
        }

        passwordField.onFocusChangeListener = View.OnFocusChangeListener {_, hasFocus ->
            if (hasFocus) {
                passwordField.hint = ""
            } else {
                passwordField.hint = "password"
            }
        }


        val loginBtn= rootView.findViewById<Button>(R.id.loginBtn)

        loginBtn.setOnClickListener {
            Log.d("Global",MainActivity.getInstance().globalServerAddress)

            val hash = sha256(passwordField.text.toString())
            Log.d("Global", hash)
            Log.d("Global", loginField.text.toString())
            httpGet("http://${MainActivity.getInstance().globalServerAddress}/login/${loginField.text.toString()}/$hash") { responseBody ->
                try {
                    val response = JSONObject(responseBody)
                    val userId = response.getString("response").toString()

                    Log.d("Global", userId)

                    if (userId != "-1") {
                        MainActivity.getInstance().globalUserId = userId
                        setAndroidId(userId)
                        Toast.makeText(requireContext(), "Logged in as ${loginField.text}", Toast.LENGTH_SHORT).show()
                        activity?.supportFragmentManager?.beginTransaction()?.apply {
                            replace(R.id.fragmentContainerView, Header().apply{})
                            replace(R.id.fragment_container, ShelfList().apply{})
                            addToBackStack(null)
                            commit()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Incorrect user or password", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return rootView
    }
}