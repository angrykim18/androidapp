package com.example.newez

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.network.ApiLiveChannel

class LiveChannelAdapter(
    private val channels: List<ApiLiveChannel>,
    private val listener: OnChannelClickListener
) : RecyclerView.Adapter<LiveChannelAdapter.ChannelViewHolder>() {

    interface OnChannelClickListener {
        fun onChannelClick(channel: ApiLiveChannel)


    }

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val channelName: TextView = itemView.findViewById(R.id.channel_name_textview)

        init {
            itemView.setOnClickListener {
                // ✅ [핵심 수정] deprecated된 adapterPosition을 bindingAdapterPosition으로 변경
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onChannelClick(channels[position])
                }
            }

            itemView.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    // channelName(글씨) 대신 v(itemView, 박스 전체)의 크기를 키웁니다.
                    v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).start()
                } else {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.live_channel_item, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]
        holder.channelName.text = channel.channelName
    }

    override fun getItemCount() = channels.size
}