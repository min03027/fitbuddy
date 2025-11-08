package com.fitbuddy.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fitbuddy.app.databinding.ActivityExerciseCategoryBinding

class ExerciseCategoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityExerciseCategoryBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupListeners()
    }
    
    private fun setupListeners() {
        // 채팅 버튼
        binding.btnChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        
        // 홈 버튼
        binding.btnHome.setOnClickListener {
            finish()
        }
        
        // 카테고리 선택
        binding.cardFullBody.setOnClickListener {
            navigateToExerciseList("full-body")
        }
        
        binding.cardUpperBody.setOnClickListener {
            navigateToExerciseList("upper-body")
        }
        
        binding.cardLowerBody.setOnClickListener {
            navigateToExerciseList("lower-body")
        }
        
        binding.cardCore.setOnClickListener {
            navigateToExerciseList("core")
        }
    }
    
    private fun navigateToExerciseList(category: String) {
        val intent = Intent(this, ExerciseListActivity::class.java).apply {
            putExtra("CATEGORY", category)
        }
        startActivity(intent)
    }
}
