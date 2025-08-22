package com.example.newez

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.newez.ui.MovieCategoryActivity
import android.widget.TextView

class PasswordActivity : AppCompatActivity() {

    private lateinit var passwordInput: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private val defaultPassword = "7890"
    private val resetCode = "10004" // 마스터 초기화 비밀번호
    private val prefKey = "adult_password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        passwordInput = findViewById(R.id.editText_password)
        val confirmButton = findViewById<Button>(R.id.button_confirm)
        val cancelButton = findViewById<Button>(R.id.button_cancel)
        val changePasswordButton = findViewById<Button>(R.id.button_change_password)

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
            // 기존 '준비 중' 토스트 메시지 대신 다이얼로그를 띄우는 함수를 호출합니다.
            showChangePasswordDialog()
        }
    }

    private fun checkPassword() {
        val enteredPassword = passwordInput.text.toString()
        val storedPassword = sharedPreferences.getString(prefKey, defaultPassword)

        // 1. 마스터 비밀번호인지 가장 먼저 확인합니다.
        if (enteredPassword == resetCode) {
            sharedPreferences.edit().putString(prefKey, defaultPassword).apply()
            Toast.makeText(this, "비밀번호가 기본값 으로 초기화되었습니다.", Toast.LENGTH_LONG).show()
            passwordInput.text.clear()
            return
        }

        // 2. 저장된 비밀번호와 일치하는지 확인합니다.
        if (enteredPassword == storedPassword) {
            Toast.makeText(this, "인증 성공", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MovieCategoryActivity::class.java).apply {
                putExtra("PARENT_ID", 69L)
                putExtra("CATEGORY_NAME", "성인방송")
            }
            startActivity(intent)
            finish()
        } else {
            // 3. 모두 아니라면 틀린 비밀번호입니다.
            Toast.makeText(this, "비밀번호가 틀립니다.", Toast.LENGTH_SHORT).show()
            passwordInput.text.clear()
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showChangePasswordDialog() {
        // 이전에 생성한 dialog_change_password.xml 레이아웃을 가져옵니다.
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)
        val oldPasswordEditText = dialogView.findViewById<EditText>(R.id.editText_old_password)
        val newPasswordEditText = dialogView.findViewById<EditText>(R.id.editText_new_password)
        val confirmPasswordEditText = dialogView.findViewById<EditText>(R.id.editText_confirm_password)
        val dialogCancelButton = dialogView.findViewById<TextView>(R.id.button_cancel_change)
        val dialogConfirmButton = dialogView.findViewById<TextView>(R.id.button_confirm_change)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true) // 바깥 영역 클릭 시 닫히도록 설정
            .create()

        dialogCancelButton.setOnClickListener {
            dialog.dismiss() // 취소 버튼: 다이얼로그 닫기
        }

        dialogConfirmButton.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val storedPassword = sharedPreferences.getString(prefKey, defaultPassword)

            // 유효성 검사
            if (newPassword.isBlank() || oldPassword.isBlank() || confirmPassword.isBlank()){
                Toast.makeText(this, "모든 칸을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (oldPassword != storedPassword) {
                Toast.makeText(this, "기존 비밀번호가 틀립니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 모든 검사를 통과하면 새 비밀번호 저장
            sharedPreferences.edit().putString(prefKey, newPassword).apply()
            Toast.makeText(this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

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