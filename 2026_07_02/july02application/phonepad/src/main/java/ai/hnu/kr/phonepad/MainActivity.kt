package ai.hnu.kr.phonepad

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var displayText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        displayText = findViewById(R.id.displayText)

        // 숫자 버튼 (1-9, 0)
        setupNumberButton(R.id.btn1, "1")
        setupNumberButton(R.id.btn2, "2")
        setupNumberButton(R.id.btn3, "3")
        setupNumberButton(R.id.btn4, "4")
        setupNumberButton(R.id.btn5, "5")
        setupNumberButton(R.id.btn6, "6")
        setupNumberButton(R.id.btn7, "7")
        setupNumberButton(R.id.btn8, "8")
        setupNumberButton(R.id.btn9, "9")
        setupNumberButton(R.id.btn0, "0")
        setupNumberButton(R.id.btnStar, "*")
        setupNumberButton(R.id.btnHash, "#")

        // 기능 버튼
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            onDeleteClick()
        }
        findViewById<Button>(R.id.btnDial).setOnClickListener {
            onDialClick()
        }
        findViewById<Button>(R.id.btnVideoCall).setOnClickListener {
            onVideoCallClick()
        }
    }

    private fun setupNumberButton(buttonId: Int, number: String) {
        findViewById<Button>(buttonId).setOnClickListener {
            displayText.append(number)
        }
    }

    private fun onDeleteClick() {
        val text = displayText.text.toString()
        if (text.isNotEmpty()) {
            displayText.setText(text.substring(0, text.length - 1))
        }
    }

    private fun onDialClick() {
        val phoneNumber = displayText.text.toString()
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "통화: $phoneNumber", Toast.LENGTH_SHORT).show()
            // 실제 통화 구현 시 Intent로 통화 시작
        }
    }

    private fun onVideoCallClick() {
        val phoneNumber = displayText.text.toString()
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "영상통화: $phoneNumber", Toast.LENGTH_SHORT).show()
            // 실제 영상통화 구현 시 Intent로 영상통화 시작
        }
    }
}