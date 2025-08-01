package com.example.newez.adapter

import android.app.Activity // ✅ [추가] Activity import
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.R
import com.example.newez.model.CategoryDto
import com.example.newez.ui.ContentListActivity

class CategoryAdapter(private val categories: List<CategoryDto>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val menuTitle: TextView = view.findViewById(R.id.menu_title)
        val menuIcon: ImageView = view.findViewById(R.id.menu_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.menu_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.menuTitle.text = category.name
        holder.menuIcon.visibility = View.GONE

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ContentListActivity::class.java).apply {
                putExtra("CATEGORY_ID", category.id)
                putExtra("CATEGORY_NAME", category.name)
            }
            context.startActivity(intent)
            // ✅ [추가] 페이드 효과를 적용합니다.
            (context as? Activity)?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    override fun getItemCount() = categories.size
}