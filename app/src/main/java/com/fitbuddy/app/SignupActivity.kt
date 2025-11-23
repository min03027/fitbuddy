package com.fitbuddy.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitbuddy.app.databinding.ActivitySignupBinding
import com.fitbuddy.app.network.ApiClient
import com.fitbuddy.app.network.SignupRequest
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // 회원가입 버튼
        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val name = binding.etName.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val passwordConfirm = binding.etPasswordConfirm.text.toString()

            if (!validateInput(email, name, password, passwordConfirm)) return@setOnClickListener

            // ✅ 회원가입 API 호출
            lifecycleScope.launch {
                try {
                    val req = SignupRequest(
                        email = email,
                        password = password,
                        name = name
                    )
                    val res = ApiClient.api.signup(req)

                    if (res.success) {
                        Toast.makeText(
                            this@SignupActivity,
                            res.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        // ✅ 회원가입 성공 후 → UserInfoActivity로 이동
                        val intent = Intent(this@SignupActivity, UserInfoActivity::class.java).apply {
                            putExtra("EMAIL", email)
                            putExtra("NAME", name)
                            putExtra("PASSWORD", password)
                        }
                        startActivity(intent)
                        finish()

                    } else {
                        // 서버에서 온 에러 메시지 표시 (이미 가입된 이메일 등)
                        Toast.makeText(
                            this@SignupActivity,
                            res.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {
                    Log.e("SIGNUP_API", "Error: ${e.message}", e)
                    Toast.makeText(
                        this@SignupActivity,
                        "회원가입 실패: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // 로그인 텍스트
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun validateInput(
        email: String,
        name: String,
        password: String,
        passwordConfirm: String
    ): Boolean {
        if (email.isEmpty() || name.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "유효한 이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 8) {
            Toast.makeText(this, "비밀번호는 8자 이상이어야 합니다", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != passwordConfirm) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
