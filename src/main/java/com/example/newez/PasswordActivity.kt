package com.example.newez

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newez.ui.MovieCategoryActivity

class PasswordActivity : AppCompatActivity() {

    private lateinit var passwordInput: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private val defaultPassword = "7890"
    private val prefKey = "adult_password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        passwordInput = findViewById(R.id.editText_password)
        val confirmButton = findViewById<Button>(R.id.button_confirm)
        val cancelButton = findViewById<Button>(R.id.button_cancel)
        val changePasswordButton = findViewById<Button>(R.id.button_change_password)

        // 🔹 버튼 포커스 강조 효과 적용
        applyFocusEffect(confirmButton)
        applyFocusEffect(cancelButton)
        applyFocusEffect(changePasswordButton)

        confirmButton.setOnClickListener {
            checkPassword()
        }

        cancelButton.setOnClickListener {
            finish()
        }

        changePasswordButton.setOnClickListener {
            Toast.makeText(this, "비밀번호 변경 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPassword() {
        val enteredPassword = passwordInput.text.toString()
        val storedPassword = sharedPreferences.getString(prefKey, defaultPassword)

        if (enteredPassword == storedPassword) {
            Toast.makeText(this, "인증 성공", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MovieCategoryActivity::class.java).apply {
                putExtra("PARENT_ID", 69L)
                putExtra("CATEGORY_NAME", "성인방송")
            }
            startActivity(intent)
            finish()

        } else {
            Toast.makeText(this, "비밀번호가 틀립니다.", Toast.LENGTH_SHORT).show()
            passwordInput.text.clear()
        }
    }

    // 🔹 버튼 포커스 시 확대 + 그림자 강조
    private fun applyFocusEffect(button: Button) {
        button.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.scaleX = 1.15f
                v.scaleY = 1.15f
                v.elevation = 12f
            } else {
                v.scaleX = 1f
                v.scaleY = 1f
                v.elevation = 0f
            }
        }
    }
}
