package com.example.newez.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.newez.adapter.CategoryAdapter
import com.example.newez.databinding.ActivityMovieCategoryBinding
import com.example.newez.viewmodel.MovieCategoryViewModel

class MovieCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieCategoryBinding
    private val viewModel: MovieCategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ✅ [추가] ViewBinding을 사용해 화면을 설정합니다.
        binding = ActivityMovieCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ [추가] RecyclerView와 ViewModel을 설정하는 함수들을 호출합니다.
        setupRecyclerView()
        observeViewModel()
        viewModel.fetchCategories()
    }

    private fun setupRecyclerView() {
        // ✅ [추가] 하위 카테고리를 한 줄에 5개씩 보여주는 그리드로 설정합니다.
        binding.recyclerViewCategories.layoutManager = GridLayoutManager(this, 5)
    }

    private fun observeViewModel() {
        // ✅ [추가] ViewModel의 데이터 변경을 감지하고, 변경 시 Adapter에 데이터를 전달합니다.
        viewModel.categories.observe(this) { categories ->
            binding.recyclerViewCategories.adapter = CategoryAdapter(categories)
        }
    }
}