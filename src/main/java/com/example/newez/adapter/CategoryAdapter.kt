package com.example.newez.adapter

import android.app.Activity
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

// ✅ [수정] 생성자에서 클릭 리스너를 전달받도록 변경합니다.
class CategoryAdapter(
    private val categories: List<CategoryDto>,
    private val listener: (CategoryDto) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

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

        // ✅ [수정] 클릭 시, 생성자에서 받은 listener를 호출합니다.
        holder.itemView.setOnClickListener {
            listener(category)
        }
    }

    override fun getItemCount() = categories.size
}