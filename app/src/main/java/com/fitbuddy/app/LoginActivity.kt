package com.fitbuddy.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitbuddy.app.databinding.ActivityLoginBinding
import com.fitbuddy.app.network.ApiClient
import com.fitbuddy.app.network.LoginRequest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 서버 연결 여부 테스트 (선택)
        lifecycleScope.launch {
            try {
                val res = ApiClient.api.ping()
                Log.d("API_TEST", "Response: $res")
            } catch (e: Exception) {
                Log.e("API_TEST", "Error: ${e.message}", e)
            }
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (!validateInput(email, password)) return@setOnClickListener

            // 🔥 서버 로그인 API 호출
            lifecycleScope.launch {
                try {
                    val req = LoginRequest(email, password)
                    val res = ApiClient.api.login(req)

                    if (res.success) {
                        Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, WeightTrackerActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(this@LoginActivity, res.message, Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Log.e("LOGIN_API", "Error: ${e.message}", e)
                    Toast.makeText(this@LoginActivity, "로그인 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "유효한 이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
