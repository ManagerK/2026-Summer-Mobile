package ai.hnu.kr.dogcatshow

import ai.hnu.kr.dogcatshow.databinding.ActivityMainBinding
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView (binding.root)
        vinding.dogview.visibility=View.INVISIBLE

        binding.dogButton.setOnclickListener {
            binding.dogview.visibility=View.VISIBLE
            binding.dogview.visibility=View.INVISIBLE
        }

        binding.catButton.setOnclickListener {
            binding.catview.visibility=View.INVISIBLE
            binding.catview.visibility=View.VISIBLE
        }
    }
}