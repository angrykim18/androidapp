package com.example.newez.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.newez.adapter.CategoryAdapter
import com.example.newez.databinding.ActivityMidCategoryBinding
import com.example.newez.viewmodel.MidCategoryViewModel

class MidCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMidCategoryBinding
    private val viewModel: MidCategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMidCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()

        // ✅ [수정] 'VOD 다시보기'의 중간 카테고리들은 parent_id가 NULL이므로,
        // parentId를 null로 전달하여 최상위 카테고리를 불러옵니다.
        viewModel.fetchCategories(null)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCategories.layoutManager = GridLayoutManager(this, 5)
    }

    private fun observeViewModel() {
        viewModel.categories.observe(this) { categories ->
            // 카테고리(예: '드라마') 클릭 시, MovieCategoryActivity로 이동하도록 클릭 리스너를 설정합니다.
            binding.recyclerViewCategories.adapter = CategoryAdapter(categories) { clickedCategory ->
                val intent = Intent(this, MovieCategoryActivity::class.java).apply {
                    // 다음 화면에 클릭된 카테고리의 ID와 이름을 전달합니다.
                    putExtra("PARENT_ID", clickedCategory.id)
                    putExtra("CATEGORY_NAME", clickedCategory.name)
                }
                startActivity(intent)
            }
        }
    }
}