package com.example.newez.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.LiveChannelActivity
import com.example.newez.R
import com.example.newez.model.MainMenuItem
import com.example.newez.ui.MidCategoryActivity
import com.example.newez.ui.MovieCategoryActivity
import android.widget.Toast
import com.example.newez.PasswordActivity
import com.example.newez.ui.SearchActivity

class MainMenuAdapter(
    private val menuItems: List<MainMenuItem>,
    private val adultContentAllowed: Boolean ) :
    RecyclerView.Adapter<MainMenuAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val menuIcon: ImageView = view.findViewById(R.id.menu_icon)
        val menuTitle: TextView = view.findViewById(R.id.menu_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.menu_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.menuIcon.setImageResource(menuItem.iconResId)
        holder.menuTitle.text = menuItem.title

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            var intent: Intent? = null

            when (menuItem.title) {
                "생방송" -> {
                    intent = Intent(context, LiveChannelActivity::class.java)
                }

                "영화" -> {
                    intent = Intent(context, MovieCategoryActivity::class.java).apply {
                        // '영화'의 최상위 카테고리 ID를 전달합니다.
                        putExtra("PARENT_ID", 19L)
                        putExtra("CATEGORY_NAME", "영화")
                    }
                }
                // ✅ [추가] 'VOD 다시보기' 메뉴 클릭 시 MidCategoryActivity로 이동하는 동작
                "VOD 다시보기" -> {
                    intent = Intent(context, MidCategoryActivity::class.java)
                }

                "실시간 다시보기" -> {
                    Toast.makeText(context, "서비스 준비중입니다.", Toast.LENGTH_SHORT).show()
                }

                "성인방송" -> {
                    if (adultContentAllowed) {
                        intent = Intent(context, PasswordActivity::class.java)
                    } else {
                        // 허용되지 않은 사용자: 안내 메시지 표시
                        Toast.makeText(context, "해당 서비스를 이용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                "검색" -> {
                    intent = Intent(context, SearchActivity::class.java)



                }
            }
            intent?.let {
                context.startActivity(it)
                (context as? Activity)?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
    }

    override fun getItemCount() = menuItems.size
}