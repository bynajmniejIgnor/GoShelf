package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class BackToMain : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_back_to_main, container, false)

        val back_btn = view.findViewById<Button>(R.id.back_btn)

        back_btn.setOnClickListener(){
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragmentContainerView, Header().apply{})
                replace(R.id.fragment_container, ShelfList().apply {  })
                addToBackStack(null)
                commit()
            }
        }
        return view
    }
}