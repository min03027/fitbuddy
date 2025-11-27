package com.fitbuddy.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fitbuddy.app.databinding.ActivityExerciseExecutionBinding
import com.fitbuddy.app.network.ApiClient
import com.fitbuddy.app.network.PoseImageRequest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ExerciseExecutionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExerciseExecutionBinding
    private lateinit var cameraExecutor: ExecutorService

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var isRunning = false

    // 포즈 분석 관련
    private var lastPoseSentAt: Long = 0L
    private val poseIntervalMs: Long = 700L   // 0.7초마다 한 번씩 서버로 프레임 전송

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseExecutionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 카메라 권한 확인
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }

        setupUI()
        setupListeners()
        startTimer()
    }

    private fun setupUI() {
        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: "운동"
        val exerciseDuration = intent.getStringExtra("EXERCISE_DURATION") ?: "30초"
        binding.tvExerciseName.text = exerciseName

        val seconds = exerciseDuration.replace("초", "").trim().toIntOrNull() ?: 30
        timeLeftInMillis = seconds * 1000L
        updateTimerText()

        // 초기 피드백
        binding.tvFeedback.text = "카메라를 정면으로 바라봐 주세요 👀"
    }

    private fun setupListeners() {
        binding.btnPause.setOnClickListener {
            if (isRunning) {
                pauseTimer()
                binding.btnPause.text = "계속하기"
            } else {
                resumeTimer()
                binding.btnPause.text = "일시정지"
            }
        }

        binding.btnFinish.setOnClickListener {
            Toast.makeText(this, "운동 완료! 수고하셨습니다! 💪", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // ============================
    // CameraX 설정 (Preview + 분석)
    // ============================
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // 분석용 ImageAnalysis
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageForPose(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
                Toast.makeText(this, "카메라를 시작할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true) {
            startCamera()
        } else {
            Toast.makeText(this, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            finish() // 권한 없으면 액티비티 종료
        }
    }

    // ============================
    // 타이머
    // ============================
    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
                // 피드백은 서버 응답에서 직접 업데이트하므로, 여기서는 타이머만 업데이트
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                updateTimerText()
                Toast.makeText(this@ExerciseExecutionActivity, "운동 완료! 🎉", Toast.LENGTH_SHORT).show()
                binding.btnFinish.performClick()
            }
        }.start()

        isRunning = true
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isRunning = false
    }

    private fun resumeTimer() {
        startTimer()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        binding.tvTimer.text = timeFormatted
    }

    // ============================
    // 포즈 분석 → 서버 호출 파트
    // ============================

    private fun processImageForPose(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()

        // 요청 너무 자주 보내지 않도록 간격 제한
        if (now - lastPoseSentAt < poseIntervalMs) {
            imageProxy.close()
            return
        }
        lastPoseSentAt = now

        // ImageProxy → Bitmap 변환
        val bitmap = imageProxy.toBitmap()
        imageProxy.close()

        // 서버로 전송 (Retrofit + 코루틴)
        lifecycleScope.launch {
            try {
                val base64 = bitmap.toBase64()
                val res = ApiClient.api.analyzePose(
                    PoseImageRequest(image_base64 = base64)
                )

                // UI 업데이트
                runOnUiThread {
                    binding.tvFeedback.text = res.feedback
                    // 필요하면 각도도 같이 표시 가능:
                    // binding.tvAngle.text = "무릎: %.1f°, 엉덩이: %.1f°".format(res.knee_angle, res.hip_angle)
                }

            } catch (e: Exception) {
                Log.e("POSE_API", "Error: ${e.message}", e)
                // 너무 자주 에러 메시지를 띄우면 시끄러우니 토스트는 생략하거나 디버그용으로만 사용
            }
        }
    }

    // ImageProxy → Bitmap 변환
    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 75, out)
        val jpegBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
    }

    // Bitmap → Base64 변환
    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        cameraExecutor.shutdown()
    }
}
