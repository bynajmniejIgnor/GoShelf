package com.example.goshelf

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.addTextChangedListener

class ShelfList : Fragment(R.layout.fragment_list) {
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        val newShelfBtn = view.findViewById<Button>(R.id.new_shelf_btn)
        val newShelfNameField = view.findViewById<EditText>(R.id.new_shelf_name)
        var newSelfName = ""

        newShelfNameField.addTextChangedListener {
            if (newShelfNameField.getText().toString().isNotEmpty()) {
                newShelfBtn.text = "Submit"
            }
            else {
                newShelfBtn.text = "Back"
            }
        }

        newShelfBtn.setOnClickListener{
            if (newShelfBtn.text == "New Shelf"){
                newShelfNameField.setText("")
                newShelfNameField.visibility = View.VISIBLE
                newShelfNameField.requestFocus()
                newShelfBtn.text = "Back"
            }
            else{
                newSelfName = newShelfNameField.getText().toString()
                if (newSelfName.isNotEmpty()) {
                    createShelf(view, newSelfName, 0)
                }
                newShelfNameField.visibility = View.GONE
                newShelfBtn.text = "New Shelf"
            }
            newShelfNameField.hideKeyboard()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createShelf(view, "Living room", 0)
        createShelf(view, "Kitchen", 0)
        createShelf(view, "Bedroom", 0)
    }

    private fun createShelf(view: View, name: String, booksOn: Int) {
        val shelfHeight = 250
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.layoutParams = layoutParams

        val addBookBtn = Button(context)
        addBookBtn.text = "+"
        val addParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            0.2f
        )
        addParams.setMargins(16, 16, 16, 16)
        addBookBtn.layoutParams = addParams
        addBookBtn.height = shelfHeight

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
        linearLayout.addView(addBookBtn)

        (view as LinearLayout).addView(linearLayout)

        val args = Bundle().apply {
            putString("shelfName", name)
        }

        shelfBtn.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragment_container, ShelfContent().apply {
                    arguments = args
                })
                replace(R.id.fragmentContainerView, BackToMain().apply {
                    arguments = args
                })
                addToBackStack(null)
                commit()
            }
        }

       addBookBtn.setOnClickListener {
           Toast.makeText(requireContext(), "Added book to shelf $name", Toast.LENGTH_SHORT).show()
       }

    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

}