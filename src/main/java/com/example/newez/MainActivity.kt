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

        val userName = intent.getStringExtra("USER_NAME")
        val endDate = intent.getStringExtra("SUBSCRIPTION_END_DATE")

        val userNameTextView = findViewById<TextView>(R.id.user_name_textview)
        val expireDateTextView = findViewById<TextView>(R.id.expire_date_textview)

        userNameTextView.text = userName ?: "이름 없음"
        expireDateTextView.text = "종료일: ${endDate ?: "미지정"}"

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
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
                // ✅ [추가] '영화' 메뉴를 클릭했을 때 MovieActivity를 시작하는 코드
                else if (item == "영화") {
                    val intent = Intent(this, MovieActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
            }

            menuContainer.addView(menuButton)
        }
    }
}