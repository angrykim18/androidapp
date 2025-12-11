package com.example.newez.ui

import android.animation.AnimatorInflater
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newez.R
import com.example.newez.model.VodContent
import com.example.newez.util.CustomKeyboardController
import com.example.newez.viewmodel.SearchViewModel

class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchInput: EditText
    private lateinit var searchButton: TextView
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var searchAdapter: SearchResultAdapter
    private var keyboardController: CustomKeyboardController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchInput = findViewById(R.id.editText_search_query)
        searchButton = findViewById(R.id.button_search)
        resultsRecyclerView = findViewById(R.id.recyclerView_search_results)

        val keyboardView = findViewById<View>(R.id.custom_keyboard_view)
        keyboardController = CustomKeyboardController(keyboardView, searchInput)

        setupRecyclerView()
        setupSearchListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchResultAdapter { content ->
            val intent = Intent(this, ContentDetailActivity::class.java).apply {
                putExtra("CONTENT_ID", content.id)
            }
            startActivity(intent)
        }
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        resultsRecyclerView.adapter = searchAdapter
    }

    private fun setupSearchListeners() {
        searchButton.setOnClickListener {
            performSearch()
        }
    }

    private fun performSearch() {
        val query = searchInput.text.toString().trim()
        if (query.isNotEmpty()) {
            viewModel.performSearch(query)
        }
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(this) { results ->
            searchAdapter.submitList(results)
        }
    }
}

// ✅ [수정] 검색 결과 어댑터의 ViewHolder 생성 부분을 수정하여 포커스 효과를 적용합니다.
class SearchResultAdapter(private val onClick: (VodContent) -> Unit) :
    RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    private var items: List<VodContent> = emptyList()

    fun submitList(list: List<VodContent>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = TextView(parent.context).apply {
            // dp 단위를 pixel 값으로 변환하는 내부 함수
            fun Int.toDp(): Int = (this * resources.displayMetrics.density).toInt()

            //--- ✅ [수정] 이 곳의 숫자들을 직접 조절하시면 됩니다. ---

            // ⭐ 1. 박스의 세로 크기 조절 (dp 단위)
            val boxHeight = 60.toDp()

            // ⭐ 2. 텍스트의 양끝(좌우) 여백 조절 (dp 단위)
            val horizontalPadding = 24.toDp()

            // ----------------------------------------------------

            // 스타일 적용
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, // 가로 크기는 꽉 채우기
                boxHeight
            ).apply {
                topMargin = 8
                bottomMargin = 8
            }
            setPadding(horizontalPadding, 0, horizontalPadding, 0)
            gravity = android.view.Gravity.CENTER_VERTICAL
            textSize = 22f
            setTextColor(parent.context.getColor(android.R.color.white))
            isFocusable = true
            isClickable = true
            setBackgroundResource(R.drawable.button_background_selector)
            //stateListAnimator = AnimatorInflater.loadStateListAnimator(parent.context, R.animator.focus_scale_animator)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        (holder.itemView as TextView).text = item.title
        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}