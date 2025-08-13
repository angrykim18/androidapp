package com.example.newez.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newez.model.VodFile
import com.example.newez.network.ApiService
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.launch

class EpisodeViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitClient.instance

    private val _episodes = MutableLiveData<List<VodFile>>()
    val episodes: LiveData<List<VodFile>> = _episodes

    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private var currentContentId: Long? = null
    private var currentDeviceId: String? = null // ✅ [추가] deviceId를 저장할 변수
    private val pageSize = 25

    /**
     * ✅ [수정] contentId와 함께 deviceId도 받도록 변경
     */
    fun loadInitialEpisodes(contentId: Long, deviceId: String) {
        if (isLoading) return
        currentContentId = contentId
        currentDeviceId = deviceId // ✅ [추가] deviceId 저장
        currentPage = 0
        fetchEpisodes()
    }

    fun loadNextPage() {
        if (isLoading || isLastPage) return
        currentPage++
        fetchEpisodes()
    }

    fun loadPreviousPage() {
        if (isLoading || currentPage == 0) return
        currentPage--
        fetchEpisodes()
    }

    private fun fetchEpisodes() {
        isLoading = true
        viewModelScope.launch {
            try {
                // ✅ [추가] contentId 또는 deviceId가 없으면 실행하지 않음
                val contentId = currentContentId ?: return@launch
                val deviceId = currentDeviceId ?: return@launch

                // ✅ [수정] apiService.getEpisodes 호출 시 deviceId를 전달
                val response = apiService.getEpisodes(contentId, deviceId, currentPage, pageSize, "fileOrder,desc")

                val newList = response.content
                _episodes.postValue(newList)
                isLastPage = response.last

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}