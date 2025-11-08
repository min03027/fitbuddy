package com.fitbuddy.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitbuddy.app.adapters.ExerciseAdapter
import com.fitbuddy.app.databinding.ActivityExerciseListBinding
import com.fitbuddy.app.models.Exercise

class ExerciseListActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityExerciseListBinding
    private lateinit var exerciseAdapter: ExerciseAdapter
    private val exercises = mutableListOf<Exercise>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val category = intent.getStringExtra("CATEGORY") ?: "full-body"
        
        setupToolbar(category)
        loadExercises(category)
        setupRecyclerView()
        setupListeners()
    }
    
    private fun setupToolbar(category: String) {
        val categoryName = when (category) {
            "full-body" -> "전신"
            "upper-body" -> "상체"
            "lower-body" -> "하체"
            "core" -> "복부"
            else -> "운동"
        }
        binding.tvTitle.text = "$categoryName 운동"
    }
    
    private fun loadExercises(category: String) {
        exercises.clear()
        
        when (category) {
            "full-body" -> {
                exercises.add(Exercise(1, "버피 테스트", "30초", "15kcal"))
                exercises.add(Exercise(2, "마운틴 클라이머", "45초", "12kcal"))
                exercises.add(Exercise(3, "점핑 잭", "60초", "18kcal"))
            }
            "upper-body" -> {
                exercises.add(Exercise(4, "푸시업", "30초", "10kcal"))
                exercises.add(Exercise(5, "덤벨 컬", "45초", "8kcal"))
                exercises.add(Exercise(6, "숄더 프레스", "40초", "9kcal"))
            }
            "lower-body" -> {
                exercises.add(Exercise(7, "스쿼트", "60초", "15kcal"))
                exercises.add(Exercise(8, "런지", "45초", "12kcal"))
                exercises.add(Exercise(9, "레그 레이즈", "40초", "10kcal"))
            }
            "core" -> {
                exercises.add(Exercise(10, "플랭크", "60초", "8kcal"))
                exercises.add(Exercise(11, "크런치", "45초", "7kcal"))
                exercises.add(Exercise(12, "러시안 트위스트", "50초", "9kcal"))
            }
        }
    }
    
    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter(exercises) { exercise ->
            Toast.makeText(this, "${exercise.name} 시작!", Toast.LENGTH_SHORT).show()
        }
        
        binding.rvExercises.apply {
            layoutManager = LinearLayoutManager(this@ExerciseListActivity)
            adapter = exerciseAdapter
        }
    }
    
    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, WeightTrackerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }
    }
}
