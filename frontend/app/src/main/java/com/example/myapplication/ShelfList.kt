package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout

class ShelfList : Fragment(R.layout.fragment_list) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createShelf(view, "Easy track")
        createShelf(view, "Medium track")
        createShelf(view, "Hard track")
    }

    private fun createShelf(view: View, name: String) {
        val button = Button(context)
        button.text = name

        val args = Bundle().apply {
            putString("shelfName", name)
        }
        button.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragment_container, ShelfContent().apply {
                    arguments = args
                })
                addToBackStack(null)
                commit()
            }
        }
        (view as LinearLayout).addView(button)
    }
}