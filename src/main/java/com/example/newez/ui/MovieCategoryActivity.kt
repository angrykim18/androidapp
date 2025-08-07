package com.example.newez.ui

import android.content.Intent
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
        binding = ActivityMovieCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ [수정] VOD 다시보기에서 전달받은 parentId와 카테고리 이름을 사용합니다.
        // 만약 전달받은 값이 없으면 '영화' 메뉴로 간주하고 기본값을 사용합니다.
        val parentId = intent.getLongExtra("PARENT_ID", 19L) // 기본값 19 (영화)
        val categoryName = intent.getStringExtra("CATEGORY_NAME")
        if (categoryName != null) {
            binding.textViewPageTitle.text = categoryName
        }

        setupRecyclerView()
        observeViewModel()

        // ✅ [수정] ViewModel 호출 시, parentId와 함께 성인 필터링 옵션을 true로 전달합니다.
        val shouldFilterAdultContent = (categoryName != "성인방송")
        viewModel.fetchCategories(parentId, true)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCategories.layoutManager = GridLayoutManager(this, 5)
    }

    private fun observeViewModel() {
        viewModel.categories.observe(this) { categories ->
            // ✅ [수정] 카테고리 클릭 시, ContentListActivity로 이동하도록 클릭 리스너를 추가합니다.
            binding.recyclerViewCategories.adapter = CategoryAdapter(categories) { clickedCategory ->
                val intent = Intent(this, ContentListActivity::class.java).apply {
                    putExtra("CATEGORY_ID", clickedCategory.id)
                    putExtra("CATEGORY_NAME", clickedCategory.name)
                }
                startActivity(intent)
            }
        }
    }
}