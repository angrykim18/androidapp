package com.example.newez // 본인 프로젝트의 패키지명으로 수정하세요

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivationChoiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation_choice)

        val hqButton = findViewById<Button>(R.id.button_hq_activation)
        val agencyButton = findViewById<Button>(R.id.button_agency_activation)

        // 본사 개통 버튼 클릭 리스너
        hqButton.setOnClickListener {

            val intent = Intent(this, HqActivationActivity::class.java)
             startActivity(intent)

            // 화면 전환 애니메이션 적용
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

            // 현재 액티비티 종료 (뒤로 가기 시 이 화면이 다시 나오지 않도록)
            finish()
        }

        // 대리점 개통 버튼 클릭 리스너
        agencyButton.setOnClickListener {
            val intent = Intent(this, ActivationActivity::class.java)
            startActivity(intent)

            // 화면 전환 애니메이션 적용
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

            // 현재 액티비티 종료
            finish()
        }
    }
}