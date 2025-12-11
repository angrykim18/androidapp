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


interface OnEpisodeClickListener {
    fun onEpisodeClick(episode: VodFile)
}


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