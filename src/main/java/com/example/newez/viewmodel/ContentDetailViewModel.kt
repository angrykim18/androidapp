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

    private val _contentDetail = MutableLiveData<VodContent>()
    val contentDetail: LiveData<VodContent> = _contentDetail

    // ✅ [수정] id와 함께 deviceId도 받도록 변경
    fun loadContentDetail(id: Long, deviceId: String) {
        viewModelScope.launch {
            try {
                // ✅ [수정] apiService 호출 시 deviceId를 전달
                val response = apiService.getVodContentDetail(id, deviceId)
                _contentDetail.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: 에러 처리
            }
        }
    }
}