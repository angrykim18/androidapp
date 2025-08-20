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

    // ✅ 메인 서버와 통신하는 공용 ApiService만 사용합니다.
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
        _episodes.postValue(emptyList()) // 이전 목록 초기화
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

    // ▼▼▼ [핵심 수정] 2단계 호출 로직을 제거하고, 원래의 단순한 1단계 호출로 변경합니다. ▼▼▼
    private fun fetchEpisodes() {
        isLoading = true
        viewModelScope.launch {
            try {
                val contentId = currentContentId ?: return@launch
                val deviceId = currentDeviceId ?: return@launch

                // 메인 서버에 deviceId를 보내 에피소드 목록을 한번에 요청합니다.
                // 서버가 알아서 그룹을 확인하고 올바른 URL을 만들어 줄 것입니다.
                val response = apiService.getEpisodes(contentId, deviceId, currentPage, pageSize, "fileOrder,desc")

                // 기존 목록에 새 페이지 내용을 추가합니다.
                val currentList = if (currentPage == 0) mutableListOf() else _episodes.value?.toMutableList() ?: mutableListOf()
                currentList.addAll(response.content)
                _episodes.postValue(currentList)
                isLastPage = response.last

            } catch (e: Exception) {
                e.printStackTrace()
                _episodes.postValue(emptyList()) // 에러 발생 시 빈 목록 처리
            } finally {
                isLoading = false
            }
        }
    }
}