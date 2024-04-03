package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout

class ShelfList : Fragment(R.layout.fragment_list) {

    private var shelves = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        val newShelfBtn = view.findViewById<Button>(R.id.new_shelf_btn)

        newShelfBtn.setOnClickListener(){
            createShelf(view, "New Shelf")
            shelves.add("New Shelf")
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createShelf(view, "Living room")
        createShelf(view, "Kitchen")
        createShelf(view, "Bedroom")

        for (shelf in shelves){
            createShelf(view, shelf)
        }
    }

    private fun createShelf(view: View, name: String) {
        val shelfHeight = 250
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)

        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.layoutParams = layoutParams

        val addBtn = Button(context)
        addBtn.text = "+"
        val addParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.2f
        )
        addParams.setMargins(16, 16, 16, 16)
        addBtn.layoutParams = addParams
        addBtn.height = shelfHeight

        val shelfBtn = Button(context)
        shelfBtn.text = name
        val shelfParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.8f
        )
        shelfParams.setMargins(16, 16, 16, 16)
        shelfBtn.layoutParams = shelfParams
        shelfBtn.height = shelfHeight

        linearLayout.addView(shelfBtn)
        linearLayout.addView(addBtn)

        (view as LinearLayout).addView(linearLayout)

        val args = Bundle().apply {
            putString("shelfName", name)
        }
        shelfBtn.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragment_container, ShelfContent().apply {
                    arguments = args
                })
                replace(R.id.fragmentContainerView, BackToMain().apply {  })
                addToBackStack(null)
                commit()
            }
        }
    }

}