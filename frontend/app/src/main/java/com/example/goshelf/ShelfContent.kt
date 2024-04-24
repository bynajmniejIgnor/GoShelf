package com.example.goshelf

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ShelfContent : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_shelf_content, container, false)

        val shelfName = arguments?.getString("shelfName")

        val textView: TextView = rootView.findViewById(R.id.shelf_name)
        textView.text = shelfName + " books"

        return rootView
    }
}