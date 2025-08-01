package com.example.newez.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.PlayerActivity
import com.example.newez.R
import com.example.newez.model.VodFile

class EpisodeAdapter : ListAdapter<VodFile, EpisodeAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val episodeTitle: TextView = view.findViewById(R.id.textView_episode_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episode = getItem(position)
        holder.episodeTitle.text = episode.vodFileName // 앱 노출용 이름 표시

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PlayerActivity::class.java).apply {
                // ✅ [추가] PlayerActivity에 실제 영상 URL을 전달
                putExtra("STREAM_URL", episode.vodFileName)
            }
            context.startActivity(intent)
            (context as? Activity)?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<VodFile>() {
            override fun areItemsTheSame(oldItem: VodFile, newItem: VodFile): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: VodFile, newItem: VodFile): Boolean {
                return oldItem == newItem
            }
        }
    }
}