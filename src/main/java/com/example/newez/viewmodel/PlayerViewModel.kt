package com.example.newez.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newez.model.WatchHistoryRequest
import com.example.newez.network.ApiService
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitClient.instance

    fun updateWatchHistory(deviceId: String, vodFileId: Long, timestampSeconds: Int) {
        viewModelScope.launch {
            try {
                val request = WatchHistoryRequest(deviceId, vodFileId, timestampSeconds)
                apiService.updateWatchHistory(request)
            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }
}