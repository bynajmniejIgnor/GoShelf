package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ShelfList())
            .addToBackStack(null)
            .commit()

        //displayBackButton()
    }
    private fun isShelfContent(): Boolean{
        val fragmentTag = supportFragmentManager.getBackStackEntryAt(
            supportFragmentManager.backStackEntryCount - 1
        ).name
        return fragmentTag == "ShelfContent"
    }

    private fun displayBackButton() {
        if (isShelfContent()) {
            val backButton: Button = findViewById(R.id.back_button)
            backButton.visibility = View.VISIBLE

            /*backButton.setOnClickListener {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, ShelfList())
                    addToBackStack(null)
                    commit()
                }
            }

             */
        }
    }
}
