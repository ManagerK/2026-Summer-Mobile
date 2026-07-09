package ai.hnu.kr.july09application

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // set up toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // keep edge-to-edge padding behavior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // set up SearchView
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Search..."
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    Toast.makeText(this@MainActivity, "Search submitted: $it", Toast.LENGTH_SHORT).show()
                    findViewById<TextView>(R.id.text_view).text = "Search submitted: $it"
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                findViewById<TextView>(R.id.text_view).text = "Searching: ${newText ?: ""}"
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val tv = findViewById<TextView>(R.id.text_view)
        return when (item.itemId) {
            R.id.menu1 -> {
                Toast.makeText(this, "Menu 1 selected", Toast.LENGTH_SHORT).show()
                tv.text = "Menu 1 selected"
                true
            }
            R.id.menu2 -> {
                Toast.makeText(this, "Menu 2 selected", Toast.LENGTH_SHORT).show()
                tv.text = "Menu 2 selected"
                true
            }
            R.id.menu3 -> {
                Toast.makeText(this, "Menu 3 selected", Toast.LENGTH_SHORT).show()
                tv.text = "Menu 3 selected"
                true
            }
            android.R.id.home -> {
                Toast.makeText(this, "Up pressed", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}