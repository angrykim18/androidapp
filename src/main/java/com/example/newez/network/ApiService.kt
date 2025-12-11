package com.example.newez.network

import com.example.newez.SplashApiService
import com.example.newez.model.CategoryDto
import com.example.newez.model.ComprehensiveUserInfo
import com.example.newez.model.IpUpdateRequest
import com.example.newez.model.Page // 👈 [수정] PageResponse 대신 Page를 import
import com.example.newez.model.VodContent
import com.example.newez.model.VodFile
import com.example.newez.model.WatchHistoryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class UserGroupInfo(
    val liveServerUrl: String?,
    val vodServerUrl: String?
)

data class ApiLiveChannel(
    val id: Long,
    val channelName: String,
    val streamUrl: String
)

interface ApiService {

    // --- 사용자 및 그룹 관련 API ---
    @GET("api/users/device/{deviceId}")
    suspend fun getUserInfo(@Path("deviceId") deviceId: String): ComprehensiveUserInfo

    @GET("api/user-groups/name/{groupName}")
    suspend fun getGroupInfo(@Path("groupName") groupName: String): UserGroupInfo

    // --- 생방송 관련 API ---
    @GET("/api/live-channels")
    suspend fun getLiveChannels(@Query("deviceId") deviceId: String): List<ApiLiveChannel>

    // --- VOD, 검색 등 API ---
    @GET("api/vod-contents/search")
    suspend fun searchVodContents(
        @Query("title") title: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Page<VodContent> // 👈 PageResponse에서 Page로 수정

    @GET("api/vod-categories")
    suspend fun getCategories(@Query("parentId") parentId: Long?): List<CategoryDto>

    @GET("api/vod-contents")
    suspend fun getVodContents(
        @Query("categoryId") categoryId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Page<VodContent> // 👈 PageResponse에서 Page로 수정

    @GET("api/vod-contents/{id}")
    suspend fun getVodContentDetail(
        @Path("id") id: Long,
        @Query("deviceId") deviceId: String
    ): VodContent

    @GET("api/vod-files")
    suspend fun getEpisodes(
        @Query("contentId") contentId: Long,
        @Query("deviceId") deviceId: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String
    ): Page<VodFile> // 👈 [핵심 수정] PageResponse에서 Page로 수정

    @POST("api/history/update")
    suspend fun updateWatchHistory(@Body request: WatchHistoryRequest)

    @POST("api/users/device/{deviceId}/ip")
    suspend fun updateUserIp(
        @Path("deviceId") deviceId: String,
        @Body request: IpUpdateRequest
    ): Response<Unit>


}