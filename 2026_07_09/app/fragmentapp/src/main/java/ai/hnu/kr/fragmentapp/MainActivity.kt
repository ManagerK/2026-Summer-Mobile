package ai.hnu.kr.fragmentapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import android.widget.Button
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val first = findViewById<Button>(R.id.firstbtn)
        val second = findViewById<Button>(R.id.secondbtn)
        val third = findViewById<Button>(R.id.thirdbtn)
        val bottomBar = findViewById<View>(R.id.bottom_bar)

        fun showFragment(tag: String) {
            val fragment = when (tag) {
                "first" -> BlankFragment()
                "second" -> secondFragment()
                "third" -> thirdFragment()
                else -> BlankFragment()
            }
            supportFragmentManager.commit {
                replace(R.id.fragment_container, fragment)
            }
            when (tag) {
                "first" -> bottomBar.setBackgroundColor(Color.parseColor("#CF2424"))
                "second" -> bottomBar.setBackgroundColor(Color.parseColor("#48DDBF"))
                "third" -> bottomBar.setBackgroundColor(Color.parseColor("#E66819"))
            }
        }

        if (savedInstanceState == null) {
            showFragment("first")
        }

        first.setOnClickListener { showFragment("first") }
        second.setOnClickListener { showFragment("second") }
        third.setOnClickListener { showFragment("third") }
    }
}