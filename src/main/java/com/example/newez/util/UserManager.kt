package com.example.newez.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.newez.model.AppUpdateInfo
import com.example.newez.model.ComprehensiveUserInfo
import com.example.newez.network.ApiService
import com.example.newez.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object UserManager {

    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> = _userName

    private val _subscriptionEndDate = MutableLiveData<String?>()
    val subscriptionEndDate: LiveData<String?> = _subscriptionEndDate

    private val _isAdultContentAllowed = MutableLiveData<Boolean>()
    val isAdultContentAllowed: LiveData<Boolean> = _isAdultContentAllowed

    private val _notice = MutableLiveData<String?>()
    val notice: LiveData<String?> = _notice

    private val _userGroup = MutableLiveData<String?>()
    val userGroup: LiveData<String?> = _userGroup

    private val _appUpdateInfo = MutableLiveData<AppUpdateInfo?>()
    val appUpdateInfo: LiveData<AppUpdateInfo?> = _appUpdateInfo

    private val _adList = MutableLiveData<List<String>?>()
    val adList: LiveData<List<String>?> = _adList

    private val _generalNotice = MutableLiveData<String?>()
    val generalNotice: LiveData<String?> = _generalNotice

    private val _urgentNotice = MutableLiveData<String?>()
    val urgentNotice: LiveData<String?> = _urgentNotice

    private val apiService: ApiService = RetrofitClient.instance


    suspend fun refresh(context: Context) = withContext(Dispatchers.IO) {
        val deviceId = getDeviceId(context) ?: return@withContext

        try {
            val userInfo = apiService.getUserInfo(deviceId)

            Log.d("UserManager", "사용자 정보 업데이트 성공: ${userInfo.userName}")

            _userName.postValue(userInfo.userName)
            _subscriptionEndDate.postValue(userInfo.subscriptionEndDate)
            _isAdultContentAllowed.postValue(userInfo.adultContentAllowed ?: false)
            _notice.postValue(userInfo.notice)
            _userGroup.postValue(userInfo.userGroup)
            _appUpdateInfo.postValue(userInfo.appUpdateInfo)
            _adList.postValue(userInfo.adList)
            _generalNotice.postValue(userInfo.generalNotice)
            _urgentNotice.postValue(userInfo.urgentNotice)

        } catch (e: Exception) {
            Log.e("UserManager", "사용자 정보 업데이트 실패", e)
        }
    }


    suspend fun checkSubscription(context: Context) = withContext(Dispatchers.IO) {
        val deviceId = getDeviceId(context) ?: throw IllegalStateException("사용자 정보를 찾을 수 없습니다.")

        try {
            val userInfo = apiService.getUserInfo(deviceId)

            if (isDateExpired(userInfo.subscriptionEndDate)) {
                throw SubscriptionExpiredException()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getDeviceId(context: Context): String? {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("device_id", null)
        if (deviceId.isNullOrEmpty()) {
            Log.e("UserManager", "저장된 deviceId가 없습니다.")
        }
        return deviceId
    }

    private fun isDateExpired(endDateString: String?): Boolean {
        if (endDateString.isNullOrEmpty()) return true
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val endDate = formatter.parse(endDateString) ?: return true
            val todayCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            endDate.before(todayCalendar.time)
        } catch (e: Exception) {
            true
        }
    }

    // 만료 예외 클래스
    class SubscriptionExpiredException : Exception("시청 기간이 만료되었습니다.")
}