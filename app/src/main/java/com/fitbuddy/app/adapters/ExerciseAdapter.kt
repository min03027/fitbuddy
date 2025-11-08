package com.fitbuddy.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fitbuddy.app.databinding.ItemExerciseBinding
import com.fitbuddy.app.models.Exercise

class ExerciseAdapter(
    private val exercises: List<Exercise>,
    private val onStartClick: (Exercise) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {
    
    inner class ExerciseViewHolder(private val binding: ItemExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(exercise: Exercise) {
            binding.tvExerciseName.text = exercise.name
            binding.tvDuration.text = exercise.duration
            binding.tvCalories.text = exercise.calories
            
            binding.btnStart.setOnClickListener {
                onStartClick(exercise)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExerciseViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercises[position])
    }
    
    override fun getItemCount() = exercises.size
}
