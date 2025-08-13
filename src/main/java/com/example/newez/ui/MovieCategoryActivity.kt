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

        val parentId = intent.getLongExtra("PARENT_ID", 19L) // 기본값 19 (영화)
        val categoryName = intent.getStringExtra("CATEGORY_NAME")
        if (categoryName != null) {
            binding.textViewPageTitle.text = categoryName
        }

        setupRecyclerView()
        observeViewModel()

        val shouldFilterAdultContent = (categoryName != "성인방송")
        viewModel.fetchCategories(parentId, true)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCategories.layoutManager = GridLayoutManager(this, 5)
    }

    private fun observeViewModel() {
        viewModel.categories.observe(this) { categories ->

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