package com.example.newez.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newez.model.PageResponse
import com.example.newez.model.VodContent
import com.example.newez.network.ApiService
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitClient.instance

    private val _searchResults = MutableLiveData<List<VodContent>>()
    val searchResults: LiveData<List<VodContent>> = _searchResults

    private val _pageInfo = MutableLiveData<PageResponse<VodContent>?>()
    val pageInfo: LiveData<PageResponse<VodContent>?> = _pageInfo

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentQuery = ""
    private var currentPage = 0

    fun performSearch(query: String) {
        if (query.isBlank()) return
        currentQuery = query
        currentPage = 0
        _searchResults.value = emptyList() // 이전 결과 초기화
        fetchResults()
    }

    fun loadNextPage() {
        pageInfo.value?.let {
            if (!it.last && !_isLoading.value!!) {
                currentPage++
                fetchResults()
            }
        }
    }

    private fun fetchResults() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val response = apiService.searchVodContents(currentQuery, currentPage, 20)
                val currentList = if (currentPage == 0) mutableListOf() else _searchResults.value?.toMutableList() ?: mutableListOf()
                currentList.addAll(response.content)
                _searchResults.postValue(currentList)
                _pageInfo.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}