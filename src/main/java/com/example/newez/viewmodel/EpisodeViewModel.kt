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

    // 현재 페이지 번호, 마지막 페이지 여부 등의 상태 관리
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private var currentContentId: Long? = null
    private val pageSize = 25

    /**
     * 콘텐츠의 첫 페이지(0번 페이지)를 불러옵니다.
     */
    fun loadInitialEpisodes(contentId: Long) {
        if (isLoading) return
        currentContentId = contentId
        currentPage = 0 // 항상 첫 페이지부터 시작
        fetchEpisodes()
    }

    /**
     * 다음 페이지를 불러옵니다.
     */
    fun loadNextPage() {
        // 로딩 중이거나 마지막 페이지이면 요청하지 않음
        if (isLoading || isLastPage) return
        currentPage++
        fetchEpisodes()
    }

    /**
     * 이전 페이지를 불러옵니다.
     */
    fun loadPreviousPage() {
        // 로딩 중이거나 첫 페이지(0)이면 요청하지 않음
        if (isLoading || currentPage == 0) return
        currentPage--
        fetchEpisodes()
    }

    /**
     * 지정된 페이지의 데이터를 불러와 LiveData를 갱신합니다.
     */
    private fun fetchEpisodes() {
        isLoading = true
        viewModelScope.launch {
            try {
                val contentId = currentContentId ?: return@launch
                val response = apiService.getEpisodes(contentId, currentPage, pageSize, "fileOrder,desc")

                // ✅ [수정] 역순으로 받아온 목록을 다시 뒤집던 .reversed() 함수를 삭제했습니다.
                val newList = response.content

                _episodes.postValue(newList)

                // 마지막 페이지 여부를 API 응답에 따라 갱신합니다.
                isLastPage = response.last

            } catch (e: Exception) {
                // 오류 처리는 일단 보류
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}