package com.example.newez; // ❗️사용자님의 실제 패키지 이름으로 변경해야 합니다.

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MovieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 이 코드가 activity_movie.xml 레이아웃을 화면에 표시하는 역할을 합니다.
        setContentView(R.layout.activity_movie);
    }
}