package com.example.goshelf

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

        val backBtn = view.findViewById<Button>(R.id.back_btn)
        backBtn.setOnClickListener(){
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right, R.anim.enter_left_to_right, R.anim.exit_right_to_left)
                replace(R.id.fragmentContainerView, Header().apply{})
                replace(R.id.fragment_container, ShelfList().apply {})
                addToBackStack(null)
                commit()
            }
        }
        return view
    }
}