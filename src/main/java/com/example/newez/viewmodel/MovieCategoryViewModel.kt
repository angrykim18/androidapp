package com.example.newez.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newez.model.CategoryDto
import com.example.newez.network.ApiService
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.launch

class MovieCategoryViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitClient.instance

    private val _categories = MutableLiveData<List<CategoryDto>>()
    val categories: LiveData<List<CategoryDto>> = _categories

    // ✅ [수정] 성인 컨텐츠 필터링 여부를 결정하는 'filterAdultContent' 파라미터를 추가합니다.
    fun fetchCategories(parentId: Long?, filterAdultContent: Boolean) {
        viewModelScope.launch {
            try {
                val response = apiService.getCategories(parentId)

                // ✅ [수정] filterAdultContent가 true일 때만 '성인'이 포함된 카테고리를 필터링합니다.
                if (filterAdultContent) {
                    val filteredData = response.filter { !it.name.contains("성인") }
                    _categories.postValue(filteredData)
                } else {
                    _categories.postValue(response)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}