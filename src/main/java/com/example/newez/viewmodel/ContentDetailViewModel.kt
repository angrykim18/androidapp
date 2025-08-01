package com.example.newez.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newez.model.VodContent
import com.example.newez.network.ApiService
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.launch

class ContentDetailViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitClient.instance

    // ✅ [추가] 컨텐츠 상세 정보를 담을 LiveData
    private val _contentDetail = MutableLiveData<VodContent>()
    val contentDetail: LiveData<VodContent> = _contentDetail

    // ✅ [추가] ID를 이용해 컨텐츠 상세 정보를 불러오는 함수
    fun loadContentDetail(id: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.getVodContentDetail(id)
                _contentDetail.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: 에러 처리
            }
        }
    }
}