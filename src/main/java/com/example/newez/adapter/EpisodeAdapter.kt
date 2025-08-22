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

// ✅ [수정] 클릭 리스너 인터페이스에 deviceId를 전달하도록 변경
interface OnEpisodeClickListener {
    fun onEpisodeClick(episode: VodFile)
}

// ✅ [수정] 생성자에서 listener를 받도록 변경
class EpisodeAdapter(private val listener: OnEpisodeClickListener) : ListAdapter<VodFile, EpisodeAdapter.ViewHolder>(DiffCallback) {

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
        holder.episodeTitle.text = episode.vodFileName

        // ✅ [수정] 클릭 시 Activity에 구현된 listener를 호출
        holder.itemView.setOnClickListener {
            listener.onEpisodeClick(episode)
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