package com.fitbuddy.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.fitbuddy.app.databinding.ActivityWeightTrackerBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeightTrackerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWeightTrackerBinding
    private var currentWeight = 65.0f
    private val weightHistory = mutableListOf<Pair<String, Float>>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeightTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 초기 체중 설정
        val initialWeight = intent.getStringExtra("INITIAL_WEIGHT")?.toFloatOrNull() ?: 65.0f
        currentWeight = initialWeight
        
        initializeWeightHistory()
        updateUI()
        setupListeners()
        setupChart()
    }
    
    private fun initializeWeightHistory() {
        weightHistory.clear()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

        // 오늘부터 과거 6일까지 (총 7일) 데이터 생성
        for (i in 6 downTo 0) {
            val day = calendar.clone() as Calendar
            day.add(Calendar.DATE, -i)
            val dateString = dateFormat.format(day.time)
            
            // 임의의 체중 변화 데이터 (실제 앱에서는 저장된 데이터 사용)
            val randomChange = (-2..2).random() * 0.5f
            val weight = if (i == 0) currentWeight else currentWeight + randomChange
            
            weightHistory.add(dateString to weight)
        }
    }
    
    private fun setupListeners() {
        binding.btnAddWeight.setOnClickListener {
            showWeightInputDialog()
        }
        
        binding.cardExercise.setOnClickListener {
            startActivity(Intent(this, ExerciseCategoryActivity::class.java))
        }
        
        binding.cardChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }
    
    private fun setupChart() {
        val entries = ArrayList<Entry>()
        weightHistory.forEachIndexed { index, pair ->
            entries.add(Entry(index.toFloat(), pair.second))
        }
        
        val dataSet = LineDataSet(entries, "체중 변화").apply {
            color = Color.parseColor("#6366F1")
            setCircleColor(Color.parseColor("#6366F1"))
            lineWidth = 3f
            circleRadius = 5f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        
        val lineData = LineData(dataSet)
        binding.chart.apply {
            data = lineData
            description.isEnabled = false
            legend.isEnabled = false
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(weightHistory.map { it.first })
                granularity = 1f
                setDrawGridLines(false)
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E5E7EB")
            }
            
            axisRight.isEnabled = false
            
            animateX(1000)
            invalidate()
        }
    }
    
    private fun showWeightInputDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_weight_input, null)
        val etWeight = dialogView.findViewById<EditText>(R.id.et_weight)
        
        builder.setView(dialogView)
            .setTitle("오늘의 체중")
            .setPositiveButton("확인") { _, _ ->
                val weight = etWeight.text.toString().toFloatOrNull()
                if (weight != null) {
                    currentWeight = weight
                    // 마지막 날짜(오늘)의 체중 업데이트
                    val todayDate = weightHistory.last().first
                    weightHistory[weightHistory.size - 1] = todayDate to weight
                    updateUI()
                    setupChart()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    private fun updateUI() {
        binding.tvCurrentWeight.text = String.format("%.1fkg", currentWeight)
    }
}
