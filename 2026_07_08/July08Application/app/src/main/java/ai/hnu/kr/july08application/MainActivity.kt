package ai.hnu.kr.july08application

import android.os.Bundle
import android.widget.Toast
import ai.hnu.kr.july08application.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val s1 = binding.editTextNumberDecimal.text?.toString()
            val s2 = binding.editTextNumberDecimal2.text?.toString()

            if (s1.isNullOrBlank() || s2.isNullOrBlank()) {
                Toast.makeText(this, "숫자를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val n1 = s1.toDoubleOrNull()
            val n2 = s2.toDoubleOrNull()

            if (n1 == null || n2 == null) {
                Toast.makeText(this, "유효한 숫자를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = when {
                binding.addado.isChecked -> n1 + n2
                binding.minusado.isChecked -> n1 - n2
                binding.multiado.isChecked -> n1 * n2
                binding.deviderdo.isChecked -> {
                    if (n2 == 0.0) {
                        Toast.makeText(this, "0으로 나눌 수 없습니다", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    } else n1 / n2
                }
                else -> {
                    Toast.makeText(this, "연산자를 선택하세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            binding.textResult.text = "결과: $result"
        }
    }
}