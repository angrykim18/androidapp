package com.example.newez.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newez.model.Page
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
    private var currentDeviceId: String? = null
    private val pageSize = 25

    fun loadInitialEpisodes(contentId: Long, deviceId: String) {
        if (isLoading) return
        currentContentId = contentId
        currentDeviceId = deviceId
        currentPage = 0
        isLastPage = false
        // [수정] 초기 로딩 시 이전 페이지로 갈 수 없으므로, 현재 페이지를 비워줍니다.
        _episodes.postValue(emptyList())
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
                val contentId = currentContentId ?: return@launch
                val deviceId = currentDeviceId ?: return@launch
                val response = apiService.getEpisodes(contentId, deviceId, currentPage, pageSize, "fileOrder,desc")

                // [수정] 기존 목록에 추가하는 대신, 새 페이지의 내용으로 '교체'하도록 변경
                _episodes.postValue(response.content)

                isLastPage = response.last

            } catch (e: Exception) {
                e.printStackTrace()
                _episodes.postValue(emptyList())
            } finally {
                isLoading = false
            }
        }
    }
}