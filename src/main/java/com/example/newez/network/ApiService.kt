package com.example.newez.network

import com.example.newez.model.CategoryDto
import com.example.newez.model.PageResponse
import com.example.newez.model.VodContent
import com.example.newez.model.VodFile
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/movies/categories")
    suspend fun getMovieCategories(): List<CategoryDto>

    @GET("api/vod-contents")
    suspend fun getVodContents(
        @Query("categoryId") categoryId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): PageResponse<VodContent>

    @GET("api/vod-contents/{id}")
    suspend fun getVodContentDetail(@Path("id") id: Long): VodContent

    @GET("api/vod-files")
    suspend fun getEpisodes(
        @Query("contentId") contentId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int,
        // ✅ [수정] 정렬 조건을 전달할 sort 파라미터를 추가합니다.
        @Query("sort") sort: String
    ): PageResponse<VodFile>
}