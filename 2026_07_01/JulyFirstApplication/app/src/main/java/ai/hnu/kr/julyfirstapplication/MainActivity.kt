package com.example.julyfirstapplication

import android.location.Address
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = TextView(this).apply {
            text = "Hello, World!"
            textSize = 24f
        }
        val image = ImageView(this).also {
            it.setImageResource(R.drawable.istock)
        }
        val address = TextView(this).apply {
            text = "Address: 123 Main St, Anytown, USA"
            textSize = 16f
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            addView(name, WRAP_CONTENT, WRAP_CONTENT)
            addView(image, 200, 100)
            addView(address, WRAP_CONTENT, WRAP_CONTENT)
        }
        setContentView(layout)
    }
}