package com.example.newez

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ 서버에 다시 요청하는 코드를 모두 삭제합니다.
        // ✅ Intent로부터 직접 사용자 정보를 받아옵니다.
        val userName = intent.getStringExtra("USER_NAME")
        val endDate = intent.getStringExtra("SUBSCRIPTION_END_DATE")

        val userNameTextView = findViewById<TextView>(R.id.user_name_textview)
        val expireDateTextView = findViewById<TextView>(R.id.expire_date_textview)

        // ✅ 받아온 정보로 즉시 화면을 업데이트합니다.
        userNameTextView.text = userName ?: "이름 없음"
        expireDateTextView.text = "종료일: ${endDate ?: "미지정"}"

        // --- 메인 메뉴 생성 ---
        val menuContainer = findViewById<LinearLayout>(R.id.main_menu_container)
        val menuItems = listOf("생방송", "영화", "VOD 다시보기", "실시간 다시보기", "성인방송", "검색")

        for (item in menuItems) {
            val menuButton = layoutInflater.inflate(R.layout.menu_item_layout, menuContainer, false)
            val menuTitle = menuButton.findViewById<TextView>(R.id.menu_title)
            val menuIcon = menuButton.findViewById<ImageView>(R.id.menu_icon)
            menuTitle.text = item

            when (item) {
                "생방송" -> menuIcon.setImageResource(R.drawable.icon_live)
                "영화" -> menuIcon.setImageResource(R.drawable.icon_movie)
                "VOD 다시보기" -> menuIcon.setImageResource(R.drawable.icon_vod)
                "실시간 다시보기" -> menuIcon.setImageResource(R.drawable.icon_realtime)
                "성인방송" -> menuIcon.setImageResource(R.drawable.icon_19)
                "검색" -> menuIcon.setImageResource(R.drawable.icon_serch)
                else -> menuIcon.setImageResource(android.R.drawable.ic_media_play)
            }

            menuButton.setOnClickListener {
                if (item == "생방송") {
                    val intent = Intent(this, LiveChannelActivity::class.java)
                    startActivity(intent)
                    // ✅ [애니메이션 추가]
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
                // 다른 메뉴 아이템에 대한 처리도 여기에 추가할 수 있습니다.
            }

            menuContainer.addView(menuButton)
        }
    }
}