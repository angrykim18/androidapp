package com.example.newez.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newez.model.VodContent
import com.example.newez.network.ApiService
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ContentListViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitClient.instance

    private val _displayedContents = MutableLiveData<List<VodContent>>()
    val displayedContents: LiveData<List<VodContent>> = _displayedContents

    private val allLoadedContents = mutableListOf<VodContent>()

    private val _totalContentCount = MutableLiveData<Long>()
    val totalContentCount: LiveData<Long> = _totalContentCount

    private val _pageInfo = MutableLiveData<String>()
    val pageInfo: LiveData<String> = _pageInfo

    private val _isFirstPage = MutableLiveData<Boolean>()
    val isFirstPage: LiveData<Boolean> = _isFirstPage

    private val _isLastPage = MutableLiveData<Boolean>()
    val isLastPage: LiveData<Boolean> = _isLastPage

    private var currentPageIndex = 0
    private var highestPageLoaded = -1
    private var totalPages = 1
    private var isLoading = false
    private var currentCategoryId: Long? = null
    private val pageSize = 12

    fun loadInitialContents(categoryId: Long) {
        if (isLoading) return
        currentCategoryId = categoryId
        currentPageIndex = 0
        highestPageLoaded = -1
        allLoadedContents.clear()
        fetchPages(listOf(0, 1, 2))
    }

    fun loadNextPage() {
        if (currentPageIndex >= totalPages - 1) return
        currentPageIndex++
        updateDisplayedPage()
    }

    fun loadPreviousPage() {
        if (currentPageIndex <= 0) return
        currentPageIndex--
        updateDisplayedPage()
    }

    private fun updateDisplayedPage() {
        val start = currentPageIndex * pageSize
        val end = (start + pageSize).coerceAtMost(allLoadedContents.size)
        if (start < end) {
            // ✅ [수정] .toList()를 추가하여 원본이 아닌 '복사본'을 전달합니다.
            _displayedContents.postValue(allLoadedContents.subList(start, end).toList())
        }

        _pageInfo.postValue("${currentPageIndex + 1} / $totalPages 페이지")
        _isFirstPage.postValue(currentPageIndex == 0)
        _isLastPage.postValue(currentPageIndex >= totalPages - 1 && highestPageLoaded >= totalPages -1)

        val nextPageToLoad1 = highestPageLoaded + 1
        val nextPageToLoad2 = highestPageLoaded + 2
        val pagesToFetch = mutableListOf<Int>()
        if (nextPageToLoad1 < totalPages) pagesToFetch.add(nextPageToLoad1)
        if (nextPageToLoad2 < totalPages) pagesToFetch.add(nextPageToLoad2)

        if (pagesToFetch.isNotEmpty()) {
            fetchPages(pagesToFetch)
        }
    }

    private fun fetchPages(pagesToFetch: List<Int>) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            try {
                val categoryId = currentCategoryId ?: return@launch
                val responses = pagesToFetch.map { page ->
                    async { apiService.getVodContents(categoryId, page, pageSize) }
                }.awaitAll()

                responses.forEach { response ->
                    val startIdx = response.number * pageSize
                    while (allLoadedContents.size < startIdx + response.content.size) {
                        allLoadedContents.add(VodContent(-1, "", "", "", -1))
                    }
                    response.content.forEachIndexed { index, vodContent ->
                        allLoadedContents[startIdx + index] = vodContent
                    }
                }

                highestPageLoaded = pagesToFetch.maxOrNull() ?: highestPageLoaded

                responses.firstOrNull()?.let {
                    totalPages = it.totalPages
                    _totalContentCount.postValue(it.totalElements)
                }

                updateDisplayedPage()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}