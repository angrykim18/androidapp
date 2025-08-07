package com.example.newez.network

import com.example.newez.model.CategoryDto
import com.example.newez.model.PageResponse
import com.example.newez.model.VodContent
import com.example.newez.model.VodFile
import com.example.newez.model.WatchHistoryRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ✅ [수정] '영화' 전용 API를 삭제하고, parentId로 하위 카테고리를 가져오는 범용 API로 대체합니다.
    @GET("api/vod-categories")
    suspend fun getCategories(@Query("parentId") parentId: Long?): List<CategoryDto>

    @GET("api/vod-contents")
    suspend fun getVodContents(
        @Query("categoryId") categoryId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): PageResponse<VodContent>

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
    ): PageResponse<VodFile>

    @POST("api/history/update")
    suspend fun updateWatchHistory(@Body request: WatchHistoryRequest)

}