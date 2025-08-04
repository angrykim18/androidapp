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
        // connection_status_textview는 원본 레이아웃에 있으므로 그대로 둡니다.
        // 필요하다면 여기서 텍스트를 설정할 수 있습니다.
    }

    private fun setupMainMenu() {
        // 1. 레이아웃에서 RecyclerView를 찾습니다.
        val recyclerView = findViewById<RecyclerView>(R.id.main_menu_recyclerview)

        // 2. 메뉴에 표시할 데이터 목록을 만듭니다. (아이콘 포함)
        val menuItems = listOf(
            MainMenuItem("생방송", R.drawable.icon_live),
            MainMenuItem("영화", R.drawable.icon_movie),
            MainMenuItem("VOD 다시보기", R.drawable.icon_vod),
            MainMenuItem("실시간 다시보기", R.drawable.icon_realtime),
            MainMenuItem("성인방송", R.drawable.icon_19),
            MainMenuItem("검색", R.drawable.icon_serch)
        )

        // 3. 어댑터를 생성하고 RecyclerView에 연결합니다.
        recyclerView.adapter = MainMenuAdapter(menuItems)

        // 4. 레이아웃 매니저를 설정합니다. (가로로 스크롤되는 목록)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }
}