package com.example.newez // 이 줄은 본인 프로젝트 주소 그대로 두세요.

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment

class MainFragment : BrowseSupportFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 우리 TV 화면의 제목을 정해주는 부분입니다.
        title = "My TV App"
    }
}