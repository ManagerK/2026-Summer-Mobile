package ai.hnu.kr.picker

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var timeDisplay: TextView
    private lateinit var timeDisplay2: TextView
    private lateinit var dateDisplay: TextView
    private lateinit var dateDisplay2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timeDisplay = findViewById(R.id.timeDisplay)
        timeDisplay2 = findViewById(R.id.timeDisplay2)
        dateDisplay = findViewById(R.id.dateDisplay)
        dateDisplay2 = findViewById(R.id.dateDisplay2)

        val button1 = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)
        val button4 = findViewById<Button>(R.id.button4)

        button1.setOnClickListener {
            showTimePicker { hour, minute ->
                timeDisplay.text = String.format("%02d:%02d", hour, minute)
            }
        }

        button2.setOnClickListener {
            showTimePicker { hour, minute ->
                timeDisplay2.text = String.format("%02d:%02d", hour, minute)
            }
        }

        button3.setOnClickListener {
            showDatePicker { year, month, day ->
                dateDisplay.text = String.format("%04d-%02d-%02d", year, month + 1, day)
            }
        }

        button4.setOnClickListener {
            showDatePicker { year, month, day ->
                dateDisplay2.text = String.format("%04d-%02d-%02d", year, month + 1, day)
            }
        }
    }

    private fun showTimePicker(onTimeSet: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                onTimeSet(selectedHour, selectedMinute)
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private fun showDatePicker(onDateSet: (Int, Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                onDateSet(selectedYear, selectedMonth, selectedDay)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}