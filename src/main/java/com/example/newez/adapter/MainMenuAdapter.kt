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
import com.example.newez.ui.MovieCategoryActivity // ✅ [추가] MovieCategoryActivity import

class MainMenuAdapter(private val menuItems: List<MainMenuItem>) :
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

            if (menuItem.title == "생방송") {
                intent = Intent(context, LiveChannelActivity::class.java)
                // ✅ [추가] '영화' 메뉴 클릭 시 MovieCategoryActivity로 이동하는 동작 추가
            } else if (menuItem.title == "영화") {
                intent = Intent(context, MovieCategoryActivity::class.java)
            }

            intent?.let {
                context.startActivity(it)
                (context as? Activity)?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
    }

    override fun getItemCount() = menuItems.size
}