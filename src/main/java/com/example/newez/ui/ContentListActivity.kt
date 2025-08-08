package com.example.newez.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.R
import com.example.newez.adapter.ContentListAdapter
import com.example.newez.viewmodel.ContentListViewModel
import android.view.View

class ContentListActivity : AppCompatActivity() {

    private val viewModel: ContentListViewModel by viewModels()
    private lateinit var contentAdapter: ContentListAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var countTextView: TextView
    private lateinit var pageInfoTextView: TextView
    private lateinit var previousButton: TextView
    private lateinit var nextButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_list)

        val categoryId = intent.getLongExtra("CATEGORY_ID", -1)
        val categoryName = intent.getStringExtra("CATEGORY_NAME")

        findViewById<TextView>(R.id.textView_category_title).text = categoryName
        countTextView = findViewById(R.id.textView_content_count)
        pageInfoTextView = findViewById(R.id.textView_page_info)
        previousButton = findViewById(R.id.button_previous)
        nextButton = findViewById(R.id.button_next)
        recyclerView = findViewById(R.id.recyclerView_contents)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        if (categoryId != -1L) {
            viewModel.loadInitialContents(categoryId)
        }
    }

    private fun setupRecyclerView() {
        contentAdapter = ContentListAdapter()
        // ✅ [수정] 한 화면에 12개가 잘 보이도록 한 줄의 아이템 개수를 6개로 유지
        recyclerView.layoutManager = GridLayoutManager(this, 6)
        recyclerView.adapter = contentAdapter
    }

    private fun setupClickListeners() {
        previousButton.setOnClickListener {
            viewModel.loadPreviousPage()
        }
        nextButton.setOnClickListener {
            viewModel.loadNextPage()
        }
    }

    private fun observeViewModel() {
        viewModel.displayedContents.observe(this) { contents ->
            contentAdapter.submitList(contents) {
                // ✅ [유지] 리스트 업데이트 후, 첫 페이지일 경우 첫번째 아이템으로 포커스 이동
                if (viewModel.isFirstPage.value == true && contents.isNotEmpty()) {
                    recyclerView.post {
                        recyclerView.getChildAt(0)?.requestFocus()
                    }
                }
            }
        }
        viewModel.totalContentCount.observe(this) { count ->
            countTextView.text = "총 ${count}개"
        }
        viewModel.pageInfo.observe(this) { info ->
            pageInfoTextView.text = info
        }
        viewModel.isFirstPage.observe(this) { isFirst ->
            previousButton.isEnabled = !isFirst
        }
        viewModel.isLastPage.observe(this) { isLast ->
            nextButton.isEnabled = !isLast
        }
    }
}