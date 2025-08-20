package com.example.newez.model

// UserManager에 있던 데이터 클래스들을 이곳으로 옮겨 공용으로 사용합니다.
data class ComprehensiveUserInfo(
    val userName: String?,
    val subscriptionEndDate: String?,
    val adultContentAllowed: Boolean?,
    val notice: String?,
    val userGroup: String?,
    val appUpdateInfo: AppUpdateInfo?,
    val adList: List<String>?,
    val generalNotice: String?,
    val urgentNotice: String?
)

data class AppUpdateInfo(
    val latestVersion: String?,
    val downloadUrl: String?,
    val isForced: Boolean?
)