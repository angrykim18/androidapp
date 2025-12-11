package com.example.newez.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.LiveChannelActivity
import com.example.newez.PasswordActivity
import com.example.newez.R
import com.example.newez.data.UserManager
import com.example.newez.model.MainMenuItem
import com.example.newez.ui.MidCategoryActivity
import com.example.newez.ui.MovieCategoryActivity
import com.example.newez.ui.SearchActivity

/**
 * [수정] '성인방송' 메뉴의 보임/숨김 처리를 제거하고, 클릭 시에만 권한을 확인하도록 변경합니다.
 */
class MainMenuAdapter(
    private val menuItems: List<MainMenuItem>
    // [삭제] adultContentAllowed를 생성자에서 받지 않습니다.
) : RecyclerView.Adapter<MainMenuAdapter.ViewHolder>() {

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

        // [삭제] '성인방송' 메뉴를 숨기거나 크기를 조절하는 코드를 모두 제거했습니다.
        // 이제 모든 메뉴는 항상 동일한 크기와 모양으로 표시됩니다.

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            var intent: Intent? = null

            when (menuItem.title) {
                "생방송" -> {
                    intent = Intent(context, LiveChannelActivity::class.java)
                }
                "영화" -> {
                    intent = Intent(context, MovieCategoryActivity::class.java).apply {
                        putExtra("PARENT_ID", 19L)
                        putExtra("CATEGORY_NAME", "영화")
                    }
                }
                "VOD다시보기" -> {
                    intent = Intent(context, MidCategoryActivity::class.java)
                }
                "실시간다시보기" -> {
                    Toast.makeText(context, "서비스 준비중입니다.", Toast.LENGTH_SHORT).show()
                }
                "성인방송" -> {
                    // [수정] 클릭하는 순간에 UserManager에서 최신 권한 정보를 가져와 확인합니다.
                    val isAllowed = UserManager.isAdultContentAllowed.value ?: false
                    if (isAllowed) {
                        intent = Intent(context, PasswordActivity::class.java)
                    } else {
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
