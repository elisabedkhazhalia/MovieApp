package com.example.movie_application.ui.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movie_application.R
import com.example.movie_application.databinding.FragmentSearchBinding
import com.example.movie_application.adapters.MovieAdapter

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private val searchAdapter = MovieAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        setupRecyclerView()

        // 1. კლავიატურის მოსმენა (Search Listener)
        binding.etSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val query = binding.etSearch.text.toString()
                if (query.isNotEmpty()) {
                    performSearch(query)
                } else {
                    Toast.makeText(context, "Please enter a movie name", Toast.LENGTH_SHORT).show()
                }
                return@setOnEditorActionListener true
            }
            false
        }

        // 2. შედეგების მიღება ViewModel-იდან
        viewModel.searchResults.observe(viewLifecycleOwner) { movies ->
            // პროგრეს ბარის დამალვა, როცა შედეგი მოვა
            binding.progressBar.visibility = View.GONE

            if (movies != null) {
                Log.d("SearchFragment", "ნაპოვნია: ${movies.size}")
                searchAdapter.differ.submitList(movies)
            }
        }

        // 3. ფილმზე დაჭერა
        searchAdapter.setOnItemClickListener { movie ->
            val bundle = Bundle().apply {
                putParcelable("movie", movie)
            }
            findNavController().navigate(
                R.id.action_searchFragment_to_movieDetailsFragment,
                bundle
            )
        }
    }

    private fun performSearch(query: String) {
        // პროგრეს ბარის გამოჩენა
        binding.progressBar.visibility = View.VISIBLE

        Log.d("SearchFragment", "ვიწყებ ძებნას: $query")
        viewModel.searchMovies(query)
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setupRecyclerView() {
        binding.rvSearchResults.apply {
            adapter = searchAdapter
            layoutManager = GridLayoutManager(activity, 2)
        }
    }
}