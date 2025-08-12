package com.example.newez.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newez.R
import com.example.newez.model.VodContent
import com.example.newez.ui.ContentDetailActivity

class ContentListAdapter :
    ListAdapter<VodContent, ContentListAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val posterFrame: FrameLayout = view.findViewById(R.id.poster_frame)
        val poster: ImageView = view.findViewById(R.id.imageView_poster)
        val title: TextView = view.findViewById(R.id.textView_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content = getItem(position)
        holder.title.text = content.title
        holder.title.isSelected = true

        Glide.with(holder.itemView.context)
            .load(content.posterPath)
            .into(holder.poster)

        // ✅ [수정] Toast 메시지 대신 ContentDetailActivity를 실행하는 코드로 변경
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ContentDetailActivity::class.java).apply {
                putExtra("CONTENT_ID", content.id)
            }
            context.startActivity(intent)
            (context as? Activity)?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        holder.itemView.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start()
                holder.posterFrame.foreground = ContextCompat.getDrawable(view.context, R.drawable.item_focus_border)
            } else {
                view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                holder.posterFrame.foreground = null
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<VodContent>() {
            override fun areItemsTheSame(oldItem: VodContent, newItem: VodContent): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: VodContent, newItem: VodContent): Boolean {
                return oldItem == newItem
            }
        }
    }
}