package com.example.newez.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newez.model.CategoryDto
import com.example.newez.network.ApiService
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.launch

class MidCategoryViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitClient.instance

    private val _categories = MutableLiveData<List<CategoryDto>>()
    val categories: LiveData<List<CategoryDto>> = _categories

    fun fetchCategories(parentId: Long?) {
        viewModelScope.launch {
            try {
                val response = apiService.getCategories(parentId)

                // ✅ [추가] '영화'와 '성인'이 포함된 카테고리를 제외하는 필터링 로직
                val filteredList = response.filter {
                    !it.name.contains("영화") && !it.name.contains("성인")
                }

                _categories.postValue(filteredList)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}