package ai.hnu.kr.july07application

import ai.hnu.kr.july07application.databinding.ActivityThirdBinding
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ThirdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainbtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.secondbtn.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.fourthbtn.setOnClickListener {
            val intent = Intent(this, FourthActivity::class.java)
            startActivity(intent)
        }
    }
}