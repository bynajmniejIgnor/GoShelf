package com.example.goshelf

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultCallback
import androidx.core.widget.addTextChangedListener
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

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
        addParams.setMargins(16, 16, 16, 16)
        addBookBtn.layoutParams = addParams
        addBookBtn.height = shelfHeight

        val shelfBtn = Button(requireContext())
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

        innerLinearLayout.addView(linearLayout) // Add to the inner LinearLayout

        val args = Bundle().apply {
            putString("shelfName", name)
        }

        shelfBtn.setOnClickListener {
            // Handle shelfBtn click
            // Replace fragments, etc.
            Toast.makeText(requireContext(), "Shelf Button Clicked: $name", Toast.LENGTH_SHORT).show()
        }

        addBookBtn.setOnClickListener {
            startBarcodeScanning()
            Toast.makeText(requireContext(), "Added book to shelf $name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startBarcodeScanning() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setPrompt("Scan a barcode")
        integrator.setOrientationLocked(true)
        integrator.setBeepEnabled(true)

        integrator.addExtra("SCAN_FOCUS_MODES", "AUTO")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Show me some EAN", Toast.LENGTH_SHORT).show()
            } else {
                // Barcode scanned successfully
                val barcodeValue = result.contents
                Toast.makeText(requireContext(), "Scanned: $barcodeValue", Toast.LENGTH_SHORT).show()
                Log.d("BarcodeScanner", "Scanned: $barcodeValue")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, start scanning
                startBarcodeScanning()
            } else {
                // Camera permission denied
                Toast.makeText(
                    requireContext(),
                    "Camera permission required for scanning",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}