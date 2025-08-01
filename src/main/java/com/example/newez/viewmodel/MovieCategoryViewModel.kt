package com.example.newez.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newez.model.CategoryDto
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.launch

class MovieCategoryViewModel : ViewModel() {

    private val _categories = MutableLiveData<List<CategoryDto>>()
    val categories: LiveData<List<CategoryDto>> = _categories

    // ✅ [수정] 전체 함수 내용을 API를 호출하는 코드로 변경
    fun fetchCategories() {
        // ViewModel이 살아있는 동안만 안전하게 네트워크 작업을 실행
        viewModelScope.launch {
            try {
                // 1. RetrofitClient를 통해 백엔드 API를 호출하고 응답을 받음
                val response = RetrofitClient.instance.getMovieCategories()

                // 2. '성인'이 포함된 카테고리를 필터링
                val filteredData = response.filter { !it.name.contains("성인") }

                // 3. 최종 데이터를 LiveData에 전달하여 화면에 표시
                _categories.postValue(filteredData)

            } catch (e: Exception) {
                // 네트워크 에러 등 예외 발생 시 처리
                e.printStackTrace()
                // TODO: 사용자에게 에러 메시지를 보여주는 로직 추가 가능
            }
        }
    }
}