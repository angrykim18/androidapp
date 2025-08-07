package com.example.newez

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.adapter.MainMenuAdapter
import com.example.newez.model.MainMenuItem

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 상단 사용자 정보 표시
        setupUserInfo()

        // RecyclerView를 사용해 메인 메뉴 표시
        setupMainMenu()
    }

    private fun setupUserInfo() {
        val userName = intent.getStringExtra("USER_NAME")
        val endDate = intent.getStringExtra("SUBSCRIPTION_END_DATE")

        findViewById<TextView>(R.id.user_name_textview).text = userName ?: "이름 없음"
        findViewById<TextView>(R.id.expire_date_textview).text = "종료일: ${endDate ?: "미지정"}"

    }

    private fun setupMainMenu() {

        val recyclerView = findViewById<RecyclerView>(R.id.main_menu_recyclerview)
        val adultContentAllowed = intent.getBooleanExtra("ADULT_CONTENT_ALLOWED", false)


        val menuItems = listOf(
            MainMenuItem("생방송", R.drawable.icon_live),
            MainMenuItem("영화", R.drawable.icon_movie),
            MainMenuItem("VOD 다시보기", R.drawable.icon_vod),
            MainMenuItem("실시간 다시보기", R.drawable.icon_realtime),
            MainMenuItem("성인방송", R.drawable.icon_19),
            MainMenuItem("검색", R.drawable.icon_serch)
        )

        recyclerView.adapter = MainMenuAdapter(menuItems, adultContentAllowed)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }
}